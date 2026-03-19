package com.yezibin.learnclaudecode.teammate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageBus {
    private final String teamDir;
    private final Gson gson;

    public MessageBus(String teamDir) {
        this.teamDir = teamDir;
        this.gson = new Gson();
        // 确保团队目录存在
        try {
            Files.createDirectories(Paths.get(teamDir));
        } catch (IOException e) {
            System.err.println("创建团队目录失败: " + e.getMessage());
        }
    }

    public void send(String sender, String receiver, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "task");
        message.addProperty("sender", sender);
        message.addProperty("content", content);
        message.addProperty("timestamp", Instant.now().toString());

        String messageJson = gson.toJson(message);
        Path inboxPath = Paths.get(teamDir, receiver + ".jsonl");

        try (BufferedWriter writer = Files.newBufferedWriter(inboxPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(messageJson);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
        }
    }

    public List<JsonObject> receive(String receiver) {
        List<JsonObject> messages = new ArrayList<>();
        Path inboxPath = Paths.get(teamDir, receiver + ".jsonl");

        if (!Files.exists(inboxPath)) {
            return messages;
        }

        try (BufferedReader reader = Files.newBufferedReader(inboxPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject message = gson.fromJson(line, JsonObject.class);
                messages.add(message);
            }
        } catch (IOException e) {
            System.err.println("读取消息失败: " + e.getMessage());
        }

        // 清空收件箱
        try {
            Files.write(inboxPath, new byte[0]);
        } catch (IOException e) {
            System.err.println("清空收件箱失败: " + e.getMessage());
        }

        return messages;
    }

    public void broadcast(String sender, String content) {
        // 实现广播功能，向所有智能体发送消息
        File teamDirFile = new File(teamDir);
        File[] files = teamDirFile.listFiles((dir, name) -> name.endsWith(".jsonl"));
        if (files != null) {
            for (File file : files) {
                String receiver = file.getName().replace(".jsonl", "");
                if (!receiver.equals(sender)) {
                    send(sender, receiver, content);
                }
            }
        }
    }
}