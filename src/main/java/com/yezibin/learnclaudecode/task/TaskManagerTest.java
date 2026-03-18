package com.yezibin.learnclaudecode.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerTest {
    public static void main(String[] args) throws IOException {
        // 清理测试环境
        cleanup();
        
        TaskManager taskManager = TaskManager.getInstance();
        
        // 创建任务1
        String task1Json = taskManager.create("创建txt文件", "创建一个新的txt文件");
        System.out.println("创建任务1: " + task1Json);
        JsonObject task1 = JsonParser.parseString(task1Json).getAsJsonObject();
        int task1Id = task1.get("id").getAsInt();
        
        // 创建任务2，依赖任务1
        List<Integer> blockedBy = new ArrayList<>();
        blockedBy.add(task1Id);
        String task2Json = taskManager.create("向文件中写入helloworld", "在第一个任务创建的文件中写入helloworld", blockedBy, null);
        System.out.println("创建任务2: " + task2Json);
        JsonObject task2 = JsonParser.parseString(task2Json).getAsJsonObject();
        int task2Id = task2.get("id").getAsInt();
        
        // 验证任务2的依赖
        JsonArray task2BlockedBy = task2.get("blockedBy").getAsJsonArray();
        System.out.println("任务2的依赖: " + task2BlockedBy);
        assert task2BlockedBy.size() == 1 : "任务2应该依赖任务1";
        assert task2BlockedBy.get(0).getAsInt() == task1Id : "任务2应该依赖任务1";
        
        // 完成任务1
        String task1Updated = taskManager.update(task1Id, "completed", null, null);
        System.out.println("完成任务1: " + task1Updated);
        
        // 验证任务2的依赖是否被清除
        String task2Updated = taskManager.get(task2Id);
        System.out.println("任务2更新后: " + task2Updated);
        JsonObject task2After = JsonParser.parseString(task2Updated).getAsJsonObject();
        JsonArray task2BlockedByAfter = task2After.get("blockedBy").getAsJsonArray();
        System.out.println("任务2的依赖(完成任务1后): " + task2BlockedByAfter);
        assert task2BlockedByAfter.size() == 0 : "任务1完成后，任务2的依赖应该被清除";
        
        System.out.println("所有测试通过！");
        
        // 清理测试环境
        cleanup();
    }
    
    private static void cleanup() throws IOException {
        Path tasksDir = Paths.get("./tasks");
        if (Files.exists(tasksDir)) {
            Files.list(tasksDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().matches("task_\\d+\\.json"))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("删除任务文件失败: " + e.getMessage());
                    }
                });
        }
        // 重置TaskManager实例
        TaskManager.getInstance();
    }
}