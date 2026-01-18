# API Gateway Service
# FastAPI-based gateway that proxies authentication to IDM and routes to other services

from fastapi import FastAPI, HTTPException, Header, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import grpc
import httpx
import os

# gRPC imports
import idm_pb2
import idm_pb2_grpc

app = FastAPI(
    title="EventFlow API Gateway",
    description="API Gateway for EventFlow platform",
    version="1.0.0"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000", "http://localhost:80", "http://localhost"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configuration
IDM_HOST = os.getenv("IDM_HOST", "localhost")
IDM_PORT = os.getenv("IDM_PORT", "50051")
BILETE_SERVICE_URL = os.getenv("BILETE_SERVICE_URL", "http://localhost:8080")
CLIENTI_SERVICE_URL = os.getenv("CLIENTI_SERVICE_URL", "http://localhost:8081")


# Models
class LoginRequest(BaseModel):
    email: str
    password: str


class RegisterRequest(BaseModel):
    email: str
    password: str
    role: str = "client"


# gRPC client for IDM
def get_idm_stub():
    channel = grpc.insecure_channel(f"{IDM_HOST}:{IDM_PORT}")
    return idm_pb2_grpc.IdentityServiceStub(channel)


# ============================================================================
# AUTHENTICATION ENDPOINTS
# ============================================================================

@app.post("/api/auth/login")
async def login(request: LoginRequest):
    """Authenticate user and return JWT token"""
    try:
        stub = get_idm_stub()
        grpc_request = idm_pb2.AuthRequest(
            username=request.email,
            password=request.password
        )
        response = stub.Authenticate(grpc_request)
        
        if response.success:
            # Validate token to get user info
            validate_request = idm_pb2.TokenRequest(token=response.token)
            validate_response = stub.ValidateToken(validate_request)
            
            return {
                "success": True,
                "token": response.token,
                "userId": validate_response.user_id,
                "role": validate_response.role,
                "message": response.message
            }
        else:
            raise HTTPException(status_code=401, detail=response.message)
    except grpc.RpcError as e:
        raise HTTPException(status_code=503, detail=f"IDM service unavailable: {e.details()}")


@app.post("/api/auth/logout")
async def logout(authorization: str = Header(None)):
    """Invalidate JWT token"""
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=400, detail="Missing or invalid Authorization header")
    
    token = authorization[7:]
    
    try:
        stub = get_idm_stub()
        grpc_request = idm_pb2.TokenRequest(token=token)
        response = stub.InvalidateToken(grpc_request)
        
        return {
            "success": response.success,
            "message": response.message
        }
    except grpc.RpcError as e:
        raise HTTPException(status_code=503, detail=f"IDM service unavailable: {e.details()}")


@app.post("/api/auth/register")
async def register(request: RegisterRequest):
    """Register a new user (admin only in production)"""
    try:
        stub = get_idm_stub()
        grpc_request = idm_pb2.CreateUserRequest(
            email=request.email,
            password=request.password,
            role=request.role
        )
        response = stub.CreateUser(grpc_request)
        
        if response.success:
            return {
                "success": True,
                "userId": response.user_id,
                "message": response.message
            }
        else:
            raise HTTPException(status_code=400, detail=response.message)
    except grpc.RpcError as e:
        raise HTTPException(status_code=503, detail=f"IDM service unavailable: {e.details()}")


@app.post("/api/auth/validate")
async def validate_token(authorization: str = Header(None)):
    """Validate a JWT token"""
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=400, detail="Missing or invalid Authorization header")
    
    token = authorization[7:]
    
    try:
        stub = get_idm_stub()
        grpc_request = idm_pb2.TokenRequest(token=token)
        response = stub.ValidateToken(grpc_request)
        
        return {
            "valid": response.valid,
            "userId": response.user_id,
            "role": response.role,
            "message": response.message
        }
    except grpc.RpcError as e:
        raise HTTPException(status_code=503, detail=f"IDM service unavailable: {e.details()}")


# ============================================================================
# HEALTH CHECK
# ============================================================================

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "api-gateway"}


@app.get("/")
async def root():
    """Root endpoint with API info"""
    return {
        "service": "EventFlow API Gateway",
        "version": "1.0.0",
        "endpoints": {
            "auth": "/api/auth/login, /api/auth/logout, /api/auth/register, /api/auth/validate",
            "docs": "/docs"
        }
    }


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("GATEWAY_PORT", "8000"))
    uvicorn.run(app, host="0.0.0.0", port=port)
