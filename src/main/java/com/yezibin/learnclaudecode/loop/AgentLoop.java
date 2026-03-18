package com.yezibin.learnclaudecode.loop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import com.yezibin.learnclaudecode.compression.CompressionHandler;
import com.yezibin.learnclaudecode.skills.SkillLoader;
import com.yezibin.learnclaudecode.tools.ToolHandler;
import com.yezibin.learnclaudecode.tools.ToolJson;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AgentLoop {

    // SiliconFlow API 地址
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    // 替换成你自己的 API Key
    private static final String API_KEY = "sk-qqvwbybttiknbxvsxqujxrhmvlvdsaeqcmqtxetdiguimzym";
    // 模型名称
    private static final String MODEL = "Pro/MiniMaxAI/MiniMax-M2.5";
    // 系统提示
    private static final String SYSTEM = "你是一个智能助手，可以使用工具来完成任务。针对于有依赖关系的任务，请注意完善参数中的依赖于此任务的任务ID列表与依赖的任务ID列表，并在每个任务完成后更新任务状态。";
    // 技能加载器
    private static final SkillLoader skillLoader = new SkillLoader();
    
    // 工具定义 - 子智能体工具
    public static JsonArray getChildTools() {
        JsonArray tools = new JsonArray();
        Gson gson = new Gson();
        
        // 从ToolJson类获取工具定义并解析
        JsonObject bashTool = gson.fromJson(ToolJson.getBashTool(), JsonObject.class);
        JsonObject readTool = gson.fromJson(ToolJson.getReadTool(), JsonObject.class);
        JsonObject writeTool = gson.fromJson(ToolJson.getWriteTool(), JsonObject.class);
        JsonObject editTool = gson.fromJson(ToolJson.getEditTool(), JsonObject.class);
        JsonObject todoTool = gson.fromJson(ToolJson.getTodoTool(), JsonObject.class);
        JsonObject skillTool = gson.fromJson(ToolJson.getSkillTool(), JsonObject.class);
        JsonObject taskCreateTool = gson.fromJson(ToolJson.getTaskCreateTool(), JsonObject.class);
        JsonObject taskUpdateTool = gson.fromJson(ToolJson.getTaskUpdateTool(), JsonObject.class);
        JsonObject taskListTool = gson.fromJson(ToolJson.getTaskListTool(), JsonObject.class);
        JsonObject taskGetTool = gson.fromJson(ToolJson.getTaskGetTool(), JsonObject.class);
        
        tools.add(bashTool);
        tools.add(readTool);
        tools.add(writeTool);
        tools.add(editTool);
        tools.add(todoTool);
        tools.add(skillTool);
        tools.add(taskCreateTool);
        tools.add(taskUpdateTool);
        tools.add(taskListTool);
        tools.add(taskGetTool);
        
        return tools;
    }
    
    // 工具定义 - 父智能体工具
    private static JsonArray getParentTools() {
        JsonArray tools = getChildTools();
        
        // 添加task工具
        JsonObject taskTool = new JsonObject();
        taskTool.addProperty("type", "function");
        
        JsonObject taskFunction = new JsonObject();
        taskFunction.addProperty("name", "task");
        taskFunction.addProperty("description", "Spawn a subagent with fresh context.");
        
        JsonObject taskParams = new JsonObject();
        taskParams.addProperty("type", "object");
        
        JsonObject taskProperties = new JsonObject();
        JsonObject promptParam = new JsonObject();
        promptParam.addProperty("type", "string");
        promptParam.addProperty("description", "The prompt for the subagent");
        taskProperties.add("prompt", promptParam);
        
        taskParams.add("properties", taskProperties);
        
        JsonArray taskRequired = new JsonArray();
        taskRequired.add("prompt");
        taskParams.add("required", taskRequired);
        
        taskFunction.add("parameters", taskParams);
        taskTool.add("function", taskFunction);
        tools.add(taskTool);
        
        return tools;
    }
    
    // 获取工具列表
    private static JsonArray getTools() {
        return getParentTools();
    }

    // 执行工具调用
    public static String runTool(String toolName, JsonObject input) {
        if (toolName.equals("task")) {
            String prompt = input.get("prompt").getAsString();
            try {
                return SubAgent.run(prompt);
            } catch (Exception e) {
                return "执行子智能体时出错：" + e.getMessage();
            }
        } else if (toolName.equals("skill")) {
            String name = input.get("name").getAsString();
            if (skillLoader.hasSkill(name)) {
                return skillLoader.getContent(name);
            } else {
                return "错误：未知技能 '" + name + "'。可用技能：\n" + skillLoader.getDescriptions();
            }
        }
        return ToolHandler.runTool(toolName, input);
    }

    // agent循环方法
    public String agentLoop(String query) throws IOException {
        // 构建 messages 数组
        JsonArray messages = new JsonArray();
        
        // system 消息
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", SYSTEM);
        messages.add(systemMsg);
        
        // user 消息
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", query);
        messages.add(userMsg);
        
        // 创建 OkHttp 客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        
        int roundsSinceTodo = 0;
        
        while (true) {
            // 检查是否需要添加todo提醒
            if (roundsSinceTodo >= 3 && messages.size() > 0) {
                JsonObject lastMessage = messages.get(messages.size() - 1).getAsJsonObject();
                if (lastMessage.get("role").getAsString().equals("user")) {
                    // 添加todo提醒
                    String originalContent = lastMessage.get("content").getAsString();
                    lastMessage.addProperty("content", "<reminder>Update your todos.</reminder>\n" + originalContent);
                    System.out.println("已添加todo提醒");
                }
            }
            
            System.out.println("\n=== 开始新一轮请求 ===");
            // 检查并压缩消息
//            if (CompressionHandler.needsCompression(messages)) {
//                JsonArray compressedMessages = CompressionHandler.compressMessages(messages);
//                System.out.println("消息已压缩，从 " + messages.size() + " 条压缩到 " + compressedMessages.size() + " 条");
//                messages = compressedMessages;
//            }
            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("temperature", 0);
            requestBody.addProperty("max_tokens", 8000);
            requestBody.addProperty("stream", false);
            requestBody.addProperty("tool_choice", "auto");
            requestBody.addProperty("enable_thinking", false);
            requestBody.add("messages", messages);
            requestBody.add("tools", getTools());
            System.out.println("请求消息: " + messages.toString());

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
                    System.out.println("请求失败，状态码：" + response.code());
                    System.out.println("错误信息：" + response.body().string());
                    return "请求失败";
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
                
                System.out.println("助手消息: " + assistantMessage.toString());
                
                // 添加助手消息到messages
                messages.add(assistantMessage);
                
                // 提取 finish_reason
                String finishReason = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .get("finish_reason").getAsString();
                
                System.out.println("Finish Reason: " + finishReason);

                if (!finishReason.equals("tool_calls")) {
                    // 如果不是工具调用，返回结果
                    roundsSinceTodo++;
                    return assistantMessage.get("content").getAsString();
                }

                // 处理工具调用
                JsonArray results = new JsonArray();
                JsonArray toolCalls = null;

                // 检查助手消息是否包含工具调用
                if (assistantMessage.has("tool_calls")) {
                    toolCalls = assistantMessage.getAsJsonArray("tool_calls");
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
                        System.out.println("执行工具调用: " + toolName + "，输入: " + toolInput.toString());
                        String output = runTool(toolName, toolInput);
                        System.out.println("工具调用结果: " + output);

                        // 构建工具结果
                        JsonObject toolResult = new JsonObject();
                        toolResult.addProperty("tool_call_id", toolCall.get("id").getAsString());
                        toolResult.addProperty("role", "tool");
                        toolResult.addProperty("name", toolName);
                        toolResult.addProperty("content", output);

                        results.add(toolResult);
                    }
                }

                // 添加工具结果到messages
                for (JsonElement result : results) {
                    messages.add(result.getAsJsonObject());
                }
                
                // 检查是否是todo工具调用
                boolean isTodoToolCall = false;
                if (toolCalls != null) {
                    for (JsonElement toolCallElement : toolCalls) {
                        JsonObject toolCall = toolCallElement.getAsJsonObject();
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

    // 测试方法
    public static void main(String[] args) throws IOException {
        AgentLoop agentLoop = new AgentLoop();
        String result = agentLoop.agentLoop("你目前是在windows系统下，请帮我管理并完成以下任务：创建一个任务，该任务创建一个hello.txt文件。创建第二个任务，其目标是在第一个任务创建的文件中写入helloworld，这个任务需要在第一个任务完成后才能开始，因为其依赖于任务1。创建第三个任务，其目标创建一个world.txt文件，这个任务也需要在第一个任务完成后才能开始，但是可以和任务二并行。");
        System.out.println("结果：" + result);
    }
}