package com.microsoft.openai.samples.assistant.agent;



import org.json.JSONObject;

public class IntentResponse {

    private IntentType intentType;

    private String message;



    public IntentResponse(IntentType intentType, String message) {
        this.intentType = intentType;
        this.message = message;
    }

    public IntentType getIntentType() {
        return intentType;
    }

    public String getMessage() {
        return this.message;
    }
}
