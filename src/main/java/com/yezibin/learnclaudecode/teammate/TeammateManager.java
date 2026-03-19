package com.yezibin.learnclaudecode.teammate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yezibin.learnclaudecode.loop.AgentLoop;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TeammateManager {
    private final String teamDir;
    private final Gson gson;
    private final MessageBus messageBus;
    private final Map<String, Thread> threads;
    private JsonObject config;

    public TeammateManager(String teamDir) {
        this.teamDir = teamDir;
        this.gson = new Gson();
        this.messageBus = new MessageBus(teamDir);
        this.threads = new HashMap<>();
        this.config = loadConfig();
    }

    private JsonObject loadConfig() {
        Path configPath = Paths.get(teamDir, "config.json");
        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                return gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                System.err.println("加载配置失败: " + e.getMessage());
            }
        }
        JsonObject defaultConfig = new JsonObject();
        defaultConfig.add("members", new JsonArray());
        saveConfig(defaultConfig);
        return defaultConfig;
    }

    private void saveConfig(JsonObject config) {
        Path configPath = Paths.get(teamDir, "config.json");
        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }

    public void spawn(String name, String instructions) {
        JsonArray members = config.getAsJsonArray("members");
        boolean nameExists = false;
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            if (member.get("name").getAsString().equals(name)) {
                System.out.println("成员 " + name + " 已存在");
                nameExists = true;
            }
        }
        if (!nameExists)
        {
            JsonObject newMember = new JsonObject();
            newMember.addProperty("name", name);
            newMember.addProperty("instructions", instructions);
            newMember.addProperty("status", "idle");
            members.add(newMember);
            saveConfig(config);
        }

        Thread thread = new Thread(() -> {
            try {
                _teammate_loop(name, instructions, messageBus, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        threads.put(name, thread);
        thread.start();

        System.out.println("已创建成员: " + name);
    }

    public void sendMessage(String sender, String receiver, String content) {
        messageBus.send(sender, receiver, content);
    }

    public void broadcastMessage(String sender, String content) {
        messageBus.broadcast(sender, content);
    }

    public void updateStatus(String name, String status) {
        JsonArray members = config.getAsJsonArray("members");
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            if (member.get("name").getAsString().equals(name)) {
                member.addProperty("status", status);
                saveConfig(config);
                break;
            }
        }
    }

    public String getStatus(String name) {
        JsonArray members = config.getAsJsonArray("members");
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            if (member.get("name").getAsString().equals(name)) {
                return member.get("status").getAsString();
            }
        }
        return "unknown";
    }

    public List<String> listMembers() {
        List<String> memberNames = new ArrayList<>();
        JsonArray members = config.getAsJsonArray("members");
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            memberNames.add(member.get("name").getAsString());
        }
        return memberNames;
    }

    private void _teammate_loop(String name, String instructions, MessageBus messageBus, TeammateManager manager) throws Exception {
        System.out.println("智能体 " + name + " 启动，指令: " + instructions);
        
        // 导入必要的类
        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        com.google.gson.JsonObject systemMsg = new com.google.gson.JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", instructions);
        messages.add(systemMsg);
        
        // 创建OkHttp客户端
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        
        // API配置
        String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
        String API_KEY = "sk-qqvwbybttiknbxvsxqujxrhmvlvdsaeqcmqtxetdiguimzym";
        String MODEL = "Pro/MiniMaxAI/MiniMax-M2.5";
        
        while (true) {
            // 检查消息
            List<com.google.gson.JsonObject> newMessages = messageBus.receive(name);
            for (com.google.gson.JsonObject message : newMessages) {
                String content = message.get("content").getAsString();
                String sender = message.get("sender").getAsString();
                System.out.println("智能体 " + name + " 收到来自 " + sender + " 的消息: " + content);
                
                // 添加为用户消息
                com.google.gson.JsonObject userMsg = new com.google.gson.JsonObject();
                userMsg.addProperty("role", "user");
                userMsg.addProperty("content", "来自 " + sender + " 的消息: " + content);
                messages.add(userMsg);
                
                // 处理消息
                processMessage(messages, client, API_URL, API_KEY, MODEL, name, messageBus, manager);
            }
            
            Thread.sleep(1000);
        }
    }
    
    private void processMessage(com.google.gson.JsonArray messages, okhttp3.OkHttpClient client, 
                               String API_URL, String API_KEY, String MODEL, 
                               String name, MessageBus messageBus, TeammateManager manager) throws Exception {
        int roundsSinceTodo = 0;
        while (true) {
            // 检查是否需要添加todo提醒
            if (roundsSinceTodo >= 3 && messages.size() > 0) {
                com.google.gson.JsonObject lastMessage = messages.get(messages.size() - 1).getAsJsonObject();
                if (lastMessage.get("role").getAsString().equals("user")) {
                    // 添加todo提醒
                    String originalContent = lastMessage.get("content").getAsString();
                    lastMessage.addProperty("content", "<reminder>Update your todos.</reminder>\n" + originalContent);
                    System.out.println("智能体 " + name + " 已添加todo提醒");
                }
            }
            
            // 构建请求体
            com.google.gson.JsonObject requestBody = new com.google.gson.JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("temperature", 0);
            requestBody.addProperty("max_tokens", 8000);
            requestBody.addProperty("stream", false);
            requestBody.addProperty("tool_choice", "auto");
            requestBody.addProperty("enable_thinking", false);
            requestBody.add("messages", messages);
            requestBody.add("tools", AgentLoop.getChildTools());
            
            // 构建请求
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(okhttp3.RequestBody.create(
                            requestBody.toString(),
                            okhttp3.MediaType.parse("application/json")
                    ))
                    .build();
            
            // 发送请求
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("请求失败，状态码：" + response.code());
                    return;
                }
                
                // 解析响应
                String responseBody = response.body().string();
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.JsonObject jsonResponse = gson.fromJson(responseBody, com.google.gson.JsonObject.class);
                
                // 提取助手消息
                com.google.gson.JsonObject assistantMessage = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message");
                
                System.out.println("智能体 " + name + " 收到助手消息: " + assistantMessage.toString());
                messages.add(assistantMessage);
                
                // 提取finish_reason
                String finishReason = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .get("finish_reason").getAsString();
                
                if (!finishReason.equals("tool_calls")) {
                    // 如果不是工具调用，返回结果
                    roundsSinceTodo++;
                    return;
                }
                
                // 处理工具调用
                com.google.gson.JsonArray results = new com.google.gson.JsonArray();
                com.google.gson.JsonArray toolCalls = null;
                
                if (assistantMessage.has("tool_calls")) {
                    toolCalls = assistantMessage.getAsJsonArray("tool_calls");
                    for (com.google.gson.JsonElement toolCallElement : toolCalls) {
                        com.google.gson.JsonObject toolCall = toolCallElement.getAsJsonObject();
                        String toolName = toolCall.get("function").getAsJsonObject().get("name").getAsString();
                        com.google.gson.JsonElement argumentsElement = toolCall.get("function").getAsJsonObject().get("arguments");
                        com.google.gson.JsonObject toolInput;
                        if (argumentsElement.isJsonObject()) {
                            toolInput = argumentsElement.getAsJsonObject();
                        } else if (argumentsElement.isJsonPrimitive() && argumentsElement.getAsJsonPrimitive().isString()) {
                            com.google.gson.Gson argumentGson = new com.google.gson.Gson();
                            toolInput = argumentGson.fromJson(argumentsElement.getAsString(), com.google.gson.JsonObject.class);
                        } else {
                            toolInput = new com.google.gson.JsonObject();
                        }
                        
                        // 执行工具调用
                        System.out.println("智能体 " + name + " 执行工具调用: " + toolName + "，输入: " + toolInput.toString());
                        String output = AgentLoop.runTool(toolName, toolInput);
                        System.out.println("智能体 " + name + " 工具调用结果: " + output);
                        
                        // 构建工具结果
                        com.google.gson.JsonObject toolResult = new com.google.gson.JsonObject();
                        toolResult.addProperty("tool_call_id", toolCall.get("id").getAsString());
                        toolResult.addProperty("role", "tool");
                        toolResult.addProperty("name", toolName);
                        toolResult.addProperty("content", output);
                        
                        results.add(toolResult);
                    }
                }
                
                // 添加工具结果到messages
                for (com.google.gson.JsonElement result : results) {
                    messages.add(result.getAsJsonObject());
                }
                
                // 继续循环处理工具结果
                // 检查是否是todo工具调用
                boolean isTodoToolCall = false;
                if (toolCalls != null) {
                    for (com.google.gson.JsonElement toolCallElement : toolCalls) {
                        com.google.gson.JsonObject toolCall = toolCallElement.getAsJsonObject();
                        String toolName = toolCall.get("function").getAsJsonObject().get("name").getAsString();
                        if (toolName.equals("todo")) {
                            isTodoToolCall = true;
                            break;
                        }
                    }
                }

                // 如果是todo工具调用，重置计数
                if (isTodoToolCall) {
                    roundsSinceTodo = 0;
                } else {
                    roundsSinceTodo++;
                }
            }
        }
    }

    public void shutdown() {
        for (Thread thread : threads.values()) {
            thread.interrupt();
        }
        try {
            for (Thread thread : threads.values()) {
                thread.join(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("所有智能体已关闭");
    }
}