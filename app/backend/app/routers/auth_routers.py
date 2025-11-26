from fastapi import APIRouter
from fastapi.responses import JSONResponse

router = APIRouter()

@router.get("/auth_setup")
def auth_setup():
    return JSONResponse(content={"useLogin": False})
