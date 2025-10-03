import os
from fastapi import FastAPI
import uvicorn
from logging_config import configure_logging
from mcp_tools import mcp
import logging

# import the transaction router we just added
from routers import router as transaction_routers

logger = logging.getLogger(__name__)

def create_app() -> FastAPI:
    # Initialize logging for the app
    configure_logging()
    logger = logging.getLogger(__name__)
    
  
   #Add mcp server to the FastAPI app
    mcp_app = mcp.http_app(path='/mcp')
    app = FastAPI(title="Transaction API and MCP server", lifespan=mcp_app.lifespan)
    app.mount("/mcp", mcp_app)

    # Include the transaction router
    app.include_router(transaction_routers, prefix="/api/transactions", tags=["transactions"]) 

    logger.info("FastAPI application created successfully")
    return app

app = create_app()

if __name__ == "__main__":
 
    profile = os.environ.get("PROFILE", "prod")
    port = 8071 if profile == "dev" else 8080
    logger.info(f"Starting transaction service server with profile: {profile}, port: {port}")
    #run app as uvicorn server
    uvicorn.run("main:app", host="0.0.0.0", port=port)
