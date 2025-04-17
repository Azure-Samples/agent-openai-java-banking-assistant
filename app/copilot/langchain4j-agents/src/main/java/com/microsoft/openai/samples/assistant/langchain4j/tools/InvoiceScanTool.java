package com.microsoft.openai.samples.assistant.langchain4j.tools;


import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class InvoiceScanTool  {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceScanTool.class);
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;
    public InvoiceScanTool(DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper) {
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }
    @Tool( "Extract the invoice or bill data scanning a photo or image")
    public String scanInvoice(
            @P("the path to the file containing the image or photo") String filePath) {

        Map<String,String> scanData = null;

        try{
            scanData = documentIntelligenceInvoiceScanHelper.scan(filePath);
        } catch (Exception e) {
          LOGGER.warn("Error extracting data from invoice {}:", filePath,e);
          scanData = new HashMap<>();
        }

        LOGGER.info("scanInvoice tool: Data extracted {}:{}", filePath,scanData);
        return scanData.toString();

    }



}

