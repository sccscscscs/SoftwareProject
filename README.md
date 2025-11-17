# 唐老鸭和小鸭子 - 多功能应用

这是一个功能丰富、可扩展的Java Swing应用程序，提供代码统计、红包雨游戏、AI对话等多种功能。

## 功能特性

### 1. 代码统计功能
点击**唐老鸭**，输入包含"**代码量**"的文字，即可进入代码统计功能。

#### 支持的语言
- Java
- Python
- C
- C++

#### 两种统计模式

**模式1：代码量统计**
- 文件数量
- 代码行数（不含注释和空行）
- 注释行数
- 空行数
- 代码占比和注释占比

**模式2：函数长度统计**
- 函数数量
- 平均长度
- 最大长度
- 最小长度
- 中位数
- **可视化图表**（柱状图和饼图）

### 2. 红包雨游戏
点击**唐老鸭**，输入包含"**红包雨**"或"**红包**"的文字，即可启动游戏。

#### 游戏玩法
- 使用**方向键 ↑↓←→** 控制小人移动
- 触碰红包即可获得金额
- 游戏时长：**10秒**
- 实时显示倒计时和总金额
- 红包有三种不同形状（圆形、方形、菱形）
- 不同红包金额不同（0.01元 - 10.00元）

### 3. AI对话功能
点击**唐老鸭**，输入任何其他内容，即可与AI进行对话。

#### AI配置说明
应用使用**智谱AI（ChatGLM）**的免费API服务。

**配置方法：**
1. 访问 [https://open.bigmodel.cn/](https://open.bigmodel.cn/) 注册账号
2. 获取免费API Key
3. 设置环境变量：
   ```bash
   export ZHIPU_AI_KEY="你的API密钥"
   ```
   或直接修改 `src/main/java/com/myapp/AIService.java` 中的 `API_KEY` 常量

**注意：** 如果未配置API Key，系统会返回模拟回复和配置提示。

### 4. 小鸭子换装系统
点击任意**小鸭子**，即可打开换装界面（衣柜）。

#### 可用配饰
- 🎩 **帽子**
- 👓 **眼镜**
- 🎀 **围巾领带手杖**

点击"穿上"按钮给小鸭子添加配饰，点击"移除"按钮去除配饰。实时预览效果！

## 运行方法

### 方法1：使用Maven
```bash
# 编译项目
mvn clean compile

# 运行应用
java -cp target/classes com.myapp.DuckGUI
```

### 方法2：使用IDE
直接运行 `com.myapp.DuckGUI` 类的 `main` 方法。

### 方法3：打包运行
```bash
# 打包
mvn package

# 运行
java -jar target/Eweek-1.0-SNAPSHOT.jar
```

## 项目结构

```
Eweek/
├── src/main/java/com/myapp/
│   ├── DuckGUI.java              # 主界面
│   ├── DuckComponent.java        # 鸭子组件
│   ├── CodeStatsCore.java        # 代码统计核心模型
│   ├── CodeStatsService.java     # 代码统计服务
│   ├── Analyzers.java            # 各语言代码分析器
│   ├── ChartPanel.java           # 图表可视化组件
│   ├── RedPacketRainGame.java    # 红包雨游戏
│   ├── AIService.java            # AI对话服务
│   └── Main.java                 # 程序入口
├── src/main/resources/
│   └── images/
│       └── image.png
├── pom.xml                       # Maven配置文件
└── README.md                     # 本文件
```

## 设计模式

本项目运用了多种设计模式，使代码具有良好的可扩展性和可维护性：

### 1. 策略模式（Strategy Pattern）
- `CodeAnalyzer` 接口定义了代码分析的统一接口
- `JavaAnalyzer`、`PythonAnalyzer`、`CppAnalyzer` 分别实现不同语言的分析策略
- 便于添加新的编程语言支持

### 2. 装饰器模式（Decorator Pattern）
- 小鸭子的配饰系统使用装饰器思想
- `DuckComponent` 可以动态添加/移除配饰
- 配饰之间相互独立，可自由组合

### 3. 服务层模式（Service Layer Pattern）
- `CodeStatsService` 提供代码统计的业务逻辑
- `AIService` 封装AI服务调用
- 将业务逻辑与UI层分离

### 4. 模板方法模式（Template Method Pattern）
- `CodeStatsService.analyze()` 定义了分析流程的框架
- 根据不同模式调用不同的具体实现方法

## 依赖项

项目使用以下主要依赖：

- **JavaParser 3.25.4** - 用于Java代码解析
- **JDK 17+** - 项目最低要求

所有依赖已在 `pom.xml` 中配置。

## 代码特点

### 高可扩展性
- 添加新语言：只需实现 `CodeAnalyzer` 接口
- 添加新统计模式：在 `CodeStatsService` 中添加新的模式常量和处理方法
- 添加新配饰：在 `DuckComponent` 的 `drawClothing()` 方法中添加绘制逻辑

### 高可维护性
- 清晰的注释说明每个类和方法的功能
- 合理的类职责划分
- 统一的代码风格

### 良好的用户体验
- 友好的交互界面
- 进度提示和错误处理
- 实时的视觉反馈

## 扩展建议

### 可添加的功能
1. **更多编程语言支持**：JavaScript、Go、Rust等
2. **代码质量分析**：代码复杂度、重复代码检测
3. **更多游戏**：打地鼠、贪吃蛇等
4. **配饰商店**：更多配饰选项
5. **数据导出**：导出统计结果为Excel/PDF
6. **主题切换**：深色模式、浅色模式

### 性能优化建议
1. 对大型项目使用多线程并行分析
2. 添加结果缓存机制
3. 使用流式处理减少内存占用

## 常见问题

**Q: 为什么AI对话返回模拟回复？**  
A: 需要配置智谱AI的API Key。请参考"AI配置说明"部分。

**Q: 如何添加新的编程语言支持？**  
A: 创建新的类实现 `CodeAnalyzer` 接口，并在 `CodeStatsService` 的 `analyzers` Map中注册。

**Q: 红包雨游戏如何控制？**  
A: 使用键盘方向键 ↑↓←→ 控制小人移动。

**Q: 代码统计支持的文件类型有哪些？**  
A: 
- Java: `.java`
- Python: `.py`
- C: `.c`, `.h`
- C++: `.cpp`, `.hpp`, `.cc`, `.cxx`, `.hh`, `.hxx`

## 作者

本项目演示了如何使用Java Swing创建功能丰富的桌面应用。

## 许可证

本项目仅供学习和参考使用。

---

**祝您使用愉快！** 🦆🎉

