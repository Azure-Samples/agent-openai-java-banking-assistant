package com.microsoft.openai.samples.assistant.plugin;

import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * reference: https://learn.microsoft.com/en-us/java/api/overview/azure/ai-openai-readme?view=azure-java-preview
 *
 * {
 *     "enhancements": {
 *             "ocr": {
 *               "enabled": true
 *             },
 *             "grounding": {
 *               "enabled": true
 *             }
 *     },
 *     "data_sources": [
 *     {
 *         "type": "AzureComputerVision",
 *         "parameters": {
 *             "endpoint": "<your_computer_vision_endpoint>",
 *             "key": "<your_computer_vision_key>"
 *         }
 *     }],
 *     "messages": [
 *         {
 *             "role": "system",
 *             "content": "You are a helpful assistant."
 *         },
 *         {
 *             "role": "user",
 *             "content": [
 * 	                            {
 * 	                "type": "text",
 * 	                "text": "Describe this picture:"
 *                },
 *                {
 * 	                "type": "image_url",
 * 	                "image_url": {
 *                         "url":"<image URL>"
 *                     }
 *                 }
 *            ]
 *         }
 *     ],
 *     "max_tokens": 100,
 *     "stream": false
 * }
 */
public class InvoiceScanPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceScanPlugin.class);
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;
    public InvoiceScanPlugin(DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper) {
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }
    @DefineKernelFunction(name = "scanInvoice", description = "Extract the invoice or bill data scanning a photo or image")
    public String scanInvoice(
            @KernelFunctionParameter(name = "filePath", description = "the path to the file containing the image or photo") String filePath) {

        Map<String,String> scanData = null;

        try{
            scanData = documentIntelligenceInvoiceScanHelper.scan(filePath);
        } catch (Exception e) {
          LOGGER.warn("Error extracting data from invoice {}:", filePath,e);
          scanData = new HashMap<>();
        }

        LOGGER.info("SK scanInvoice plugin: Data extracted {}:{}", filePath,scanData);
        return scanData.toString();

    }




}

