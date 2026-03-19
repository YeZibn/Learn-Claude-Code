package com.yezibin.learnclaudecode.todo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TodoManager {
    private static TodoManager instance;
    private List<JsonObject> items;

    private TodoManager() {
        items = new ArrayList<>();
    }

    public static synchronized TodoManager getInstance() {
        if (instance == null) {
            instance = new TodoManager();
        }
        return instance;
    }

    public String update(JsonArray itemsArray) throws Exception {
        List<JsonObject> validated = new ArrayList<>();
        int inProgressCount = 0;

        for (int i = 0; i < itemsArray.size(); i++) {
            JsonObject item = itemsArray.get(i).getAsJsonObject();
            
            String status = item.has("status") ? item.get("status").getAsString() : "pending";

            if (status.equals("in_progress")) {
                inProgressCount++;
            }

            JsonObject validatedItem = new JsonObject();
            validatedItem.addProperty("id", item.get("id").getAsString());
            validatedItem.addProperty("text", item.get("text").getAsString());
            validatedItem.addProperty("status", status);
            validated.add(validatedItem);
        }

        if (inProgressCount > 1) {
            throw new Exception("Only one task can be in_progress");
        }

        this.items = validated;
        return render();
    }

    public String render() {
        if (items.isEmpty()) {
            return "Todo list is empty";
        }

        StringBuilder sb = new StringBuilder("Todo list:\n");
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i);
            sb.append(i + 1).append(". ")
              .append(item.get("text").getAsString())
              .append(" (status: ").append(item.get("status").getAsString()).append(")")
              .append("\n");
        }
        return sb.toString();
    }

    public List<JsonObject> getItems() {
        return items;
    }
}