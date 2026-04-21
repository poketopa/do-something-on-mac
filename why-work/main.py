import asyncio
import time
from typing import Dict, List

import aiohttp
import uvicorn
import yfinance as yf
from fastapi import FastAPI, Request, Depends, HTTPException, status, Query
from fastapi.responses import JSONResponse, RedirectResponse
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from sqlalchemy.future import select
from sqlalchemy.orm import Session
from tradingview_ta import TA_Handler, Interval

# local imports
import auth
import models
import schemas
from database import engine, get_db

# --- Database Initialization ---
async def create_tables():
    async with engine.begin() as conn:
        await conn.run_sync(models.Base.metadata.create_all)

# --- FastAPI App Initialization ---
app = FastAPI(
    title="Why-Work API",
    description="개인 포트폴리오 관리 웹앱 API 문서",
    version="1.0.0",
    docs_url="/swagger-ui",
    redoc_url="/redoc",
    openapi_url="/api/openapi.json",
)

@app.on_event("startup")
async def on_startup():
    await create_tables()

# --- Static files and Templates ---
app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")

# --- Security and Authentication ---
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/token")

async def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    """Decodes token, gets username, fetches user, and returns user's essential data as a dict."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    username = auth.decode_access_token(token)
    if username is None:
        raise credentials_exception
    
    query = select(models.User).where(models.User.username == username)
    result = await db.execute(query)
    user = result.scalar_one_or_none()

    if user is None:
        raise credentials_exception
    return {"id": user.id, "username": user.username}


# --- API Endpoints ---
@app.get("/")
async def root(request: Request):
    """Serves the main index.html page."""
    return templates.TemplateResponse("index.html", {"request": request})

@app.get("/docs", include_in_schema=False)
async def docs_redirect():
    """Redirects default docs path to custom Swagger UI path."""
    return RedirectResponse(url="/swagger-ui")

# --- User and Auth Routes ---
@app.post("/api/users/signup", response_model=schemas.UserResponse, status_code=status.HTTP_201_CREATED)
async def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    """Handles user registration."""
    query = select(models.User).where(models.User.username == user.username)
    result = await db.execute(query)
    db_user = result.scalar_one_or_none()

    if db_user:
        raise HTTPException(status_code=400, detail="Username already registered")
        
    hashed_password = auth.get_password_hash(user.password)
    db_user = models.User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    await db.commit()
    await db.refresh(db_user)
    return db_user

@app.post("/api/token", response_model=schemas.Token)
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """Handles user login and returns a JWT access token."""
    query = select(models.User).where(models.User.username == form_data.username)
    result = await db.execute(query)
    user = result.scalar_one_or_none()
    
    if not user or not auth.verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token = auth.create_access_token(data={"sub": user.username})
    return {"access_token": access_token, "token_type": "bearer"}


# --- Portfolio Routes ---
@app.get("/api/portfolio", response_model=List[schemas.PortfolioAsset])
async def get_user_portfolio(
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
    preset: int = Query(1, ge=1, le=3)
):
    query = select(models.PortfolioAsset).where(
        models.PortfolioAsset.owner_id == current_user["id"],
        models.PortfolioAsset.preset == preset
    )
    result = await db.execute(query)
    assets = result.scalars().all()
    return assets

@app.post("/api/portfolio", status_code=status.HTTP_204_NO_CONTENT)
async def save_user_portfolio(
    portfolio: schemas.PortfolioUpdate, 
    current_user: dict = Depends(get_current_user), 
    db: Session = Depends(get_db),
    preset: int = Query(1, ge=1, le=3)
):
    # 기존 자산 모두 삭제 (해당 preset만)
    result = await db.execute(select(models.PortfolioAsset).where(
        models.PortfolioAsset.owner_id == current_user["id"],
        models.PortfolioAsset.preset == preset
    ))
    assets = result.scalars().all()
    for asset in assets:
        await db.delete(asset)
    await db.commit()
    # 새 자산 추가
    for asset_data in portfolio.assets:
        asset = models.PortfolioAsset(
            symbol=asset_data.symbol,
            quantity=asset_data.quantity,
            screener=asset_data.screener,
            exchange=asset_data.exchange,
            owner_id=current_user["id"],
            preset=preset
        )
        db.add(asset)
    await db.commit()

@app.get("/api/portfolio/value")
async def get_portfolio_value(
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db),
    preset: int = Query(1, ge=1, le=3)
):
    result = await db.execute(
        select(models.PortfolioAsset).where(
            models.PortfolioAsset.owner_id == current_user["id"],
            models.PortfolioAsset.preset == preset
        )
    )
    assets = result.scalars().all()
    if not assets:
        return {"portfolio": [], "total_usd": 0, "total_krw": 0}
    try:
        loop = asyncio.get_event_loop()
        usd_krw_rate_task = loop.run_in_executor(None, get_usd_krw_from_tv)
        usdt_krw_rate_task = loop.run_in_executor(None, get_usdt_krw_from_tv)
        usd_krw_rate, usdt_krw_rate = await asyncio.gather(usd_krw_rate_task, usdt_krw_rate_task)

        async def process_asset(asset):
            symbol = asset.symbol
            quantity = asset.quantity
            screener = asset.screener
            exchange = asset.exchange
            if screener.lower() == "cash":
                if symbol.upper() == "KRW":
                    usd_price = quantity / usd_krw_rate
                    krw_price = quantity
                elif symbol.upper() == "USD":
                    usd_price = quantity
                    krw_price = quantity * usd_krw_rate
                elif symbol.upper() == "USDT":
                    usd_price = quantity
                    krw_price = quantity * usdt_krw_rate
                else:
                    usd_price = 0
                    krw_price = 0
                total_usd = usd_price
                total_krw = krw_price
            else:
                is_crypto = screener.lower() == "crypto"
                if is_crypto:
                    usd_price = await get_crypto_price(symbol, screener, exchange)
                    krw_price = usd_price * usdt_krw_rate
                else:
                    usd_price = await loop.run_in_executor(None, get_stock_price, symbol)
                    krw_price = usd_price * usd_krw_rate
                total_usd = usd_price * quantity
                total_krw = krw_price * quantity
            return {
                "symbol": symbol,
                "quantity": quantity,
                "screener": screener,
                "exchange": exchange,
                "usd_price": usd_price,
                "krw_price": krw_price,
                "total_usd": total_usd,
                "total_krw": total_krw,
            }

        tasks = [process_asset(asset) for asset in assets]
        results = await asyncio.gather(*tasks)
        total_usd = sum(r['total_usd'] for r in results)
        total_krw = sum(r['total_krw'] for r in results)
        return {
            "success": True,
            "portfolio": results,
            "total_usd": total_usd,
            "total_krw": total_krw,
            "usd_krw_rate": usd_krw_rate,
            "usdt_krw_rate": usdt_krw_rate
        }
    except Exception as e:
        print("포트폴리오 계산 에러:", e)
        import traceback; traceback.print_exc()
        return JSONResponse(status_code=500, content={"success": False, "error": str(e)})


# --- Data Fetching Logic (with Caching) ---
# Note: This part is mostly unchanged from the original file.
api_cache = {}
CACHE_DURATION = 600  # 10분

COINGECKO_IDS = {"BTC": "bitcoin", "ETH": "ethereum"}

def get_usd_krw_from_tv() -> float:
    cache_key = "tv_usd_krw_rate"
    if cache_key in api_cache and time.time() - api_cache[cache_key]['timestamp'] < CACHE_DURATION:
        return api_cache[cache_key]['data']
    handler = TA_Handler(symbol="USDKRW", screener="forex", exchange="FX_IDC", interval=Interval.INTERVAL_1_MINUTE)
    price = handler.get_analysis().indicators["close"]
    api_cache[cache_key] = {'data': price, 'timestamp': time.time()}
    return price

def get_usdt_krw_from_tv() -> float:
    cache_key = "tv_usdt_krw_rate"
    if cache_key in api_cache and time.time() - api_cache[cache_key]['timestamp'] < CACHE_DURATION:
        return api_cache[cache_key]['data']
    handler = TA_Handler(symbol="USDTKRW", screener="crypto", exchange="BITHUMB", interval=Interval.INTERVAL_1_MINUTE)
    price = handler.get_analysis().indicators["close"]
    api_cache[cache_key] = {'data': price, 'timestamp': time.time()}
    return price

async def get_crypto_price(symbol: str, screener: str, exchange: str) -> float:
    # tradingview-ta는 동기 라이브러리이므로 run_in_executor로 실행
    def fetch_price():
        handler = TA_Handler(
            symbol=symbol,
            screener=screener,
            exchange=exchange,
            interval=Interval.INTERVAL_1_MINUTE
        )
        analysis = handler.get_analysis()
        return analysis.indicators["close"]
    loop = asyncio.get_event_loop()
    price = await loop.run_in_executor(None, fetch_price)
    return price

def get_stock_price(symbol: str) -> float:
    try:
        cache_key = f"stock_price_{symbol}"
        if cache_key in api_cache and time.time() - api_cache[cache_key]['timestamp'] < CACHE_DURATION:
            return api_cache[cache_key]['data']
        ticker = yf.Ticker(symbol)
        price = ticker.info.get("regularMarketPrice") or ticker.history(period="1d")['Close'][0]
        api_cache[cache_key] = {'data': price, 'timestamp': time.time()}
        return price
    except Exception:
        if cache_key in api_cache:
            return api_cache[cache_key]['data']
        return 0.0

# --- Main execution ---
if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=True)