package com.yezibin.learnclaudecode.tools;

import java.util.Arrays;
import java.util.List;

/**
 * 系统工具 JSON 定义类
 * 包含 bash、文件操作、待办、任务管理等工具的定义
 */
public class ToolJson {

    // ========== Bash 工具 ==========
    public static String getBashTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"execute_bash\"," +
                "\"description\":\"执行 bash 命令，适用于系统命令行操作\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"command\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要执行的 bash 命令，例如：ls -la、pwd、mkdir test\"" +
                "}" +
                "}," +
                "\"required\":[\"command\"]" +
                "}" +
                "}" +
                "}";
    }

    // ========== 文件操作工具 ==========
    public static String getReadTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"read\"," +
                "\"description\":\"读取文件内容，支持指定行数限制\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"file_path\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要读取的文件路径，例如：/path/to/file.txt、./test.json\"" +
                "}," +
                "\"limit\":{" +
                "\"type\":\"integer\"," +
                "\"description\":\"限制返回的行数，默认返回所有行，示例值：10、20\"" +
                "}" +
                "}," +
                "\"required\":[\"file_path\"]" +
                "}" +
                "}" +
                "}";
    }

    public static String getWriteTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"write\"," +
                "\"description\":\"写入内容到指定文件，会覆盖原有内容\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"file_path\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要写入的文件路径，例如：/path/to/file.txt、./output.log\"" +
                "}," +
                "\"content\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要写入的文件内容，可以是文本、JSON、代码等\"" +
                "}" +
                "}," +
                "\"required\":[\"file_path\",\"content\"]" +
                "}" +
                "}" +
                "}";
    }

    public static String getEditTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"edit\"," +
                "\"description\":\"替换文件中的指定字符串内容\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"file_path\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要编辑的文件路径，例如：/path/to/file.txt\"" +
                "}," +
                "\"old_string\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"要替换的旧字符串，支持模糊匹配\"" +
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

    // ========== 待办事项工具 ==========
    public static String getTodoTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"todo\"," +
                "\"description\":\"管理待办事项，支持添加/更新待办状态\"," +
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
                "\"description\":\"待办事项内容，例如：完成项目文档、测试接口\"" +
                "}," +
                "\"status\":{" +
                "\"type\":\"string\"," +
                "\"enum\":[\"pending\",\"in_progress\",\"completed\"]," +
                "\"description\":\"待办事项状态：pending(待处理)、in_progress(进行中)、completed(已完成)\"" +
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

    // ========== 任务管理工具 ==========
    public static String getTaskCreateTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"task_create\"," +
                "\"description\":\"创建新任务\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"subject\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"任务标题/主题，例如：开发工具调用模块、修复API bug\"" +
                "}," +
                "\"description\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"任务详细描述，可选字段\"" +
                "}," +
                "\"blockedBy\":{" +
                "\"type\":\"array\"," +
                "\"items\":{" +
                "\"type\":\"integer\"" +
                "}," +
                "\"description\":\"此任务依赖的任务ID列表，例如：[1, 2]表示依赖任务1和任务2\"" +
                "}," +
                "\"blocks\":{" +
                "\"type\":\"array\"," +
                "\"items\":{" +
                "\"type\":\"integer\"" +
                "}," +
                "\"description\":\"依赖于此任务的任务ID列表，例如：[3, 4]表示任务3和任务4依赖于此任务\"" +
                "}" +
                "}," +
                "\"required\":[\"subject\"]" +
                "}" +
                "}" +
                "}";
    }

    public static String getTaskUpdateTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"task_update\"," +
                "\"description\":\"更新指定ID的任务状态\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"task_id\":{" +
                "\"type\":\"integer\"," +
                "\"description\":\"任务ID，整数类型，例如：1、100\"" +
                "}," +
                "\"status\":{" +
                "\"type\":\"string\"," +
                "\"description\":\"任务状态，例如：未开始、进行中、已完成、已取消\"" +
                "}" +
                "}," +
                "\"required\":[\"task_id\"]" +
                "}" +
                "}" +
                "}";
    }

    public static String getTaskListTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"task_list\"," +
                "\"description\":\"列出所有任务信息\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{}," +  // 空属性，规范写法
                "\"required\":[]" +
                "}" +
                "}" +
                "}";
    }

    public static String getTaskGetTool() {
        return "{" +
                "\"type\":\"function\"," +
                "\"function\":{" +
                "\"name\":\"task_get\"," +
                "\"description\":\"根据ID获取单个任务的详细信息\"," +
                "\"parameters\":{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"task_id\":{" +
                "\"type\":\"integer\"," +
                "\"description\":\"任务ID，整数类型，例如：1、100\"" +
                "}" +
                "}," +
                "\"required\":[\"task_id\"]" +
                "}" +
                "}" +
                "}";
    }

    // ========== 批量获取工具 ==========
    /**
     * 获取所有系统工具的JSON定义列表
     * @return 包含所有工具的字符串列表
     */
    public static List<String> getAllSystemTools() {
        return Arrays.asList(
                getBashTool(),
                getReadTool(),
                getWriteTool(),
                getEditTool(),
                getTodoTool(),
                getTaskCreateTool(),
                getTaskUpdateTool(),
                getTaskListTool(),
                getTaskGetTool()
        );
    }

    /**
     * 获取指定的工具列表（按需选择）
     * @param toolNames 要获取的工具名（对应方法名前缀，例如：bash、read、task_create）
     * @return 选中的工具JSON列表
     */
    public static List<String> getSelectedTools(String... toolNames) {
        List<String> selected = new java.util.ArrayList<>();
        for (String name : toolNames) {
            switch (name.toLowerCase()) {
                case "bash":
                    selected.add(getBashTool());
                    break;
                case "read":
                    selected.add(getReadTool());
                    break;
                case "write":
                    selected.add(getWriteTool());
                    break;
                case "edit":
                    selected.add(getEditTool());
                    break;
                case "todo":
                    selected.add(getTodoTool());
                    break;
                case "task_create":
                    selected.add(getTaskCreateTool());
                    break;
                case "task_update":
                    selected.add(getTaskUpdateTool());
                    break;
                case "task_list":
                    selected.add(getTaskListTool());
                    break;
                case "task_get":
                    selected.add(getTaskGetTool());
                    break;
                default:
                    System.out.println("未知工具名：" + name);
            }
        }
        return selected;
    }
}