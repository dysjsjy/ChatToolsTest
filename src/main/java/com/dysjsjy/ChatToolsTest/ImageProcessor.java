package com.dysjsjy.ChatToolsTest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;


// todo 图片处理还在测试中
public class ImageProcessor {
    private String apiUrl;
    private String apiKey;

    public ImageProcessor(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl + "/v1/images";
        this.apiKey = apiKey;
    }

    public String uploadAndDescribeImage(File imageFile) throws IOException {
        String base64Image = encodeImageToBase64(imageFile);
        String jsonPayload = "{\"image\": \"" + base64Image + "\", \"prompt\": \"请描述这张图片\"}";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        conn.disconnect();
        return parseImageResponse(response.toString());
    }

    private String encodeImageToBase64(File imageFile) throws IOException {
        byte[] fileContent = new byte[(int) imageFile.length()];
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            fis.read(fileContent);
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private String parseImageResponse(String response) {
        // 简单解析JSON响应，实际需要使用JSON库
        return response.contains("description") ? response.split("\"description\":")[1].split("\"")[1] : "无法生成描述";
    }
}
