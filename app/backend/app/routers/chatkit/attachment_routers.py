import mimetypes
import logging
from io import BytesIO
from fastapi import APIRouter, HTTPException, Depends, UploadFile, File
from fastapi.responses import  StreamingResponse, FileResponse
from starlette.responses import JSONResponse
from dependency_injector.wiring import Provide, inject

# Foundry Agent based dependencies
#from app.config.container_foundry import Container

# Azure Chat based agents dependencies
from app.config.container_azure_chat import Container

from app.helpers.blob_proxy import BlobStorageProxy
from .sqllite_store import SQLiteStore

router = APIRouter()
logger = logging.getLogger(__name__)

data_store = SQLiteStore()
DEFAULT_USER_ID = "demo_user"


@router.post("/upload/{attachment_id}")
@inject
async def upload_file(attachment_id: str, 
                      file: UploadFile = File(...),
                      blob_proxy: BlobStorageProxy = Depends(Provide[Container.blob_proxy])):
    """Handle file upload for two-phase upload.

    The client POSTs the file bytes here after creating the attachment
    via the ChatKit attachments.create endpoint.
    """
    logger.info(f"Receiving file upload for attachment: {attachment_id}")

    try:
        # Read file contents
        contents = await file.read()

        # Save to azure storage
        blob_proxy.store_file(contents, attachment_id)

        logger.info(f"Saved {len(contents)} bytes for {file.filename} as attachment {attachment_id} in blob storage")

        # Load the attachment metadata from the data store
        attachment = await data_store.load_attachment(attachment_id, {"user_id": DEFAULT_USER_ID})

        # Clear the upload_url since upload is complete
        attachment.upload_url = None

        # Save the updated attachment back to the store
        await data_store.save_attachment(attachment, {"user_id": DEFAULT_USER_ID})

        # Return the attachment metadata as JSON
        return JSONResponse(content=attachment.model_dump(mode="json"))

    except Exception as e:
        logger.error(f"Error uploading file for attachment {attachment_id}: {e}", exc_info=True)
        return JSONResponse(status_code=500, content={"error": f"Failed to upload file: {str(e)}"})


@router.get("/preview/{attachment_id}")
@inject
async def preview_image(attachment_id: str,
                         blob_proxy: BlobStorageProxy = Depends(Provide[Container.blob_proxy])):
    """Serve image preview/thumbnail.

    For simplicity, this serves the full image. In production, you should
    generate and cache thumbnails.
    """
    logger.debug(f"Serving preview for attachment: {attachment_id}")

    try:
        try:
            file_bytes = blob_proxy.get_file_as_bytes(attachment_id)
        except Exception:
            return JSONResponse(status_code=404, content={"error": "File not found in blob storage for " + attachment_id})

    
        # Determine media type from file extension or attachment metadata
        # For simplicity, we'll try to load from the store
        try:
            attachment = await data_store.load_attachment(attachment_id, {"user_id": DEFAULT_USER_ID})
            media_type = attachment.mime_type
        except Exception:
            # Default to binary if we can't determine
            media_type = "application/octet-stream"

         # Create a BytesIO stream from the file bytes
        file_stream = BytesIO(file_bytes)
    
        return StreamingResponse(
            BytesIO(file_bytes),
            media_type=media_type
        )
        

    except Exception as e:
        logger.error(f"Error serving preview for attachment {attachment_id}: {e}", exc_info=True)
        return JSONResponse(status_code=500, content={"error": str(e)})