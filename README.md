# LearnClaudeCode

LearnClaudeCode 是一个基于大模型的智能代理系统，支持多轮对话、工具调用、技能扩展和消息压缩功能。

## 功能特性

### 核心功能
- **智能体循环**：支持多轮对话和工具调用
- **子智能体**：可创建子智能体处理复杂任务
- **消息压缩**：使用大模型生成对话摘要，优化上下文管理
- **技能系统**：可扩展的技能模块，支持多种功能
- **待办事项**：内置待办事项管理功能
- **工具调用**：支持调用外部工具执行具体任务

### 技术特点
- **基于 Spring Boot**：快速开发和部署
- **OkHttp**：高效的HTTP客户端
- **Gson**：灵活的JSON处理
- **模块化设计**：易于扩展和维护

## 项目结构

```
LearnClaudeCode/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yezibin/learnclaudecode/
│   │   │       ├── compression/    # 消息压缩功能
│   │   │       ├── loop/           # 智能体循环逻辑
│   │   │       ├── skills/         # 技能模块
│   │   │       ├── todo/           # 待办事项管理
│   │   │       ├── tools/          # 工具处理
│   │   │       └── LearnClaudeCodeApplication.java  # 应用入口
│   │   └── resources/
│   │       └── application.properties  # 配置文件
│   └── test/                        # 测试代码
├── pom.xml                          # Maven配置
└── README.md                        # 项目说明
```

## 安装与运行

### 前提条件
- JDK 17 或更高版本
- Maven 3.6 或更高版本
- 有效的大模型API密钥

### 安装步骤
1. 克隆项目到本地
   ```bash
   git clone <repository-url>
   cd LearnClaudeCode
   ```

2. 配置API密钥
   在 `AgentLoop.java` 和 `CompressionHandler.java` 中设置你的API密钥：
   ```java
   private static final String API_KEY = "your-api-key";
   ```

3. 构建项目
   ```bash
   mvn clean compile
   ```

4. 运行项目
   ```bash
   mvn exec:java -Dexec.mainClass="com.yezibin.learnclaudecode.loop.AgentLoop"
   ```

## 使用方法

### 基本使用
1. 运行 `AgentLoop` 主类
2. 在控制台输入你的问题或指令
3. 智能体将分析问题并执行相应的操作
4. 支持多轮对话，可继续输入后续问题

### 工具调用
系统支持以下工具：
- **todo_write**：添加待办事项
- **todo_read**：读取待办事项列表
- **todo_done**：标记待办事项为完成

### 技能扩展
可以通过在 `skills` 目录下添加新的技能模块来扩展系统功能。每个技能需要包含：
- `SKILL.md`：技能描述文件
- 相关的实现代码

## 消息压缩机制

系统采用混合压缩策略：
- **最近三轮对话**：保持完整内容
- **早期对话**：使用大模型生成摘要
- **压缩触发条件**：当消息数量超过6条时自动触发

这种策略既保留了最近对话的完整上下文，又通过摘要减少了整体消息长度，优化了API调用效率。

## 示例对话

```
用户: 帮我创建一个待办事项，内容是"学习Java并发编程"
助手: 已添加待办事项：学习Java并发编程

用户: 查看我的待办事项
助手: 你的待办事项列表：
1. 学习Java并发编程 (未完成)

用户: 标记第一个待办事项为完成
助手: 已将"学习Java并发编程"标记为完成

用户: 再次查看待办事项
助手: 你的待办事项列表：
1. 学习Java并发编程 (已完成)
```

## 配置说明

### API配置
- `API_URL`：大模型API地址
- `API_KEY`：API访问密钥
- `MODEL`：使用的模型名称

### 压缩配置
- `RECENT_ROUNDS`：保留的最近对话轮数（默认3轮）

## 性能优化

1. **消息压缩**：减少API调用的消息长度
2. **错误处理**：大模型调用失败时使用备用摘要
3. **超时设置**：合理的HTTP请求超时时间

## 扩展建议

1. **添加更多工具**：根据需要扩展工具库
2. **优化摘要生成**：调整摘要提示以获得更好的效果
3. **添加缓存机制**：缓存常见问题的回答
4. **支持更多技能**：扩展技能系统以支持更多功能

## 许可证

本项目采用 MIT 许可证 - 详见 LICENSE 文件

## 贡献

欢迎提交 issue 和 pull request 来改进这个项目！

## 联系方式

- 项目维护者：yezibin
- 邮箱：[your-email@example.com]
- 项目地址：[repository-url]