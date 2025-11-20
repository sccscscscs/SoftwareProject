package com.myapp;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 AI 对话服务（智谱 GLM 接口）
 *
 * 使用方式：
 * 1. 注册：https://open.bigmodel.cn/
 * 2. 控制台创建并复制 API Key
 * 3. 设置环境变量：ZHIPU_AI_KEY=你的key
 *    或在代码里通过 new AIService("你的key") 传入
 */
public class AIService {

    /** 默认接口地址（不需要动） */
    private static final String DEFAULT_API_URL =
            "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /** 默认模型：glm-4-flash（免费 / 超便宜，适合作为默认） */
    private static final String DEFAULT_MODEL = "glm-4-flash";

    /** 默认从这个环境变量里读 key */
    private static final String ENV_API_KEY = "ZHIPU_AI_KEY";

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final Gson gson = new Gson();

    // ===== 构造函数区域 =====

    /**
     * 使用默认配置：
     * - API Key：从环境变量 ZHIPU_AI_KEY 读取
     * - 地址：DEFAULT_API_URL
     * - 模型：glm-4-flash
     */
    public AIService() {
        this(System.getenv(ENV_API_KEY));
    }

    /**
     * 只指定 API Key，其他用默认。
     */
    public AIService(String apiKey) {
        this(apiKey, DEFAULT_API_URL, DEFAULT_MODEL, 10_000, 30_000);
    }

    /**
     * 完全自定义。
     */
    public AIService(String apiKey,
                     String apiUrl,
                     String model,
                     int connectTimeoutMs,
                     int readTimeoutMs) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    // ===== 对外主要方法 =====

    /**
     * 最简单的一轮对话：只发一条 user 消息。
     */
    public String chat(String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", userMessage));
        return chat(messages);
    }

    /**
     * 支持多轮对话：传入整个 messages 历史。
     * messages 里元素格式：new Message("user"/"assistant"/"system", "内容")
     */
    public String chat(List<Message> messages) {
        // 1. 检查 API Key
        if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
            return buildNoApiKeyMessage();
        }

        try {
            // 2. 构建请求体
            ChatRequest request = new ChatRequest();
            request.model = this.model;
            request.messages = messages;
            request.temperature = 0.7; // 你想更稳一点可以改小
            request.maxTokens = null;  // 用服务端默认，也可以改数值

            String requestJson = gson.toJson(request);

            // 3. 创建连接并发送请求
            HttpURLConnection connection = createConnection();
            sendRequest(connection, requestJson);

            // 4. 读取响应
            int statusCode = connection.getResponseCode();
            String rawResponse = readResponse(connection, statusCode);

            // 打印日志方便调试（生产环境可以换成 logger）
            System.out.println("HTTP Status: " + statusCode);
            System.out.println("Raw Response: " + rawResponse);

            // 5. 按状态码处理
            if (statusCode != HttpURLConnection.HTTP_OK) {
                // 尝试解析为错误结构
                ChatErrorResponse err = safeParseError(rawResponse);
                if (err != null && err.error != null) {
                    return "AI服务返回错误：[" + err.error.code + "] "
                            + err.error.message;
                }
                return "AI服务调用失败，HTTP状态码：" + statusCode + "，响应内容：" + rawResponse;
            }

            // 6. 解析正常返回
            ChatResponse chatResponse = gson.fromJson(rawResponse, ChatResponse.class);
            if (chatResponse == null) {
                return "AI服务返回为空，请稍后重试。";
            }

            if (chatResponse.error != null) {
                return "AI服务返回错误：[" + chatResponse.error.code + "] "
                        + chatResponse.error.message;
            }

            if (chatResponse.choices == null || chatResponse.choices.isEmpty()) {
                return "AI服务未返回任何内容。";
            }

            ChatChoice firstChoice = chatResponse.choices.get(0);
            if (firstChoice.message == null || firstChoice.message.content == null) {
                return "AI服务返回格式异常：未找到 message.content 字段。";
            }

            return firstChoice.message.content;

        } catch (Exception e) {
            // 网络异常 / JSON 解析异常等
            return "AI服务调用失败：" + e.getMessage() + "\n\n"
                    + "可能的原因：\n"
                    + "1. 网络连接问题（防火墙 / 代理 / GFW 等）\n"
                    + "2. API Key 配置错误或已过期\n"
                    + "3. 调用频率或额度超限\n\n"
                    + "请检查网络连接和 API 配置。";
        }
    }

    // ===== HTTP & IO 相关私有方法 =====

    private HttpURLConnection createConnection() throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        return connection;
    }

    private void sendRequest(HttpURLConnection connection, String body) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input);
            os.flush();
        }
    }

    private String readResponse(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream = statusCode == HttpURLConnection.HTTP_OK
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (inputStream == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
        }
        return sb.toString();
    }

    private ChatErrorResponse safeParseError(String raw) {
        try {
            return gson.fromJson(raw, ChatErrorResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildNoApiKeyMessage() {
        return "AI功能需要配置 API Key。\n\n"
                + "1. 访问 https://open.bigmodel.cn/ 注册并获取免费 API Key\n"
                + "2. 将 API Key 设置到环境变量：ZHIPU_AI_KEY\n"
                + "   或使用 new AIService(\"你的Key\") 传入\n\n"
                + "【模拟回复】目前尚未配置真实 API Key。\n";
    }

    // ===== 内部数据结构（与 JSON 对应）=====

    /**
     * 对话消息结构：role + content
     */
    public static class Message {
        public String role;
        public String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 请求结构体
     */
    private static class ChatRequest {
        public String model;
        public List<Message> messages;

        public Double temperature;

        @SerializedName("max_tokens")
        public Integer maxTokens;
    }

    /**
     * 正常返回的结构体（只保留我们需要的字段）
     */
    private static class ChatResponse {
        public String id;
        public String object;
        public long created;
        public String model;
        public List<ChatChoice> choices;
        public Usage usage;
        public ChatError error; // 有些实现会把错误放在这里（兼容一下）
    }

    private static class ChatChoice {
        public int index;
        public Message message;
        @SerializedName("finish_reason")
        public String finishReason;
    }

    private static class Usage {
        @SerializedName("prompt_tokens")
        public int promptTokens;
        @SerializedName("completion_tokens")
        public int completionTokens;
        @SerializedName("total_tokens")
        public int totalTokens;
    }

    /**
     * 错误返回结构
     */
    private static class ChatErrorResponse {
        public ChatError error;
    }

    private static class ChatError {
        public String code;
        public String message;
    }
}


