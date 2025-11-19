package com.myapp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * AI对话服务
 * 接入免费的中国AI API（智谱AI - ChatGLM）
 * 
 * 使用说明：
 * 1. 访问 https://open.bigmodel.cn/ 注册账号
 * 2. 获取API Key
 * 3. 将API Key设置到系统环境变量 ZHIPU_AI_KEY 或直接修改 API_KEY 常量
 * 
 * 注意：本实现使用简化的HTTP请求，实际生产环境建议使用官方SDK
 */
public class AIService {
    
    // 智谱AI API配置
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static String API_KEY = System.getenv("ZHIPU_AI_KEY"); // 从环境变量读取
    
    // 如果环境变量未设置，可以直接在这里填写API Key（不推荐，仅用于测试）
    static {
        if (API_KEY == null || API_KEY.isEmpty()) {
            // 警告：请不要将API Key提交到代码仓库！
            API_KEY = "your-api-key-here";
        }
    }
    
    /**
     * 发送对话请求到AI服务
     * @param userMessage 用户输入的消息
     * @return AI的回复
     */
    public String chat(String userMessage) {
        // 如果API Key未配置，返回友好提示
        if (API_KEY == null || API_KEY.equals("your-api-key-here") || API_KEY.isEmpty()) {
            return "AI功能需要配置API Key。\n\n" +
                   "请访问 https://open.bigmodel.cn/ 注册并获取免费API Key，\n" +
                   "然后设置环境变量 ZHIPU_AI_KEY 或修改 AIService.java 中的 API_KEY。\n\n" +
                   "【模拟回复】您说：" + userMessage + "\n" +
                   "这是一个模拟回复，配置API Key后将获得真实的AI对话功能。";
        }
        
        try {
            // 构建请求JSON（使用GLM-4-Flash免费模型）
            String requestBody = buildRequestJson(userMessage);
            
            // 发送HTTP请求
            HttpURLConnection connection = createConnection();
            sendRequest(connection, requestBody);
            
            // 读取响应
            String response = readResponse(connection);
            
            // 解析响应JSON
            return parseResponse(response);
            
        } catch (Exception e) {
            return "AI服务调用失败：" + e.getMessage() + "\n\n" +
                   "可能的原因：\n" +
                   "1. 网络连接问题\n" +
                   "2. API Key配置错误\n" +
                   "3. API调用限额已用完\n\n" +
                   "请检查网络连接和API配置。";
        }
    }
    
    /**
     * 构建请求JSON
     */
    private String buildRequestJson(String userMessage) {
        // 使用GLM-4-Flash模型（免费且响应快）
        return String.format(
            "{\n" +
            "  \"model\": \"glm-4-flash\",\n" +
            "  \"messages\": [\n" +
            "    {\n" +
            "      \"role\": \"user\",\n" +
            "      \"content\": \"%s\"\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            escapeJson(userMessage)
        );
    }
    
    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createConnection() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        return connection;
    }
    
    /**
     * 发送请求
     */
    private void sendRequest(HttpURLConnection connection, String requestBody) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }
    
    /**
     * 读取响应
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode == 200) ? 
            connection.getInputStream() : connection.getErrorStream();
            
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }
    
    /**
     * 解析响应JSON
     */
    private String parseResponse(String jsonResponse) {
        // 简单解析JSON响应，提取AI回复内容
        // 实际生产环境建议使用JSON解析库如Gson或Jackson
        
        try {
            // 使用简单的字符串处理提取内容
            int startIndex = jsonResponse.indexOf("\"content\":\"");
            if (startIndex == -1) {
                return "AI服务返回格式错误：" + jsonResponse;
            }
            
            startIndex += 11; // 跳过"content":"前缀
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            
            if (endIndex == -1) {
                return "AI服务返回格式错误：" + jsonResponse;
            }
            
            String content = jsonResponse.substring(startIndex, endIndex);
            return unescapeJson(content);
            
        } catch (Exception e) {
            return "解析AI响应失败：" + e.getMessage() + "\n原始响应：" + jsonResponse;
        }
    }
    
    /**
     * 转义JSON特殊字符
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * 反转义JSON特殊字符
     */
    private String unescapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\b", "\b")
                   .replace("\\f", "\f")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
}