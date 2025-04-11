package com.dysjsjy.ChatToolsTest.test.LLMConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


public class LLMPropertiesConfigManager {
    private static final String PROPERTIES_FILE = "data/db/config.properties";
    private static LLMPropertiesConfigManager instance;
    private final Properties properties;

    private LLMPropertiesConfigManager() {
        properties = new Properties();
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configuration file", ex);
        }
    }

    public static synchronized LLMPropertiesConfigManager getInstance() {
        if (instance == null) {
            instance = new LLMPropertiesConfigManager();
        }
        return instance;
    }

    // 获取所有提供商前缀列表 (如 openai, anthropic, google)
    public List<String> getProviderPrefixes() {
        return properties.stringPropertyNames().stream()
                .map(key -> key.split("\\.")[0])
                .distinct()
                .collect(Collectors.toList());
    }

    // 获取指定提供商的所有配置
    public Map<String, String> getProviderConfig(String providerPrefix) {
        return properties.stringPropertyNames().stream()
                .filter(key -> key.startsWith(providerPrefix + "."))
                .collect(Collectors.toMap(
                        key -> key.substring(key.indexOf('.') + 1),
                        properties::getProperty
                ));
    }

    // 获取所有提供商的友好名称映射
    public Map<String, String> getProviderNames() {
        return getProviderPrefixes().stream()
                .collect(Collectors.toMap(
                        prefix -> prefix,
                        prefix -> properties.getProperty(prefix + ".name", prefix)
                ));
    }

    // 获取指定配置项
    public String getConfigValue(String providerPrefix, String configKey) {
        return properties.getProperty(providerPrefix + "." + configKey);
    }

    // 获取API URL
    public String getApiUrl(String providerPrefix) {
        return getConfigValue(providerPrefix, "api_url");
    }

    // 获取API Key
    public String getApiKey(String providerPrefix) {
        return getConfigValue(providerPrefix, "api_key");
    }

    // 获取所有额外参数 (排除name, api_url, api_key)
    public Map<String, String> getAdditionalParams(String providerPrefix) {
        Set<String> set = new HashSet<>();
        set.add("name");
        set.add("api_url");
        set.add("api_key");

        return getProviderConfig(providerPrefix).entrySet().stream()
                .filter(entry -> !set.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

//    public static void main(String[] args) {
//        LLMPropertiesConfigManager instance1 = LLMPropertiesConfigManager.getInstance();
//        Map<String, String> providerNames = instance1.getProviderNames();
//        String apiUrl = instance1.getApiUrl("siliconflow");
//        String apiKey = instance1.getApiKey("siliconflow");
//    }
}
