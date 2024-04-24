package com.microsoft.openai.samples.assistant.agent;
public class AgentContext {

    private String result;


    public void AgentContext() {
    }

    public void AgentContext(String result) {
          this.result = result;
     }

    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }

}
