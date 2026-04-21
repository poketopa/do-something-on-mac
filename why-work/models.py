from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship
from database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)

    assets = relationship("PortfolioAsset", back_populates="owner")

class PortfolioAsset(Base):
    __tablename__ = "portfolio_assets"

    id = Column(Integer, primary_key=True, index=True)
    symbol = Column(String(20), index=True, nullable=False)
    quantity = Column(Float, nullable=False)
    screener = Column(String(20), nullable=False)
    exchange = Column(String(20), nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))
    preset = Column(Integer, nullable=False, default=1, index=True)

    owner = relationship("User", back_populates="assets") 