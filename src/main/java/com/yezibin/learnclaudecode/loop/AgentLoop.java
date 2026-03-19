package com.yezibin.learnclaudecode.loop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import com.yezibin.learnclaudecode.compression.CompressionHandler;

import com.yezibin.learnclaudecode.task.BackgroundTaskManager;
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
    private static final String MODEL = "Pro/zai-org/GLM-5";
    // 系统提示
    private static final String SYSTEM = "你是一个智能团队的总指挥官，负责统筹全局并协调团队成员完成任务。你的职责包括：\n\n" +
            "【角色定位】\n" +
            "- 总指挥官：负责整体任务规划和资源分配\n" +
            "- 协调者：确保团队成员之间的有效沟通\n" +
            "- 监督者：监控任务执行进度和质量\n" +
            "- 决策者：根据团队反馈做出明智的决策\n\n" +
            "【工作流程】\n" +
            "1. 任务分析与规划：\n" +
            "   - 全面分析用户的任务需求\n" +
            "   - 分解为具体、可管理的子任务\n" +
            "   - 确定任务的优先级和依赖关系\n" +
            "   - 制定详细的执行计划，根据安排指定todo事项\n\n" +
            "2. 团队协作安排：\n" +
            "   - 根据成员的专长分配适合的任务\n" +
            "   - 使用team工具创建和管理团队成员\n" +
            "   - 向成员发送详细的任务指令，包括目标、要求和截止时间\n" +
            "   - 确保每个成员理解自己的职责和任务目标\n\n" +
            "3. 任务执行监控：\n" +
            "   - 持续跟踪任务执行进度\n" +
            "   - 定期检查成员的工作质量\n" +
            "   - 及时处理成员的反馈和请求\n" +
            "   - 解决任务执行中的问题和障碍\n" +
            "   - 必要时调整任务分配和执行计划\n\n" +
            "4. 结果整合与交付：\n" +
            "   - 收集所有成员的工作成果\n" +
            "   - 审核和验证工作质量\n" +
            "   - 整合为完整的解决方案\n" +
            "   - 向用户交付最终结果，并提供详细的说明\n\n" +
            "【可用团队成员】\n" +
            "- researcher：研究助手，负责收集和分析信息，提供深入的研究报告\n" +
            "- writer：专业 writers，负责撰写清晰、专业的报告和文档\n" +
            "- analyst：数据分析专家，负责数据处理、分析和可视化\n" +
            "- coder：专业的程序员，负责编写和维护软件代码\n\n" +
            "【工具使用指南】\n" +
            "- team工具：用于管理团队成员，包括创建(spawn)、发送消息(send)、广播消息(broadcast)和列出成员(list)\n" +
            "- task工具：用于创建子智能体处理特定任务\n" +
            "- execute_bash工具：用于执行命令行操作\n" +
            "- read工具：用于读取文件内容\n" +
            "- write工具：用于写入文件内容\n" +
            "- edit工具：用于编辑文件内容\n" +
            "- todo工具：用于管理待办事项，每个事项需要id、text和status字段\n\n" +
            "【工作原则】\n" +
            "- 充分利用团队成员的专长，让专业的人做专业的事\n" +
            "- 保持清晰、准确的任务分配和沟通\n" +
            "- 确保任务按时高质量完成\n" +
            "- 及时调整计划以应对变化和挑战\n" +
            "- 持续学习和改进团队协作流程\n" +
            "- 对团队成员的工作给予及时的反馈和支持\n" +
            "- 保持透明和开放的沟通渠道\n\n" +
            "【决策指南】\n" +
            "- 基于事实和数据做出决策\n" +
            "- 考虑所有团队成员的意见和建议\n" +
            "- 权衡利弊，做出最优选择\n" +
            "- 对决策结果负责，并准备应对可能的风险\n"; 

    
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
        JsonObject backgroundTaskTool = gson.fromJson(ToolJson.getBackgroundTaskTool(), JsonObject.class);
        JsonObject teamTool = gson.fromJson(ToolJson.getTeamTool(), JsonObject.class);
        JsonObject taskTool = gson.fromJson(ToolJson.getTaskTool(), JsonObject.class);


        tools.add(taskTool);
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
        tools.add(backgroundTaskTool);
        tools.add(teamTool);
        
        return tools;
    }
    
    // 工具定义 - 父智能体工具
    private static JsonArray getParentTools() {

        JsonArray tools = new JsonArray();
        Gson gson = new Gson();

        // 从ToolJson类获取工具定义并解析
        JsonObject todoTool = gson.fromJson(ToolJson.getTodoTool(), JsonObject.class);
        JsonObject taskCreateTool = gson.fromJson(ToolJson.getTaskCreateTool(), JsonObject.class);
        JsonObject taskUpdateTool = gson.fromJson(ToolJson.getTaskUpdateTool(), JsonObject.class);
        JsonObject taskListTool = gson.fromJson(ToolJson.getTaskListTool(), JsonObject.class);
        JsonObject taskGetTool = gson.fromJson(ToolJson.getTaskGetTool(), JsonObject.class);
        JsonObject teamTool = gson.fromJson(ToolJson.getTeamTool(), JsonObject.class);

        tools.add(todoTool);
        tools.add(taskCreateTool);
        tools.add(taskUpdateTool);
        tools.add(taskListTool);
        tools.add(taskGetTool);
        tools.add(teamTool);
        
        return tools;
    }
    
    // 获取工具列表
    private static JsonArray getTools() {
        return getParentTools();
    }

    // 执行工具调用
    public static String runTool(String toolName, JsonObject input) {
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
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        System.out.println("智能助手已启动，输入'exit'退出程序");
        
        while (true) {
            // 检查后台任务通知
            BackgroundTaskManager.Notification notification = BackgroundTaskManager.getInstance().getNotification();
            if (notification != null) {
                System.out.println("\n=== 后台任务完成通知 ===");
                System.out.println("任务ID: " + notification.getTaskId());
                System.out.println("任务结果: " + notification.getContent());
                
                // 处理通知，将其作为用户输入处理
                System.out.println("\n处理后台任务通知...");
                String notificationInput = "后台任务完成通知\n任务ID: " + notification.getTaskId() + "\n结果: " + notification.getContent();
                String result = agentLoop.agentLoop(notificationInput);
                System.out.println("助手: " + result);
            }
            
            // 读取用户输入
            System.out.print("\n用户: ");
            String userInput = scanner.nextLine();
            
            // 检查是否退出
            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("退出程序");
                // 关闭后台任务管理器
                BackgroundTaskManager.getInstance().shutdown();
                break;
            }
            
            // 处理用户输入
            String result = agentLoop.agentLoop(userInput);
            System.out.println("助手: " + result);
        }
        
        scanner.close();
    }
}