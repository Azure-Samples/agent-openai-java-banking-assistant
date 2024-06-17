package com.microsoft.openai.samples.assistant.agent.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ToolsExecutionCache<T>  {

    public void put(ToolExecutionCacheKey key, T value );
    public T get(ToolExecutionCacheKey chatKey, List<ToolParameter> parameters);

    public List<T> values();

    public Set<Map.Entry<ToolExecutionCacheKey, T>> entries();

    public void flush();




}
