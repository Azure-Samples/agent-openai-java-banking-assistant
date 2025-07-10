package com.microsoft.openai.samples.assistant.config.agent;

import com.google.adk.tools.mcp.McpTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;

import java.util.List;

public class ADKUtils {

    public static List<McpTool> buildMCPTools(String mcpServerUrl) {
        SseServerParameters sseParams = SseServerParameters.builder()
                .url(mcpServerUrl)
                .build();

        McpToolset.McpToolsAndToolsetResult toolsAndToolsetResult;
        try {
            toolsAndToolsetResult = McpToolset.fromServer(sseParams).get();
        } catch (Exception e) {
            throw new IllegalArgumentException( "Error while trying to setup MCP tools for server" + mcpServerUrl, e);
        }

        return toolsAndToolsetResult.getTools();
    }
}
