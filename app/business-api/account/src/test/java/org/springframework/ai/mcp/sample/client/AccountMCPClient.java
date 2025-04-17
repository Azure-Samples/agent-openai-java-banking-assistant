/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.client;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

/**
 * @author Christian Tzolov
 */

public class AccountMCPClient {



	public static void main(String[] args) {
		var transport = new HttpClientSseClientTransport("http://localhost:8070");

		var client = McpClient.sync(transport).build();

		client.initialize();

		client.ping();

		// List and demonstrate tools
		ListToolsResult toolsList = client.listTools();
		System.out.println("Available Tools = " + toolsList);
		toolsList.tools().stream().forEach(tool -> {
			System.out.println("Tool: " + tool.name() + ", description: " + tool.description() + ", schema: " + tool.inputSchema());
		});

		CallToolResult accountResult = client.callTool(new CallToolRequest("getAccountDetails",
				Map.of("accountId", "1010")));
		System.out.println("Account : " + accountResult);

		CallToolResult userResult = client.callTool(new CallToolRequest("getAccountsByUserName",
				Map.of("userName", "bob.user@contoso.com")));
		System.out.println("Account for Bob : " + userResult);

		client.closeGracefully();

	}

}
