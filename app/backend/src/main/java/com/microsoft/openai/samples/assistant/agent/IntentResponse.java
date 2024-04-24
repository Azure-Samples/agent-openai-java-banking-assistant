package com.microsoft.openai.samples.assistant.agent;



import org.json.JSONObject;

public class IntentResponse {

    private IntentType intentType;

    private JSONObject jsonData;



    public IntentResponse(IntentType intentType, JSONObject jsonData) {
        this.intentType = intentType;
        this.jsonData = jsonData;
    }

    public IntentType getIntentType() {
        return intentType;
    }

    public JSONObject getJsonData() {
        return jsonData;
    }
}
