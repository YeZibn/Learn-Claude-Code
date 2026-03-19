package com.yezibin.learnclaudecode.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundTaskManager {
    
    private static BackgroundTaskManager instance;
    private final ExecutorService executorService;
    private final Map<String, TaskStatus> tasks;
    private final BlockingQueue<Notification> notificationQueue;
    private final AtomicInteger taskIdCounter;
    private final Object lock;
    
    private BackgroundTaskManager() {
        // 使用ThreadPoolExecutor显式创建线程池
        this.executorService = new ThreadPoolExecutor(
                2, // 核心线程数
                5, // 最大线程数
                60L, // 空闲线程存活时间
                TimeUnit.SECONDS, // 时间单位
                new LinkedBlockingQueue<>(100), // 工作队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        this.tasks = new HashMap<>();
        this.notificationQueue = new LinkedBlockingQueue<>();
        this.taskIdCounter = new AtomicInteger(0);
        this.lock = new Object();
    }
    
    public static synchronized BackgroundTaskManager getInstance() {
        if (instance == null) {
            instance = new BackgroundTaskManager();
        }
        return instance;
    }
    
    public String run(String command) {
        String taskId = "task_" + taskIdCounter.incrementAndGet();
        
        synchronized (lock) {
            tasks.put(taskId, new TaskStatus("running"));
        }
        
        executorService.submit(() -> {
            String result = "";
            String error = "";
            
            try {
                Process process = Runtime.getRuntime().exec(command);
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result += line + "\n";
                    }
                }
                
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        error += line + "\n";
                    }
                }
                
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    result = "Command failed with exit code " + exitCode + "\n" + error;
                }
                
            } catch (IOException | InterruptedException e) {
                result = "Error executing command: " + e.getMessage();
            }
            
            synchronized (lock) {
                tasks.put(taskId, new TaskStatus("completed", result));
            }
            
            notificationQueue.offer(new Notification(taskId, result));
        });
        
        return taskId;
    }
    
    public TaskStatus getTaskStatus(String taskId) {
        synchronized (lock) {
            return tasks.get(taskId);
        }
    }
    
    public Notification getNotification() {
        try {
            return notificationQueue.poll(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static class TaskStatus {
        private final String status;
        private final String result;
        
        public TaskStatus(String status) {
            this(status, "");
        }
        
        public TaskStatus(String status, String result) {
            this.status = status;
            this.result = result;
        }
        
        public String getStatus() {
            return status;
        }
        
        public String getResult() {
            return result;
        }
    }
    
    public static class Notification {
        private final String taskId;
        private final String content;
        
        public Notification(String taskId, String content) {
            this.taskId = taskId;
            this.content = content;
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        public String getContent() {
            return content;
        }
    }
}