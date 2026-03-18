package com.yezibin.learnclaudecode.tools;

public class ToolJson {

    // 获取bash工具的JSON定义
    public static String getBashTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"execute_bash\"," +
                "\"description\":\"执行 bash 命令\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"command\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要执行的 bash 命令，例如：ls -la、pwd\"" +
                "}" +
                "}," +
                "\"required\":[\"command\"]" +
                "}" +
                "}" +
                "}";
    }

    // 获取read工具的JSON定义
    public static String getReadTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"read\"," +
                "\"description\":\"读取文件内容\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"file_path\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要读取的文件路径，例如：/path/to/file.txt\"" +
                "}," +
                "\"limit\":{" +
                "\"type\":\"integer\"," +
                "\"description\":\"限制返回的行数，默认返回所有行\"" +
                "}" +
                "}," +
                "\"required\":[\"file_path\"]" +
                "}" +
                "}" +
                "}";
    }

    // 获取write工具的JSON定义
    public static String getWriteTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"write\"," +
                "\"description\":\"写入文件内容\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"file_path\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要写入的文件路径，例如：/path/to/file.txt\"" +
                "}," +
                "\"content\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要写入的文件内容\"" +
                "}" +
                "}," +
                "\"required\":[\"file_path\",\"content\"]" +
                "}" +
                "}" +
                "}";
    }

    // 获取edit工具的JSON定义
    public static String getEditTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"edit\"," +
                "\"description\":\"编辑文件内容\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"file_path\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要编辑的文件路径，例如：/path/to/file.txt\"" +
                "}," +
                "\"old_string\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要替换的旧字符串\"" +
                "}," +
                "\"new_string\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要替换的新字符串\"" +
                "}" +
                "}," +
                "\"required\":[\"file_path\",\"old_string\",\"new_string\"]" +
                "}" +
                "}" +
                "}";
    }

    // 获取todo工具的JSON定义
    public static String getTodoTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"todo\"," +
                "\"description\":\"管理待办事项\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"items\":{" +
                "\"type\":\"array\"," +
                "\"items\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"content\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"待办事项内容\"" +
                "}," +
                "\"status\":{" +
                "\"type\":\"string\"," +
                "\"enum\":[\"pending\",\"in_progress\",\"completed\"]," +
                "\"description\":\"待办事项状态\"" +
                "}" +
                "}," +
                "\"required\":[\"content\",\"status\"]" +
                "}" +
                "}" +
                "}," +
                "\"required\":[\"items\"]" +
                "}" +
                "}" +
                "}";
    }

    // 获取skill工具的JSON定义
    public static String getSkillTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"skill\"," +
                "\"description\":\"Load a skill and run it on the current conversation.\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"name\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"The name of the skill to load.\"" +
                "}" +
                "}," +
                "\"required\":[\"name\"]" +
                "}" +
                "}" +
                "}";
    }
}