package com.dysjsjy.ChatToolsTest.test;

import java.io.IOException;
import java.util.Scanner;
import java.io.*;

public class Main {

    private SystemInit systemInit;
    private Scanner scanner;

    public Main() {
        systemInit = new SystemInit();
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    private void start() {
        System.out.println("欢迎使用dysjsjy的小工具");
        System.out.println("++++++++++++++++++++");
        System.out.println("正在初始化参数...");

        while (true) {
            System.out.println("\n请选择功能：");
            System.out.println("1. 使用本地Ollama聊天");
            System.out.println("2. 使用ChatAPI聊天");
            System.out.println("3. 上传图片并获取描述");
            System.out.println("4. 退出");
            System.out.print("输入选项 (1-4): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    useLocalOllama();
                    break;
                case "2":
                    useChatAPI();
                    break;
                case "3":
                    processImage();
                    break;
                case "4":
                    System.out.println("再见！");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选项，请重新输入！");
            }
        }
    }

    private void startChatService(String serviceName, String model, ChatFunction chatFunction) {
        System.out.println("欢迎使用" + serviceName + "！输入 'quit' 退出");
        while (true) {
            System.out.print("\n你: ");
            String input = scanner.nextLine();
            if ("quit".equalsIgnoreCase(input)) {
                System.out.println("再见！");
                break;
            }
            try {
                String response = chatFunction.apply(input, model);
                System.out.println(model + ": " + response);
            } catch (IOException e) {
                throw new RuntimeException(serviceName + "服务出现问题了。");
            }
        }
        System.out.println(serviceName + "服务正常退出。");
    }

    @FunctionalInterface
    interface ChatFunction {
        String apply(String input, String model) throws IOException;
    }

    private void useLocalOllama() {
        ChatBot chatBot = new ChatBot();
        String model = "qwen-2.5-1.5B-Instruct";
        startChatService("本地Ollama聊天服务", model, (input, m) -> chatBot.sendMessage(input, m));
    }

    private void useChatAPI() {
        ChatBot chatBot = new ChatBot();
        String model = "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B";
        startChatService("ChatAPI", model, (input, m) -> chatBot.sendMessageToDeepSeek(input, m));
    }

    private void processImage() {
        System.out.print("请输入图片文件路径: ");
        String imagePath = scanner.nextLine();

        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.out.println("错误: 图片文件不存在！");
                return;
            }

            ImageProcessor imageProcessor = new ImageProcessor(systemInit.getChatAPIUrl(), systemInit.getApiKey());
            String description = imageProcessor.uploadAndDescribeImage(imageFile);
            System.out.println("图片描述: " + description);
        } catch (IOException e) {
            System.out.println("错误: 图片处理失败 - " + e.getMessage());
        }
    }
}