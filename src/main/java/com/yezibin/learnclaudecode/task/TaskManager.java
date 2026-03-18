package com.yezibin.learnclaudecode.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskManager {
    private static TaskManager instance;
    private final Path tasksDir;
    private int nextId;
    private final Gson gson;

    private TaskManager() {
        this.tasksDir = Paths.get("./tasks");
        this.gson = new Gson();
        try {
            Files.createDirectories(tasksDir);
            this.nextId = maxId() + 1;
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize tasks directory", e);
        }
    }

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    private int maxId() {
        try {
            return Files.list(tasksDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("task_\\d+\\.json"))
                    .map(path -> {
                        String name = path.getFileName().toString();
                        return Integer.parseInt(name.replace("task_", "").replace(".json", ""));
                    })
                    .max(Integer::compare)
                    .orElse(0);
        } catch (IOException e) {
            return 0;
        }
    }

    public String create(String subject, String description, List<Integer> blockedBy, List<Integer> blocks) {
        JsonObject task = new JsonObject();
        int newTaskId = nextId;
        task.addProperty("id", newTaskId);
        task.addProperty("subject", subject);
        task.addProperty("description", description);
        task.addProperty("status", "pending");
        
        if (blockedBy == null) {
            blockedBy = new ArrayList<>();
        }
        if (blocks == null) {
            blocks = new ArrayList<>();
        }
        task.add("blockedBy", gson.toJsonTree(blockedBy));
        task.add("blocks", gson.toJsonTree(blocks));
        
        task.addProperty("owner", "");

        save(task);
        nextId++;
        
        // 更新被依赖任务的blocks字段
        for (Integer dependencyId : blockedBy) {
            JsonObject dependencyTask = load(dependencyId);
            if (dependencyTask != null) {
                com.google.gson.JsonArray blocksArray = dependencyTask.get("blocks").getAsJsonArray();
                java.util.List<Integer> dependencyBlocks = new java.util.ArrayList<>();
                for (int i = 0; i < blocksArray.size(); i++) {
                    dependencyBlocks.add(blocksArray.get(i).getAsInt());
                }
                if (!dependencyBlocks.contains(newTaskId)) {
                    dependencyBlocks.add(newTaskId);
                    dependencyTask.add("blocks", gson.toJsonTree(dependencyBlocks));
                    save(dependencyTask);
                }
            }
        }
        
        return gson.toJson(task, JsonObject.class);
    }
    
    public String create(String subject, String description) {
        return create(subject, description, null, null);
    }

    public String get(int taskId) {
        Path taskPath = tasksDir.resolve("task_" + taskId + ".json");
        if (!Files.exists(taskPath)) {
            return "Task not found: " + taskId;
        }

        try {
            String content = Files.readString(taskPath);
            return content;
        } catch (IOException e) {
            return "Error reading task: " + e.getMessage();
        }
    }

    public String listAll() {
        try {
            List<String> taskContents = Files.list(tasksDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("task_\\d+\\.json"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException e) {
                            return "Error reading task file: " + e.getMessage();
                        }
                    })
                    .collect(Collectors.toList());

            if (taskContents.isEmpty()) {
                return "No tasks found";
            }

            return String.join("\n\n", taskContents);
        } catch (IOException e) {
            return "Error listing tasks: " + e.getMessage();
        }
    }

    public String update(int taskId, String status, List<Integer> addBlockedBy, List<Integer> addBlocks) {
        JsonObject task = load(taskId);
        if (task == null) {
            return "Task not found: " + taskId;
        }

        if (status != null) {
            task.addProperty("status", status);
            if (status.equals("completed")) {
                clearDependency(taskId);
            }
        }

        if (addBlockedBy != null && !addBlockedBy.isEmpty()) {
            com.google.gson.JsonArray blockedByArray = task.get("blockedBy").getAsJsonArray();
            java.util.List<Integer> blockedBy = new java.util.ArrayList<>();
            for (int i = 0; i < blockedByArray.size(); i++) {
                blockedBy.add(blockedByArray.get(i).getAsInt());
            }
            blockedBy.addAll(addBlockedBy);
            task.add("blockedBy", gson.toJsonTree(blockedBy));
        }

        if (addBlocks != null && !addBlocks.isEmpty()) {
            com.google.gson.JsonArray blocksArray = task.get("blocks").getAsJsonArray();
            java.util.List<Integer> blocks = new java.util.ArrayList<>();
            for (int i = 0; i < blocksArray.size(); i++) {
                blocks.add(blocksArray.get(i).getAsInt());
            }
            blocks.addAll(addBlocks);
            task.add("blocks", gson.toJsonTree(blocks));
        }

        save(task);
        return gson.toJson(task, JsonObject.class);
    }

    public String delete(int taskId) {
        Path taskPath = tasksDir.resolve("task_" + taskId + ".json");
        if (!Files.exists(taskPath)) {
            return "Task not found: " + taskId;
        }

        try {
            Files.delete(taskPath);
            return "Task deleted: " + taskId;
        } catch (IOException e) {
            return "Error deleting task: " + e.getMessage();
        }
    }

    private JsonObject load(int taskId) {
        Path taskPath = tasksDir.resolve("task_" + taskId + ".json");
        if (!Files.exists(taskPath)) {
            return null;
        }

        try {
            String content = Files.readString(taskPath);
            return gson.fromJson(content, JsonObject.class);
        } catch (IOException e) {
            return null;
        }
    }

    private void save(JsonObject task) {
        int taskId = task.get("id").getAsInt();
        Path taskPath = tasksDir.resolve("task_" + taskId + ".json");

        try {
            Files.writeString(taskPath, gson.toJson(task, JsonObject.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save task", e);
        }
    }

    private void clearDependency(int completedId) {
        try {
            Files.list(tasksDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("task_\\d+\\.json"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            JsonObject task = gson.fromJson(content, JsonObject.class);
                            
                            if (task.has("blockedBy")) {
                                com.google.gson.JsonArray blockedByArray = task.get("blockedBy").getAsJsonArray();
                                java.util.List<Integer> blockedBy = new java.util.ArrayList<>();
                                for (int i = 0; i < blockedByArray.size(); i++) {
                                    blockedBy.add(blockedByArray.get(i).getAsInt());
                                }
                                
                                if (blockedBy.contains(completedId)) {
                                    blockedBy.remove(Integer.valueOf(completedId));
                                    task.add("blockedBy", gson.toJsonTree(blockedBy));
                                    save(task);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error processing task file: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error clearing dependencies: " + e.getMessage());
        }
    }
}