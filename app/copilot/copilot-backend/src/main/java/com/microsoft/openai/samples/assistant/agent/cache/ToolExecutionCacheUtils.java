package com.microsoft.openai.samples.assistant.agent.cache;

import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ToolExecutionCacheUtils {

    public static List<ToolParameter> convert(KernelFunctionArguments kernelFunctionArguments) {
        return kernelFunctionArguments.entrySet().stream().map(entry -> new ToolParameter(entry.getKey(), entry.getValue().getValue().toString())).toList();
    }

    public static String printWithToolNameAndParametersAndValues(Set<Map.Entry<ToolExecutionCacheKey,Object>> cacheEntry){
        StringBuilder sb = new StringBuilder();
        //toolname(param1:value1,param2:value2):resultString
        for (Map.Entry<ToolExecutionCacheKey,Object> entry : cacheEntry) {
            sb.append(entry.getKey().toolName()).append("(");
            //paramName:paramaValue
            for (int i = 0; i < entry.getKey().parameters().size(); i++) {
                ToolParameter parameter = entry.getKey().parameters().get(i);
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(parameter.name()).append(":").append(parameter.value());
            }
            sb.append(")")
                .append(":")
                .append(entry.getValue().toString())
                .append("\n");
        }
        return sb.toString();
    }

    public static String printWithToolNameAndValues(Set<Map.Entry<ToolExecutionCacheKey,Object>> cacheEntry){
        StringBuilder sb = new StringBuilder();
        //toolname(param1:value1,param2:value2):resultString
        for (Map.Entry<ToolExecutionCacheKey,Object> entry : cacheEntry) {
            sb.append(entry.getKey().toolName())
            .append(":")
            .append(entry.getValue().toString())
            .append("\n");
        }
        return sb.toString();
    }


}
