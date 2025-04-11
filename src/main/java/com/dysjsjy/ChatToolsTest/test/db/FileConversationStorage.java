package com.dysjsjy.ChatToolsTest.test.db;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import com.dysjsjy.ChatToolsTest.test.ChatMessage;


public class FileConversationStorage implements ConversationHistoryStorage {
    private final Path storageDir;
    private final Gson gson;

    public FileConversationStorage(String baseDir) throws IOException {
        this.storageDir = Paths.get(baseDir, "history");
        // 配置Gson以美化输出
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
    }

    @Override
    public void saveConversation(String sessionId, List<ChatMessage> history) throws IOException {
        Path filePath = storageDir.resolve(sessionId + ".json");
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            // 使用JsonWriter实现更精细的控制
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("  "); // 使用2个空格缩进
            gson.toJson(history, List.class, jsonWriter);
        }
    }

    @Override
    public List<ChatMessage> loadConversation(String sessionId) throws IOException {
        Path filePath = storageDir.resolve(sessionId + ".json");
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            // 使用TypeToken确保正确的类型解析
            return gson.fromJson(reader, new com.google.gson.reflect.TypeToken<List<ChatMessage>>(){}.getType());
        }
    }

    @Override
    public void cleanupOldConversations(int maxToKeep) throws IOException {
        List<Path> files = getSortedHistoryFiles();

        // 删除旧文件
        for (int i = maxToKeep; i < files.size(); i++) {
            Files.deleteIfExists(files.get(i));
        }
    }

    // 获取按修改时间排序的历史文件列表(最新的在前)
    private List<Path> getSortedHistoryFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*.json")) {
            stream.forEach(files::add);
        }

        // 按修改时间排序(最新的在前)
        files.sort((p1, p2) -> {
            try {
                return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
            } catch (IOException e) {
                return 0;
            }
        });

        return files;
    }

    // 新增方法: 获取所有会话历史文件信息
    public List<String> getAvailableConversations() throws IOException {
        List<Path> files = getSortedHistoryFiles();
        List<String> result = new ArrayList<>();

        for (Path file : files) {
            String filename = file.getFileName().toString();
            String timestamp = Files.getLastModifiedTime(file).toString();
            result.add(String.format("%s (Last modified: %s)", filename, timestamp));
        }

        return result;
    }
}