package com.yezibin.learnclaudecode.loop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import com.yezibin.learnclaudecode.compression.CompressionHandler;
import com.yezibin.learnclaudecode.skills.SkillLoader;
import com.yezibin.learnclaudecode.tools.ToolHandler;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SubAgent {
    // SiliconFlow API 地址
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    // 替换成你自己的 API Key
    private static final String API_KEY = "sk-qqvwbybttiknbxvsxqujxrhmvlvdsaeqcmqtxetdiguimzym";
    // 模型名称
    private static final String MODEL = "Pro/MiniMaxAI/MiniMax-M2.5";
    // 子智能体系统提示
    private static final String SUBAGENT_SYSTEM = "你是一个子智能助手，擅长解决具体的任务，并将需要的结果返回给父智能体。";
    // 技能加载器
    private static final SkillLoader skillLoader = new SkillLoader();

    // 运行子智能体
    public static String run(String prompt) throws IOException {
        // 构建子智能体的消息数组
        JsonArray subMessages = new JsonArray();
        
        // 添加用户消息
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);
        subMessages.add(userMsg);
        
        // 创建 OkHttp 客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        
        // 安全限制：最多运行30轮
        for (int i = 0; i < 30; i++) {
            System.out.println("\n=== 子智能体第 " + (i + 1) + " 轮 ===");
            
            // 检查并压缩消息
//            if (CompressionHandler.needsCompression(subMessages)) {
//                JsonArray compressedMessages = CompressionHandler.compressMessages(subMessages);
//                System.out.println("子智能体消息已压缩，从 " + subMessages.size() + " 条压缩到 " + compressedMessages.size() + " 条");
//                subMessages = compressedMessages;
//            }
            
            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("temperature", 0);
            requestBody.addProperty("max_tokens", 8000);
            requestBody.addProperty("stream", false);
            requestBody.addProperty("system", SUBAGENT_SYSTEM);
            requestBody.addProperty("tool_choice", "auto");
            requestBody.addProperty("enable_thinking", false);
            requestBody.add("messages", subMessages);
            requestBody.add("tools", AgentLoop.getChildTools());
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .build();
            
            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("子智能体请求失败，状态码：" + response.code());
                    System.out.println("错误信息：" + response.body().string());
                    return "子智能体请求失败";
                }
                
                // 解析响应
                String responseBody = response.body().string();
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                // 提取助手消息
                JsonObject assistantMessage = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message");
                
                // 添加助手消息到subMessages
                subMessages.add(assistantMessage);
                
                // 提取 finish_reason
                String finishReason = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .get("finish_reason").getAsString();
                
                if (!finishReason.equals("tool_calls")) {
                    // 如果不是工具调用，返回结果
                    String content = assistantMessage.has("content") ? assistantMessage.get("content").getAsString() : "(no summary)";
                    System.out.println("子智能体完成，返回结果：" + content);
                    return content;
                }
                
                // 处理工具调用
                JsonArray results = new JsonArray();
                
                // 检查助手消息是否包含工具调用
                if (assistantMessage.has("tool_calls")) {
                    JsonArray toolCalls = assistantMessage.getAsJsonArray("tool_calls");
                    for (JsonElement toolCallElement : toolCalls) {
                        JsonObject toolCall = toolCallElement.getAsJsonObject();
                        String toolName = toolCall.get("function").getAsJsonObject().get("name").getAsString();
                        JsonElement argumentsElement = toolCall.get("function").getAsJsonObject().get("arguments");
                        JsonObject toolInput;
                        if (argumentsElement.isJsonObject()) {
                            toolInput = argumentsElement.getAsJsonObject();
                        } else if (argumentsElement.isJsonPrimitive() && argumentsElement.getAsJsonPrimitive().isString()) {
                            // 如果arguments是字符串，先解析为JsonObject
                            Gson argumentGson = new Gson();
                            toolInput = argumentGson.fromJson(argumentsElement.getAsString(), JsonObject.class);
                        } else {
                            toolInput = new JsonObject();
                        }
                        
                        // 执行工具调用
                        System.out.println("子智能体执行工具调用: " + toolName + "，输入: " + toolInput.toString());
                        String output = runTool(toolName, toolInput);
                        System.out.println("子智能体工具调用结果: " + output);
                        
                        // 构建工具结果
                        JsonObject toolResult = new JsonObject();
                        toolResult.addProperty("tool_call_id", toolCall.get("id").getAsString());
                        toolResult.addProperty("role", "tool");
                        toolResult.addProperty("name", toolName);
                        toolResult.addProperty("content", output);
                        
                        results.add(toolResult);
                    }
                }
                
                // 添加工具结果到subMessages
                for (JsonElement result : results) {
                    subMessages.add(result.getAsJsonObject());
                }
            }
        }
        
        // 如果达到安全限制，返回提示
        return "子智能体运行达到安全限制";
    }

    // 执行工具调用
    private static String runTool(String toolName, JsonObject input) {
        if (toolName.equals("skill")) {
            String name = input.get("name").getAsString();
            if (skillLoader.hasSkill(name)) {
                return skillLoader.getContent(name);
            } else {
                return "错误：未知技能 '" + name + "'。可用技能：\n" + skillLoader.getDescriptions();
            }
        }
        return ToolHandler.runTool(toolName, input);
    }
}