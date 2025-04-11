package com.dysjsjy.ChatToolsTest.test.db;

import com.dysjsjy.ChatToolsTest.test.ChatMessage;

import java.io.IOException;
import java.util.List;

public interface ConversationHistoryStorage {
    void saveConversation(String sessionId, List<ChatMessage> history) throws IOException;
    List<ChatMessage> loadConversation(String sessionId) throws IOException;
    void cleanupOldConversations(int maxToKeep) throws IOException;
}