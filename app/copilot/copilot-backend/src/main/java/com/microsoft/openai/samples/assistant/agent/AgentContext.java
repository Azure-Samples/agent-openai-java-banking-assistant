// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import java.util.HashMap;

public class AgentContext extends HashMap<String,Object>{

    private String result;

    public AgentContext() {
        super();
    }

    public AgentContext(String result) {
        super();
        this.put("result", result);
     }

    public String getResult() {
        return (String)this.get("result");
    }
    public void setResult(String result) {
        this.put("result", result);
    }

}
