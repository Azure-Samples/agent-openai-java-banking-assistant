package com.microsoft.openai.samples.assistant.agent.cache;

import java.util.*;

public class InMemoryToolsExecutionCache<T> implements ToolsExecutionCache<T> {

    private final Map<ToolExecutionCacheKey, T> cache = new HashMap<>();

    @Override
    public void put(ToolExecutionCacheKey key, T value) {
        //String key = buildKey(chatKey,toolName, parameters);
        cache.put(key, value);
    }

    @Override
    public T get(ToolExecutionCacheKey key,  List<ToolParameter> parameters) {
       // String key = buildKey(chatKey,toolName, parameters);
        return cache.get(key);
    }

    @Override
    public List<T> values() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public Set<Map.Entry<ToolExecutionCacheKey, T>> entries() {
        return cache.entrySet();
    }

    @Override
    public void flush() {
        cache.clear();
    }

    private String buildKey(ToolExecutionCacheKey chatKey, String toolName, List<ToolParameter> parameters) {

      if(chatKey == null || (chatKey.userId() == null && chatKey.threadId() == null))
        throw new IllegalArgumentException("ChatKey is null or empty");

       // key format: userId.conversationId.toolName(param1:value1,param2:value2)
       StringBuilder key = new StringBuilder(toolName).append("(");
       key.append(chatKey.userId()).append(".").append(chatKey.threadId()).append(".");
        for (int i = 0; i < parameters.size(); i++) {
            ToolParameter parameter = parameters.get(i);
            if (i != 0) {
                key.append(",");
            }
            key.append(parameter.name()).append(":").append(parameter.value());
        }
        key.append(")");
        return key.toString();
    }
}