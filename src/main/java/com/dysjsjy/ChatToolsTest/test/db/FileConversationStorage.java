package com.dysjsjy.ChatToolsTest.test.db;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.dysjsjy.ChatToolsTest.test.ChatMessage;
import com.google.gson.*;

public class FileConversationStorage implements ConversationHistoryStorage {
    private final Path storageDir;
    private final Gson gson;

    public FileConversationStorage(String baseDir) throws IOException {
        this.storageDir = Paths.get(baseDir, "chat_history");
        this.gson = new Gson();

        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
    }

    @Override
    public void saveConversation(String sessionId, List<ChatMessage> history) throws IOException {
        Path filePath = storageDir.resolve(sessionId + ".json");
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(history, writer);
        }
    }

    @Override
    public List<ChatMessage> loadConversation(String sessionId) throws IOException {
        Path filePath = storageDir.resolve(sessionId + ".json");
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            ChatMessage[] messages = gson.fromJson(reader, ChatMessage[].class);
            return new ArrayList<>(Arrays.asList(messages));
        }
    }

    @Override
    public void cleanupOldConversations(int maxToKeep) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*.json")) {
            for (Path entry : stream) {
                files.add(entry);
            }
        }

        // 按修改时间排序，最新的在前面
        files.sort((p1, p2) -> {
            try {
                return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
            } catch (IOException e) {
                return 0;
            }
        });

        // 删除旧文件
        for (int i = maxToKeep; i < files.size(); i++) {
            Files.deleteIfExists(files.get(i));
        }
    }
}