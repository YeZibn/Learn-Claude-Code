package com.yezibin.learnclaudecode.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillLoader {
    private Map<String, Skill> skills;

    public SkillLoader() {
        skills = new HashMap<>();
        // 加载技能目录
        String skillsDir = "/Users/xiezibin/IdeaProjects/LearnClaudeCode/src/main/java/com/yezibin/learnclaudecode/skills";
        loadSkills(Paths.get(skillsDir));
    }

    private void loadSkills(Path skillsDir) {
        try {
            if (Files.exists(skillsDir) && Files.isDirectory(skillsDir)) {
                Files.walk(skillsDir)
                        .filter(path -> path.getFileName().toString().equals("SKILL.md"))
                        .forEach(this::loadSkill);
            }
        } catch (IOException e) {
            System.err.println("加载技能失败: " + e.getMessage());
        }
    }

    private void loadSkill(Path skillFile) {
        try {
            String content = Files.readString(skillFile);
            Skill skill = parseSkill(content);
            // 从文件路径中提取技能名称
            String name = skillFile.getParent().getFileName().toString();
            skills.put(name, skill);
        } catch (IOException e) {
            System.err.println("加载技能文件失败: " + skillFile + ", 错误: " + e.getMessage());
        }
    }

    private Skill parseSkill(String content) {
        Map<String, String> meta = new HashMap<>();
        String body = content;

        // 解析frontmatter
        Pattern frontmatterPattern = Pattern.compile("^---\\n(.*?)\\n---\\n", Pattern.DOTALL);
        Matcher matcher = frontmatterPattern.matcher(content);
        if (matcher.find()) {
            String frontmatter = matcher.group(1);
            body = content.substring(matcher.end());

            // 解析frontmatter中的键值对
            for (String line : frontmatter.split("\\n")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int colonIndex = line.indexOf(':');
                if (colonIndex != -1) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    meta.put(key, value);
                }
            }
        }

        return new Skill(meta, body);
    }

    public String getDescriptions() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Skill> entry : skills.entrySet()) {
            String name = entry.getKey();
            String description = entry.getValue().getMeta().getOrDefault("description", "");
            sb.append("  - " + name + ": " + description + "\n");
        }
        return sb.toString();
    }

    public String getContent(String name) {
        Skill skill = skills.get(name);
        if (skill == null) {
            return "Error: Unknown skill '" + name + "'.";
        }
        return "<skill name=\"" + name + "\">\n" + skill.getBody() + "\n</skill>";
    }

    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }

    private static class Skill {
        private final Map<String, String> meta;
        private final String body;

        public Skill(Map<String, String> meta, String body) {
            this.meta = meta;
            this.body = body;
        }

        public Map<String, String> getMeta() {
            return meta;
        }

        public String getBody() {
            return body;
        }
    }
}