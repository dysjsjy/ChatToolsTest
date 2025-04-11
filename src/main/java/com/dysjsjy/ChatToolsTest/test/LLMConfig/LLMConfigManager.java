package com.dysjsjy.ChatToolsTest.test.LLMConfig;

import java.util.HashMap;
import java.util.Map;

public class LLMConfigManager {
    private static LLMConfigManager instance;
    private final Map<String, LLMProviderConfig> providerConfigs;

    // 私有构造方法实现单例
    private LLMConfigManager() {
        providerConfigs = new HashMap<>();
    }

    // 获取单例实例
    public static synchronized LLMConfigManager getInstance() {
        if (instance == null) {
            instance = new LLMConfigManager();
        }
        return instance;
    }

    // 添加配置
    public void addProviderConfig(LLMProviderConfig config) {
        providerConfigs.put(config.getProviderName().toLowerCase(), config);
    }

    // 获取配置
    public LLMProviderConfig getProviderConfig(String providerName) {
        return providerConfigs.get(providerName.toLowerCase());
    }

    // 移除配置
    public void removeProviderConfig(String providerName) {
        providerConfigs.remove(providerName.toLowerCase());
    }

    // 列出所有可用的提供商
    public String[] getAvailableProviders() {
        return providerConfigs.keySet().toArray(new String[0]);
    }

    // 检查提供商是否存在
    public boolean hasProvider(String providerName) {
        return providerConfigs.containsKey(providerName.toLowerCase());
    }
}
