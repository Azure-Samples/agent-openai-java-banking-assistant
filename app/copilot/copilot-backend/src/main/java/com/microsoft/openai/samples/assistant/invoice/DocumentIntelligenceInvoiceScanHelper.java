package com.microsoft.openai.samples.assistant.invoice;



import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.*;
import com.azure.core.util.polling.SyncPoller;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DocumentIntelligenceInvoiceScanHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIntelligenceInvoiceScanHelper.class);

    private final DocumentIntelligenceClient client;
    
    private final BlobStorageProxy blobStorageProxy;

    private final String modelId;

   public DocumentIntelligenceInvoiceScanHelper(DocumentIntelligenceClient client, BlobStorageProxy blobStorageProxy){
        this.client = client;
        this.modelId = "prebuilt-invoice";
        this.blobStorageProxy = blobStorageProxy;
    }
  
    public  Map<String, String> scan(String blobName) throws IOException {

       LOGGER.info("Retrieving blob file with name [{}]", blobName);

       byte[] blobData = blobStorageProxy.getFileAsBytes(blobName);

        LOGGER.debug("Found blob file with name [{}] and size [{}]", blobName,blobData.length);
        SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeInvoicePoller =
                client.beginAnalyzeDocument("prebuilt-invoice",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,  new AnalyzeDocumentRequest().setBase64Source(blobStorageProxy.getFileAsBytes(blobName)));

        return internalScan(analyzeInvoicePoller);
        
    }
    public Map<String,String> scan (File file) throws IOException {

        SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeInvoicePoller =
                client.beginAnalyzeDocument("prebuilt-invoice",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,  new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(file.toPath())));

        return internalScan(analyzeInvoicePoller);

    }

    private  Map<String, String> internalScan(SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeInvoicePoller) {
        AnalyzeResult analyzeInvoiceResult = analyzeInvoicePoller.getFinalResult().getAnalyzeResult();

        LOGGER.debug("Document intelligence:start extracting data.." );

        Map<String,String> scanData = new HashMap<>();

        for (int i = 0; i < analyzeInvoiceResult.getDocuments().size(); i++) {
            Document analyzedInvoice = analyzeInvoiceResult.getDocuments().get(i);
            Map<String, DocumentField> invoiceFields = analyzedInvoice.getFields();

            DocumentField vendorNameField = invoiceFields.get("VendorName");
            if (vendorNameField != null) {
                if (DocumentFieldType.STRING == vendorNameField.getType()) {
                    scanData.put("VendorName", vendorNameField.getValueString());
                }
            }

            DocumentField vendorAddressField = invoiceFields.get("VendorAddress");
            if (vendorAddressField != null) {
                  scanData.put("VendorAddress", vendorAddressField.getContent());
            }

            DocumentField customerNameField = invoiceFields.get("CustomerName");
            if (customerNameField != null) {
                    scanData.put("CustomerName", customerNameField.getValueString());
            }

            DocumentField customerAddressRecipientField = invoiceFields.get("CustomerAddressRecipient");
            if (customerAddressRecipientField != null) {
                    scanData.put("CustomerAddressRecipient", customerAddressRecipientField.getValueString());
            }

            DocumentField invoiceIdField = invoiceFields.get("InvoiceId");
            if (invoiceIdField != null) {
                    scanData.put("InvoiceId", invoiceIdField.getValueString());
            }

            DocumentField invoiceDateField = invoiceFields.get("InvoiceDate");
            if (invoiceDateField != null) {
                    scanData.put("InvoiceDate", invoiceDateField.getValueDate().toString());

            }

            DocumentField invoiceTotalField = invoiceFields.get("InvoiceTotal");
            if (invoiceTotalField != null) {
                    scanData.put("InvoiceTotal", invoiceTotalField.getContent());

            }



        }
        return scanData;
    }


}