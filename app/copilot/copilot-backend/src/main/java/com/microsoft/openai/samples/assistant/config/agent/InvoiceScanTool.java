package com.microsoft.openai.samples.assistant.config.agent;


import com.google.adk.tools.Annotations.Schema;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class InvoiceScanTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceScanTool.class);
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;

    private static DocumentIntelligenceInvoiceScanHelper staticHelper;
    public InvoiceScanTool(DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper) {
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }


    public void init() {
        //adk-java require tools to be static, so we assign the injected value to a static field
        staticHelper = this.documentIntelligenceInvoiceScanHelper; // Assigning to static field
    }

    public  Map<String,String> scanInvoice(
            @Schema(description ="the path to the file containing the image or photo") String filePath) {

        Map<String,String> scanData = null;

        try{
            scanData = this.documentIntelligenceInvoiceScanHelper.scan(filePath);
        } catch (Exception e) {
          LOGGER.warn("Error extracting data from invoice {}:", filePath,e);
          return Map.of("status","error","report","Error extracting data from invoice: " + e.getMessage());
        }

        LOGGER.info("scanInvoice tool: Data extracted {}:{}", filePath,scanData);
        return scanData;

    }



}

