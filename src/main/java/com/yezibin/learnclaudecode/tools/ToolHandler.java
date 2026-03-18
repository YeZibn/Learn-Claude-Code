package com.yezibin.learnclaudecode.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yezibin.learnclaudecode.task.TaskManager;
import com.yezibin.learnclaudecode.todo.TodoManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ToolHandler {

    // 执行工具调用
    public static String runTool(String toolName, JsonObject input) {
        if (toolName.equals("execute_bash")) {
            return runBashTool(input);
        } else if (toolName.equals("read")) {
            return runReadTool(input);
        } else if (toolName.equals("write")) {
            return runWriteTool(input);
        } else if (toolName.equals("edit")) {
            return runEditTool(input);
        } else if (toolName.equals("todo")) {
            return runTodoTool(input);
        } else if (toolName.equals("task_create")) {
            return runTaskCreateTool(input);
        } else if (toolName.equals("task_update")) {
            return runTaskUpdateTool(input);
        } else if (toolName.equals("task_list")) {
            return runTaskListTool(input);
        } else if (toolName.equals("task_get")) {
            return runTaskGetTool(input);
        }
        return "工具调用失败：未知工具";
    }

    // 执行bash工具
    private static String runBashTool(JsonObject input) {
        String command = input.get("command").getAsString();
        try {
            // 执行bash命令
            Process process = Runtime.getRuntime().exec(command);
            
            // 读取命令输出
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
            );
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            
            // 等待命令执行完成
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                return "命令执行成功\n输出：\n" + output.toString();
            } else {
                return "命令执行失败，退出码：" + exitCode + "\n错误输出：\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            return "执行命令时出错：" + e.getMessage();
        }
    }

    // 执行read工具
    private static String runReadTool(JsonObject input) {
        String filePath = input.get("file_path").getAsString();
        int limit = input.has("limit") ? input.get("limit").getAsInt() : -1;
        try {
            // 安全路径检查
            java.nio.file.Path safePath = safePath(filePath);
            if (safePath == null) {
                return "路径不安全，操作被拒绝";
            }
            
            // 读取文件内容
            java.util.List<String> lines = Files.readAllLines(safePath, StandardCharsets.UTF_8);
            
            // 限制返回行数
            if (limit > 0 && limit < lines.size()) {
                lines = lines.subList(0, limit);
            }
            
            // 拼接内容
            StringBuilder content = new StringBuilder();
            for (String line : lines) {
                content.append(line).append("\n");
            }
            
            // 限制返回字符数
            String result = content.toString();
            if (result.length() > 50000) {
                result = result.substring(0, 50000) + "\n...（内容过长，已截断）";
            }
            
            return "文件读取成功\n内容：\n" + result;
        } catch (Exception e) {
            return "读取文件时出错：" + e.getMessage();
        }
    }

    // 执行write工具
    private static String runWriteTool(JsonObject input) {
        String filePath = input.get("file_path").getAsString();
        String content = input.get("content").getAsString();
        try {
            // 安全路径检查
            Path safePath = safePath(filePath);
            if (safePath == null) {
                return "路径不安全，操作被拒绝";
            }
            
            // 确保目录存在
            Path parent = safePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            // 写入文件
            Files.write(safePath, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            return "文件写入成功";
        } catch (Exception e) {
            return "写入文件时出错：" + e.getMessage();
        }
    }

    // 执行edit工具
    private static String runEditTool(JsonObject input) {
        String filePath = input.get("file_path").getAsString();
        String oldString = input.get("old_string").getAsString();
        String newString = input.get("new_string").getAsString();
        try {
            // 安全路径检查
            Path safePath = safePath(filePath);
            if (safePath == null) {
                return "路径不安全，操作被拒绝";
            }
            
            // 读取文件内容
            String content = new String(Files.readAllBytes(safePath), java.nio.charset.StandardCharsets.UTF_8);
            
            // 替换内容
            String newContent = content.replace(oldString, newString);
            
            // 写入文件
            Files.write(safePath, newContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            return "文件编辑成功";
        } catch (Exception e) {
            return "编辑文件时出错：" + e.getMessage();
        }
    }

    // 执行todo工具
    private static String runTodoTool(JsonObject input) {
        JsonArray items = input.get("items").getAsJsonArray();
        try {
            TodoManager todoManager = TodoManager.getInstance();
            return todoManager.update(items);
        } catch (Exception e) {
            return "管理待办事项时出错：" + e.getMessage();
        }
    }

    // 执行task_create工具
    private static String runTaskCreateTool(JsonObject input) {
        String subject = input.get("subject").getAsString();
        String description = input.has("description") ? input.get("description").getAsString() : "";
        java.util.List<Integer> blockedBy = new java.util.ArrayList<>();
        java.util.List<Integer> blocks = new java.util.ArrayList<>();
        if (input.has("blockedBy")) {
            JsonArray blockedByArray = input.get("blockedBy").getAsJsonArray();
            for (int i = 0; i < blockedByArray.size(); i++) {
                blockedBy.add(blockedByArray.get(i).getAsInt());
            }
        }
        if (input.has("blocks")) {
            JsonArray blocksArray = input.get("blocks").getAsJsonArray();
            for (int i = 0; i < blocksArray.size(); i++) {
                blocks.add(blocksArray.get(i).getAsInt());
            }
        }
        try {
            TaskManager taskManager = TaskManager.getInstance();
            return taskManager.create(subject, description, blockedBy, blocks);
        } catch (Exception e) {
            return "创建任务时出错：" + e.getMessage();
        }
    }

    // 执行task_update工具
    private static String runTaskUpdateTool(JsonObject input) {
        int taskId = input.get("task_id").getAsInt();
        String status = input.has("status") ? input.get("status").getAsString() : null;
        try {
            TaskManager taskManager = TaskManager.getInstance();
            return taskManager.update(taskId, status, null, null);
        } catch (Exception e) {
            return "更新任务时出错：" + e.getMessage();
        }
    }

    // 执行task_list工具
    private static String runTaskListTool(JsonObject input) {
        try {
            TaskManager taskManager = TaskManager.getInstance();
            return taskManager.listAll();
        } catch (Exception e) {
            return "列出任务时出错：" + e.getMessage();
        }
    }

    // 执行task_get工具
    private static String runTaskGetTool(JsonObject input) {
        int taskId = input.get("task_id").getAsInt();
        try {
            TaskManager taskManager = TaskManager.getInstance();
            return taskManager.get(taskId);
        } catch (Exception e) {
            return "获取任务时出错：" + e.getMessage();
        }
    }

    // 安全路径检查，沙箱化路径
    private static java.nio.file.Path safePath(String filePath) {
        try {
            // 获取当前工作目录
            java.nio.file.Path cwd = java.nio.file.Paths.get(".").toAbsolutePath().normalize();
            // 解析文件路径
            java.nio.file.Path path = java.nio.file.Paths.get(filePath).toAbsolutePath().normalize();
            // 检查路径是否在工作目录内
            if (path.startsWith(cwd)) {
                return path;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}