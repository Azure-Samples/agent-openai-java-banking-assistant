# Useful Kusto queries to inspect tools calls
By default all Azure Open AI requests and response are logged in the java application logs.
The following Kusto queries can be used to inspect the tools calls requests and responses.


### Inspect all Azure Open AI responses
```kusto
traces
| where cloud_RoleName == "copilot-api"
| where message contains "openai.azure.com/openai/deployments"
| extend chatmessage=parse_json(message)
| where chatmessage["az.sdk.message"] == "HTTP response"
| extend chatbody=parse_json(tostring(chatmessage.body)).choices[0]
```
### Inspect all Azure Open AI requests
```kusto
traces 
| where cloud_RoleName == "copilot-api"
| where message contains "openai.azure.com/openai/deployments"
| extend chatrequest=parse_json(message)
| where chatrequest.method == "POST" 
| extend chatbody=parse_json(tostring(parse_json(message).body))
```

### Inspect all tools calls requests
```kusto
traces
| where cloud_RoleName == "copilot-api"
| where message contains "openai.azure.com/openai/deployments"
| extend chatrequest=parse_json(message)
| where chatrequest["az.sdk.message"] == "HTTP response"
| extend response=parse_json(tostring(parse_json(tostring(chatrequest.body)))).choices[0]
| where response.finish_reason=="tool_calls"
| extend tool_calls=parse_json(tostring(parse_json(tostring(response.message)).tool_calls))
```

### Inspect all tools calls requests for TransactionHistoryPlugin function
```kusto
traces 
| where cloud_RoleName == "copilot-api"
| where message contains "openai.azure.com/openai/deployments"
| extend chatrequest=parse_json(message)
| where chatrequest["az.sdk.message"] == "HTTP response" 
| extend response=parse_json(tostring(parse_json(tostring(chatrequest.body)))).choices[0]
| where response.finish_reason=="tool_calls" //and response.message contains "TransactionHistoryPlugin"
| extend tool_calls=parse_json(tostring(parse_json(tostring(response.message)).tool_calls))
```