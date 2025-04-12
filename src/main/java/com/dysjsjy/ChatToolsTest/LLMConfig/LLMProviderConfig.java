package com.dysjsjy.ChatToolsTest.LLMConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LLMProviderConfig {
    private String providerName;
    private String apiUrl;
    private String apiKey;
    private Map<String, String> additionalParams;

    // 构造方法
    public LLMProviderConfig(String providerName, String apiUrl, String apiKey) {
        this.providerName = providerName;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.additionalParams = new HashMap<>();
    }

    // 添加额外参数
    public void addAdditionalParam(String key, String value) {
        this.additionalParams.put(key, value);
    }

    // Getter方法
    public String getProviderName() {
        return providerName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Map<String, String> getAdditionalParams() {
        return Collections.unmodifiableMap(additionalParams);
    }

    @Override
    public String toString() {
        return "LLMProviderConfig{" +
                "providerName='" + providerName + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", apiKey='" + (apiKey != null ? "***" : "null") + '\'' +
                ", additionalParams=" + additionalParams +
                '}';
    }
}