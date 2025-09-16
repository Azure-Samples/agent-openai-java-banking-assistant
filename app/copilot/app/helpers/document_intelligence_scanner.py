"""Document Intelligence scanner for invoice processing.

Provides helpers to scan invoice documents using Azure Document Intelligence service.
Configuration is externalized in ``app.config.settings`` which builds the
client using the application's credential strategy.
"""
import logging
from typing import Dict, Optional, Annotated
from pathlib import Path
from semantic_kernel.functions import kernel_function
from azure.ai.documentintelligence import DocumentIntelligenceClient
from azure.ai.documentintelligence.models import AnalyzeDocumentRequest
from app.config.settings import settings
from app.config.azure_credential import get_azure_credential
from app.helpers.blob_proxy import BlobStorageProxy

logger = logging.getLogger(__name__)


class DocumentIntelligenceInvoiceScanHelper:
    """Helper class for scanning invoices using Azure Document Intelligence.

    Usage:
        scanner = DocumentIntelligenceInvoiceScanHelper(client, blob_proxy)
        result = scanner.scan("path/to/invoice.pdf")
        result = scanner.scan_file(Path("local/file.pdf"))
    """

    def __init__(
        self,
        client: DocumentIntelligenceClient,
        blob_storage_proxy: BlobStorageProxy,
        model_id: str = "prebuilt-invoice"
    ) -> None:
        """Initialize the invoice scanner.
        
        Args:
            client: Azure Document Intelligence client
            blob_storage_proxy: Blob storage proxy for file operations
            model_id: Document Intelligence model ID (default: prebuilt-invoice)
        """
        self._client = client
        self._blob_storage_proxy = blob_storage_proxy
        self._model_id = model_id

    def scan(self, blob_name: str) -> Dict[str, str]:
        """Scan an invoice document from blob storage.

        Args:
            blob_name: Name of the blob containing the invoice document

        Returns:
            Dictionary containing extracted invoice fields

        Raises:
            Azure SDK exceptions on failure
        """
        logger.info("Retrieving blob file with name [%s]", blob_name)

        blob_data = self._blob_storage_proxy.get_file_as_bytes(blob_name)
        logger.debug("Found blob file with name [%s] and size [%d]", blob_name, len(blob_data))

        return self._internal_scan(blob_data)

    def scan_file(self, file_path: Path) -> Dict[str, str]:
        """Scan an invoice document from local file.

        Args:
            file_path: Path to the local invoice file

        Returns:
            Dictionary containing extracted invoice fields

        Raises:
            FileNotFoundError if file doesn't exist
            Azure SDK exceptions on failure
        """
        with open(file_path, "rb") as file:
            file_data = file.read()

        return self._internal_scan(file_data)

    def _internal_scan(self, file_data: bytes) -> Dict[str, str]:
        """Internal method to scan document data.

        Args:
            file_data: Binary data of the document

        Returns:
            Dictionary containing extracted invoice fields
        """
        logger.debug("Document intelligence: start extracting data...")

        # Create analyze request
        analyze_request = AnalyzeDocumentRequest(bytes_source=file_data)
        
        # Start analysis operation
        poller = self._client.begin_analyze_document(
            model_id=self._model_id,
            body=analyze_request
        )

        # Get the result
        result = poller.result()

        scan_data = {}

        if result.documents:
            for document in result.documents:
                if document.fields:
                    # Extract vendor name
                    vendor_name = document.fields.get("VendorName")
                    if vendor_name and vendor_name.value_string:
                        scan_data["VendorName"] = vendor_name.value_string

                    # Extract vendor address
                    vendor_address = document.fields.get("VendorAddress")
                    if vendor_address and vendor_address.content:
                        scan_data["VendorAddress"] = vendor_address.content

                    # Extract customer name
                    customer_name = document.fields.get("CustomerName")
                    if customer_name and customer_name.value_string:
                        scan_data["CustomerName"] = customer_name.value_string

                    # Extract customer address recipient
                    customer_address_recipient = document.fields.get("CustomerAddressRecipient")
                    if customer_address_recipient and customer_address_recipient.value_string:
                        scan_data["CustomerAddressRecipient"] = customer_address_recipient.value_string

                    # Extract invoice ID
                    invoice_id = document.fields.get("InvoiceId")
                    if invoice_id and invoice_id.value_string:
                        scan_data["InvoiceId"] = invoice_id.value_string

                    # Extract invoice date
                    invoice_date = document.fields.get("InvoiceDate")
                    if invoice_date and invoice_date.value_date:
                        scan_data["InvoiceDate"] = invoice_date.value_date.isoformat()

                    # Extract invoice total
                    invoice_total = document.fields.get("InvoiceTotal")
                    if invoice_total and invoice_total.content:
                        scan_data["InvoiceTotal"] = invoice_total.content

        return scan_data

    @kernel_function(description="Extract the invoice or bill data scanning a photo or image")
    def scan_invoice_plugin(
        self, 
        blob_name: Annotated[str, "the path to the file containing the image or photo"]
    ) -> Annotated[str, "Returns a JSON string containing extracted invoice fields like VendorName, CustomerName, InvoiceId, InvoiceDate, and InvoiceTotal"]:
        """Semantic Kernel plugin function to scan invoice documents.
        
        This function exposes the scan method as a Semantic Kernel plugin,
        allowing it to be used by AI agents for invoice processing.
        
        Args:
            blob_name: Name of the blob containing the invoice document
            
        Returns:
            JSON string representation of extracted invoice fields
        """
        import json
        
        try:
            scan_result = self.scan(blob_name)
            logger.debug("Scan result: %s", scan_result)
            # Convert dictionary to JSON string for semantic kernel compatibility
            return json.dumps(scan_result, indent=2)
        except Exception as e:
            logger.error("Error scanning invoice with blob name [%s]: %s", blob_name, str(e))
            return json.dumps({"error": f"Failed to scan invoice: {str(e)}"})
    
    # semantic-kernel plugin for scan
