package com.dysjsjy.ChatToolsTest.test;

import com.dysjsjy.ChatToolsTest.test.LLMConfig.LLMPropertiesConfigManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ChatBot {
    private final LLMPropertiesConfigManager configManager;
    private final List<ChatMessage> conversationHistory;
    private final Gson gson;
    private String currentProviderPrefix;

    public ChatBot(LLMPropertiesConfigManager configManager) {
        this.configManager = configManager;
        this.conversationHistory = new ArrayList<>();
        this.gson = new Gson();
        this.currentProviderPrefix = "siliconflow"; // 默认提供商
    }

    // 设置当前使用的LLM提供商
    public void setProvider(String providerPrefix) {
        if (configManager.getProviderNames().containsKey(providerPrefix)) {
            this.currentProviderPrefix = providerPrefix;
        } else {
            throw new IllegalArgumentException("Unknown provider: " + providerPrefix);
        }
    }

    // 获取当前可用的提供商列表
    public Map<String, String> getAvailableProviders() {
        return configManager.getProviderNames();
    }

    private void addUserMessageToHistory(String message) {
        conversationHistory.add(new ChatMessage("user", message));
    }

    public String sendMessage(String message, String model) throws IOException {
        addUserMessageToHistory(message);

        // 从配置管理器获取当前提供商的配置
        String apiUrl = configManager.getApiUrl(currentProviderPrefix);
        String apiKey = configManager.getApiKey(currentProviderPrefix);
        Map<String, String> additionalParams = configManager.getAdditionalParams(currentProviderPrefix);

        // 构造请求对象
        JsonObject payload = createPayload(model, additionalParams);

        // 构建真正的apiUrl
        switch (currentProviderPrefix) {
            case "ollama":
                break;
            case "siliconflow":
                apiUrl += "/chat/completions";
                break;
            default: // OpenAI 和其他兼容API
        }

        // 发送请求并处理响应
        String response = sendRequest(apiUrl, apiKey, payload);
        return processResponse(response, currentProviderPrefix);
    }

    private JsonObject createPayload(String model, Map<String, String> additionalParams) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.add("messages", gson.toJsonTree(conversationHistory));
        payload.addProperty("stream", false);

        // todo 对额外参数部分优化
//        for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
//            try {
//                // 尝试解析为数字
//                payload.addProperty(entry.getKey(), Double.parseDouble(entry.getValue()));
//            } catch (NumberFormatException e) {
//                // 不是数字，作为字符串添加
//                payload.addProperty(entry.getKey(), entry.getValue());
//            }
//        }

        return payload;
    }

    private String sendRequest(String apiUrl, String apiKey, JsonObject payload) throws IOException {
        // 创建HTTP连接
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        // 设置请求头
        if (apiKey != null && !apiKey.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(payload).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 获取响应
        return readResponse(conn);
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } finally {
            conn.disconnect();
        }
        return response.toString();
    }

    private String processResponse(String response, String providerPrefix) {
        try {
            JsonObject responseJson = gson.fromJson(response, JsonObject.class);
            String assistantMessage;

            // 根据不同提供商处理响应
            switch (providerPrefix) {
                case "ollama":
                    assistantMessage = responseJson.getAsJsonArray("content")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    break;
                case "siliconflow":
                    assistantMessage = responseJson.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();
                    break;
                default: // OpenAI 和其他兼容API
                    assistantMessage = responseJson.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();
            }

            conversationHistory.add(new ChatMessage("assistant", assistantMessage));
            return assistantMessage;
        } catch (JsonSyntaxException | NullPointerException e) {
            throw new RuntimeException("Failed to parse response from " + providerPrefix + ": " + response, e);
        }
    }

    public void clearConversationHistory() {
        conversationHistory.clear();
    }

    public List<ChatMessage> getConversationHistory() {
        return Collections.unmodifiableList(conversationHistory);
    }
}