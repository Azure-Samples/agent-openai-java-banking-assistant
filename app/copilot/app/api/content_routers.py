import mimetypes
import logging
from io import BytesIO
from fastapi import APIRouter, HTTPException, Depends, UploadFile, File
from fastapi.responses import StreamingResponse
from dependency_injector.wiring import Provide, inject

# Foundry Agent based dependencies
from app.config.container_foundry import Container

# Azure Chat based agents dependencies
#from app.config.container_azure_chat import Container

from app.helpers.blob_proxy import BlobStorageProxy

router = APIRouter()
logger = logging.getLogger(__name__)


@router.get("/content/{file_name}")
@inject
async def get_content(
    file_name: str,
    blob_proxy: BlobStorageProxy = Depends(Provide[Container.blob_proxy])
):
    """Download a file from blob storage."""
    logger.info(f"Received request for content with name [{file_name}]")
    
    if not file_name or not file_name.strip():
        logger.warning("file name cannot be null or empty")
        raise HTTPException(status_code=400, detail="File name cannot be null or empty")
    
    try:
        file_bytes = blob_proxy.get_file_as_bytes(file_name)
    except Exception as ex:
        logger.error(f"Cannot retrieve file [{file_name}] from blob: {str(ex)}")
        raise HTTPException(status_code=404, detail="File not found")
    
    # Guess content type from filename
    content_type, _ = mimetypes.guess_type(file_name)
    if content_type is None:
        content_type = "application/octet-stream"
    
    # Create a BytesIO stream from the file bytes
    file_stream = BytesIO(file_bytes)
    
    return StreamingResponse(
        BytesIO(file_bytes),
        media_type=content_type,
        headers={
            "Content-Disposition": f"inline; filename={file_name}"
        }
    )


@router.post("/content")
@inject
async def upload_content(
    file: UploadFile = File(...),
    blob_proxy: BlobStorageProxy = Depends(Provide[Container.blob_proxy])
):
    """Upload a file to blob storage."""
    logger.info(f"Received request to upload a file [{file.filename}]")
    
    if not file.filename:
        logger.warning("Uploaded file has no filename")
        raise HTTPException(status_code=400, detail="File must have a filename")
    
    try:
        file_bytes = await file.read()
        
        if not file_bytes:
            logger.warning("Uploaded file is empty")
            raise HTTPException(status_code=400, detail="Uploaded file is empty")
        
        blob_proxy.store_file(file_bytes, file.filename)
        
    except Exception as ex:
        logger.error(f"Cannot store file [{file.filename}] to blob: {str(ex)}")
        raise HTTPException(status_code=500, detail="Error occurred while storing file")
    
    return {"filename": file.filename, "message": "File uploaded successfully"}
