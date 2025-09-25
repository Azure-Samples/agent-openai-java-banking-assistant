from fastmcp import FastMCP
import logging
from services import PaymentService
from models import Payment

logger = logging.getLogger(__name__)
payment_service = PaymentService()
mcp = FastMCP("Payment MCP Server")


@mcp.tool(name="processPayment", description="Submit a payment request")
def process_payment(payment_obj: Payment):
    # accept either dict or Payment model
    logger.info("processPayment called with payment=%s", payment_obj)
    # if isinstance(payment, dict):
    #     payment_obj = Payment(**payment)
    # elif isinstance(payment, Payment):
    #     payment_obj = payment
    # else:
    #     raise ValueError("Unsupported payment type")

    payment_service.process_payment(payment_obj)
    return {"status": "ok"}
