//package com.yezibin.learnclaudecode.compression;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import okhttp3.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//public class CompressionHandler {
//
//    // 最近三轮消息保存数量
//    private static final int RECENT_ROUNDS = 3;
//
//    // SiliconFlow API 地址
//    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
//    // 替换成你自己的 API Key
//    private static final String API_KEY = "sk-qqvwbybttiknbxvsxqujxrhmvlvdsaeqcmqtxetdiguimzym";
//    // 模型名称
//    private static final String MODEL = "Pro/MiniMaxAI/MiniMax-M2.5";
//
//    // HTTP客户端
//    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .connectTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(60, TimeUnit.SECONDS)
//            .writeTimeout(60, TimeUnit.SECONDS)
//            .build();
//
//    /**
//     * 压缩消息列表
//     * @param messages 原始消息列表
//     * @return 压缩后的消息列表
//     */
//    public static JsonArray compressMessages(JsonArray messages) {
//        JsonArray compressedMessages = new JsonArray();
//
//        if (messages.size() <= RECENT_ROUNDS) {
//            // 如果消息数量不足，直接返回原消息
//            return messages;
//        }
//
//        // 提取需要摘要的消息（前三轮之前的）
//        List<JsonObject> messagesToSummarize = new ArrayList<>();
//        for (int i = 0; i < messages.size() - RECENT_ROUNDS; i++) {
//            messagesToSummarize.add(messages.get(i).getAsJsonObject());
//        }
//
//        // 为需要摘要的消息生成摘要
//        if (!messagesToSummarize.isEmpty()) {
//            JsonObject summaryMessage = generateSummary(messagesToSummarize);
//            // 将摘要消息添加到压缩消息列表的开头
//            compressedMessages.add(summaryMessage);
//        }
//
//        // 提取最近三轮的消息（全量保存）
//        for (int i = messages.size() - RECENT_ROUNDS; i < messages.size(); i++) {
//            compressedMessages.add(messages.get(i));
//        }
//
//        return compressedMessages;
//    }
//
//    /**
//     * 生成消息摘要
//     * @param messages 需要摘要的消息列表
//     * @return 摘要消息对象
//     */
//    private static JsonObject generateSummary(List<JsonObject> messages) {
//        JsonObject summaryMessage = new JsonObject();
//        summaryMessage.addProperty("role", "assistant");
//
//        String summaryContent = getModelSummary(messages);
//        summaryMessage.addProperty("content", "[消息摘要] " + summaryContent);
//        return summaryMessage;
//    }
//
//    /**
//     * 使用大模型生成消息摘要
//     * @param messages 需要摘要的消息列表
//     * @return 摘要内容
//     */
//    private static String getModelSummary(List<JsonObject> messages) {
//        JsonArray summaryMessages = new JsonArray();
//
//        // 构建系统提示
//        JsonObject systemMessage = new JsonObject();
//        systemMessage.addProperty("role", "system");
//        systemMessage.addProperty("content", "你是一个对话摘要助手，需要对以下对话进行简洁、准确的摘要。");
//        summaryMessages.add(systemMessage);
//
//        // 添加需要摘要的消息
//        for (JsonObject message : messages) {
//            summaryMessages.add(message);
//        }
//
//        // 构建请求体
//        JsonObject requestBody = new JsonObject();
//        requestBody.addProperty("model", MODEL);
//        requestBody.addProperty("temperature", 0);
//        requestBody.addProperty("max_tokens", 500);
//        requestBody.addProperty("stream", false);
//        requestBody.addProperty("enable_thinking", false);
//        requestBody.add("messages", summaryMessages);
//
//        // 发送请求
//        Request request = new Request.Builder()
//                .url(API_URL)
//                .addHeader("Authorization", "Bearer " + API_KEY)
//                .addHeader("Content-Type", "application/json")
//                .post(RequestBody.create(
//                        requestBody.toString(),
//                        MediaType.parse("application/json")
//                ))
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                System.out.println("摘要生成失败，状态码：" + response.code());
//                throw new RuntimeException("摘要生成失败，状态码：" + response.code());
//            }
//
//            // 解析响应
//            String responseBody = response.body().string();
//            Gson gson = new Gson();
//            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
//
//            // 提取摘要内容
//            String summary = jsonResponse
//                    .getAsJsonArray("choices")
//                    .get(0).getAsJsonObject()
//                    .getAsJsonObject("message")
//                    .get("content").getAsString();
//
//            return summary;
//        } catch (IOException e) {
//            System.out.println("摘要生成异常：" + e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 检查是否需要压缩消息
//     * @param messages 消息列表
//     * @return 是否需要压缩
//     */
//    public static boolean needsCompression(JsonArray messages) {
//        return messages.size() > RECENT_ROUNDS;
//    }
//}