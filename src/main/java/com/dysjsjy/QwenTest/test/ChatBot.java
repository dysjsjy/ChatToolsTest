package com.dysjsjy.QwenTest.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatBot {
    private String apiUrl;
    private String chatAPIUrl;
    private String apiKey;
    private List<ChatMessage> conversationHistory;
    private Gson gson;

    {
        SystemInit systemInit = new SystemInit();
        this.apiUrl = systemInit.getApiUrl();
        this.apiKey = systemInit.getApiKey();
        this.chatAPIUrl = systemInit.getChatAPIUrl();
        this.apiUrl = systemInit.getChatAPIUrl();
        this.conversationHistory = new ArrayList<>();
        this.gson = new Gson();
    }

    public ChatBot() {
    }

    private void addUserMessageToHistory(String message) {
        conversationHistory.add(new ChatMessage("user", message));
    }

    public String sendMessage(String message, String model) throws IOException {
        addUserMessageToHistory(message);
        return sendRequest(apiUrl, model, null);
    }

    public String sendMessageToDeepSeek(String message, String model) throws IOException {
        addUserMessageToHistory(message);
        return processDeepSeekResponse(sendRequest(chatAPIUrl, model, apiKey));
    }

    private String sendRequest(String apiUrl, String model, String apiKey) throws IOException {
        // 构造请求对象
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.add("messages", gson.toJsonTree(conversationHistory));
        payload.addProperty("stream", false);

        // 创建HTTP连接
        URL url = new URL(apiUrl + (apiKey == null ? "/api/chat" : "/chat/completions"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        if (apiKey != null) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(payload).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 获得响应
        return readResponse(conn);
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        // 检查服务器响应头的字符编码
        String contentType = conn.getContentType();
        String charset = "UTF-8";
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    charset = param.split("=", 2)[1];
                    break;
                }
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), charset)
        )) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("不支持的字符编码: " + charset);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("读取响应时出错: " + e.getMessage());
            e.printStackTrace();
        }

        conn.disconnect();
        return response.toString();
    }

    private String processResponse(String response) {
        // 使用Gson解析响应
        JsonObject responseJson = gson.fromJson(response, JsonObject.class);
        String assistantMessage = responseJson.getAsJsonObject("message")
                .get("content")
                .getAsString();

        conversationHistory.add(new ChatMessage("assistant", assistantMessage));
        return assistantMessage;
    }

    private String processDeepSeekResponse(String response) {
        // 使用Gson解析响应
        JsonObject responseJson = gson.fromJson(response, JsonObject.class);
        // 获取 choices 数组的第一个元素
        JsonObject firstChoice = responseJson.getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject();
        // 获取 assistantMessage 对象
        String assistantMessage = firstChoice.getAsJsonObject("message")
                .get("content")
                .getAsString();

        conversationHistory.add(new ChatMessage("assistant", assistantMessage));
        return assistantMessage;
    }

    public String sendMessageToChatGPT(String input) {
        String assistantMessage = "测试中。";
        return assistantMessage;
    }

    public List<ChatMessage> getHistory() {
        return conversationHistory;
    }

//    public static void main(String[] args) {
//        ChatBot chatBot = new ChatBot();
//        String s = null;
//        try {
//            s = chatBot.sendMessageToDeepSeek("你好你是谁", "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(s);
//    }
}