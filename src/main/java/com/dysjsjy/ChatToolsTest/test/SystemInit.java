package com.dysjsjy.ChatToolsTest.test;

import java.io.FileReader;
import java.util.Properties;


class SystemInit {
    private String filePath = "data/db/config.properties";
    private String apiUrl;
    private String apiKey;
    private String ollamaApiUrl;
    private String chatAPIUrl;

    public SystemInit() {
        Properties pp = new Properties();
        try {
            pp.load(new FileReader(filePath));
        } catch (Exception e) {
            throw new RuntimeException("Properties加载失败。");
        }
//        pp.list(System.out);
        apiUrl = pp.getProperty("apiUrl");
        apiKey = pp.getProperty("apiKey");
        ollamaApiUrl = pp.getProperty("ollamaApiUrl");
        chatAPIUrl = pp.getProperty("chatAPIUrl");
    }

    public String getFilePath() {
        return filePath;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getOllamaApiUrl() {
        return ollamaApiUrl;
    }

    public String getChatAPIUrl() {
        return chatAPIUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public static void main(String[] args) {
        SystemInit systemInit = new SystemInit();
    }
}