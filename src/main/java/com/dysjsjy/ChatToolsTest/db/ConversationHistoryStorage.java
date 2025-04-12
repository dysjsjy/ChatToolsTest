package com.dysjsjy.ChatToolsTest.db;

import com.dysjsjy.ChatToolsTest.ChatMessage;

import java.io.IOException;
import java.util.List;

public interface ConversationHistoryStorage {
    void saveConversation(String sessionId, List<ChatMessage> history) throws IOException;
    List<ChatMessage> loadConversation(String sessionId) throws IOException;
    void cleanupOldConversations(int maxToKeep) throws IOException;
}