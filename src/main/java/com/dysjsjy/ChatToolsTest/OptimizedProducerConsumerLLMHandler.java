package com.dysjsjy.ChatToolsTest;

import java.util.Collections;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;


// todo 消息队列异步处理
public class OptimizedProducerConsumerLLMHandler {
    // 消息队列，用于存储待处理的消息
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(1000);

    // 线程池，用于管理生产者和消费者线程
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 内存缓存，存储API调用结果
    private final List<String> memoryCache = Collections.synchronizedList(new ArrayList<>());

    // 内存缓存阈值，当达到此值时触发磁盘存储
    private static final int MEMORY_THRESHOLD = 100;

    // 磁盘存储路径（模拟）
    private static final String DISK_STORAGE_PATH = "disk_storage.txt";

    public OptimizedProducerConsumerLLMHandler() {
        // 启动多个消费者线程
        for (int i = 0; i < 4; i++) {
            executorService.submit(this::consumerTask);
        }

        // 启动内存监控线程，定期检查缓存是否超过阈值
        executorService.submit(this::memoryMonitor);
    }

    // 生产者：将消息放入队列
    public void produceMessage(String message) {
        try {
            messageQueue.put(message);
            System.out.println("Produced message: " + message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Producer interrupted: " + e.getMessage());
        }
    }

    // 消费者：从队列中取出消息并处理
    private void consumerTask() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String message = messageQueue.take();
                processMessageAsync(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Consumer interrupted: " + e.getMessage());
                break;
            }
        }
    }

    // 异步处理消息
    private void processMessageAsync(String message) {
        // 优先调用LLM API
        CompletableFuture<String> apiFuture = CompletableFuture.supplyAsync(() -> callLLMApi(message), executorService)
                .exceptionally(throwable -> {
                    System.err.println("API call failed for message " + message + ": " + throwable.getMessage());
                    return "Error: " + throwable.getMessage();
                });

        // API调用完成后，缓存到内存
        apiFuture.thenAccept(result -> {
            synchronized (memoryCache) {
                memoryCache.add(result);
                System.out.println("Cached to memory: " + result);
            }
        });
    }

    // 内存监控线程，检查缓存是否超过阈值
    private void memoryMonitor() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (memoryCache) {
                if (memoryCache.size() >= MEMORY_THRESHOLD) {
                    storeToDiskAndCleanup();
                }
            }
            try {
                Thread.sleep(1000); // 每秒检查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // 将80%的缓存数据存储到磁盘，并清理这些缓存
    private void storeToDiskAndCleanup() {
        synchronized (memoryCache) {
            int itemsToStore = (int) (memoryCache.size() * 0.8); // 80%的缓存
            if (itemsToStore > 0) {
                List<String> toStore = new ArrayList<>(memoryCache.subList(0, itemsToStore));
                CompletableFuture.runAsync(() -> {
                    storeToDisk(toStore);
                    synchronized (memoryCache) {
                        memoryCache.subList(0, itemsToStore).clear();
                        System.out.println("Stored " + itemsToStore + " items to disk and cleaned up memory.");
                    }
                }, executorService);
            }
        }
    }

    // 模拟调用LLM API
    private String callLLMApi(String message) {
        System.out.println("Calling LLM API with: " + message);
        try {
            Thread.sleep(1000); // 模拟API调用耗时
            return "API Response for: " + message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: API call interrupted";
        }
    }

    // 模拟存储到磁盘
    private void storeToDisk(List<String> messages) {
        System.out.println("Storing to disk: " + messages.size() + " items");
        try {
            Thread.sleep(2000); // 模拟磁盘I/O耗时
            // 这里可以实际写入文件，例如使用FileWriter
            System.out.println("Disk storage completed for: " + messages);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 关闭系统
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        OptimizedProducerConsumerLLMHandler handler = new OptimizedProducerConsumerLLMHandler();

        // 模拟生产者发送消息
        for (int i = 1; i <= 20; i++) {
            final int messageId = i;
            new Thread(() -> handler.produceMessage("Message " + messageId)).start();
            Thread.sleep(200); // 模拟生产者之间的间隔
        }

        // 等待一段时间后关闭
        Thread.sleep(10000);
        handler.shutdown();
    }
}
