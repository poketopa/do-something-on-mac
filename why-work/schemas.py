from pydantic import BaseModel
from typing import Dict, List, Optional

# --- Asset Schemas ---
class PortfolioAssetBase(BaseModel):
    symbol: str
    quantity: float
    screener: str
    exchange: str
    preset: int = 1

class PortfolioAssetCreate(PortfolioAssetBase):
    pass

class PortfolioAsset(PortfolioAssetBase):
    id: int
    owner_id: int

    class Config:
        orm_mode = True

# --- User Schemas ---
class UserBase(BaseModel):
    username: str

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int
    is_active: bool = True
    assets: List[PortfolioAsset] = []

    class Config:
        orm_mode = True

class UserResponse(UserBase):
    id: int
    is_active: bool = True

    class Config:
        orm_mode = True

# --- Token Schema ---
class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    username: Optional[str] = None

# --- Portfolio Update Schema ---
class PortfolioUpdate(BaseModel):
    assets: List[PortfolioAssetBase] 