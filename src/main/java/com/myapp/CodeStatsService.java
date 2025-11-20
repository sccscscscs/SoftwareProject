package com.myapp;

import com.myapp.CodeStatsCore.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** 
 * 代码统计服务类（三种）
 */
public class CodeStatsService {
    
    // 统计模式常量
    public static final int MODE_CODE_METRICS = 1;      // 代码量统计模式
    public static final int MODE_FUNCTION_LENGTH = 2;   // 函数长度统计模式
    public static final int MODE_BOTH = 3;              // 两种都统计模式

    private final Map<Language, CodeAnalyzer> analyzers = Map.of(
            Language.JAVA, new JavaAnalyzer(),
            Language.PYTHON, new PythonAnalyzer(),
            Language.C, new CppAnalyzer(),
            Language.CPP, new CppAnalyzer(),
            Language.CSHARP, new CSharpAnalyzer()
    );

    /** 前端若直接传代码（不是磁盘文件），用这个结构 */
    public static class InMemoryFile {
        public String path;  // 仅用于显示
        public String code;
    }

    /** 统一请求体：二选一或同时传 */
    public static class AnalyzeRequest {
        public Language language;          // 必填：JAVA / PYTHON / C / CPP
        public List<InMemoryFile> files;   // 可选：内存代码
        public List<String> paths;         // 可选：文件或目录路径
        public int mode = MODE_FUNCTION_LENGTH; // 统计模式，默认为函数长度统计
    }

    /** 如果前端输入“代码量”，识别并使用 */
    public static boolean isCodeStatIntent(String userInput) {
        return userInput != null && userInput.contains("代码量");
    }

    /** 
     * 主入口：根据模式返回不同的统计结果
     * MODE_CODE_METRICS：返回代码量统计（文件数、代码行数、注释行数）
     * MODE_FUNCTION_LENGTH：返回函数长度统计（均值/最大/最小/中位数 + 函数明细）
     * MODE_BOTH：返回代码量统计和函数长度统计
     */
    public AnalyzeResult analyze(AnalyzeRequest req) {
        if (req == null || req.language == null)
            throw new IllegalArgumentException("language 不能为空");

        CodeAnalyzer analyzer = analyzers.get(req.language);
        if (analyzer == null)
            throw new IllegalArgumentException("不支持的语言: " + req.language);

        // 根据模式选择不同的分析方法
        switch (req.mode) {
            case MODE_CODE_METRICS:
                return analyzeCodeMetrics(req, analyzer);
            case MODE_FUNCTION_LENGTH:
                return analyzeFunctionLength(req, analyzer);
            case MODE_BOTH:
                return analyzeBoth(req, analyzer);
            default:
                return analyzeFunctionLength(req, analyzer);
        }
    }
    
    /** 代码量统计*/
    private AnalyzeResult analyzeCodeMetrics(AnalyzeRequest req, CodeAnalyzer analyzer) {
        CodeMetrics totalMetrics = new CodeMetrics();
        
        if (req.files != null) {
            for (InMemoryFile f : req.files) {
                if (f == null || f.code == null) continue;
                String path = (f.path == null || f.path.isBlank()) ? "<memory>" : f.path;
                CodeMetrics metrics = analyzer.analyzeCodeMetrics(f.code, path);
                totalMetrics.merge(metrics);
            }
        }
        if (req.paths != null) {
            for (String p : req.paths) {
                if (p == null || p.isBlank()) continue;
                Path path = Paths.get(p);
                if (Files.isDirectory(path)) {
                    try {
                        Files.walk(path)
                                .filter(fp -> matchExt(fp, req.language))
                                .forEach(fp -> {
                                    CodeMetrics metrics = readAndAnalyzeMetrics(analyzer, fp);
                                    totalMetrics.merge(metrics);
                                });
                    } catch (IOException ignored) {}
                } else if (Files.isRegularFile(path) && matchExt(path, req.language)) {
                    CodeMetrics metrics = readAndAnalyzeMetrics(analyzer, path);
                    totalMetrics.merge(metrics);
                }
            }
        }
        
        AnalyzeResult result = new AnalyzeResult();
        result.codeMetrics = totalMetrics;
        return result;
    }
    
    /** 函数长度统计 */
    private AnalyzeResult analyzeFunctionLength(AnalyzeRequest req, CodeAnalyzer analyzer) {
        List<FunctionStat> all = new ArrayList<>();

        if (req.files != null) {
            for (InMemoryFile f : req.files) {
                if (f == null || f.code == null) continue;
                String path = (f.path == null || f.path.isBlank()) ? "<memory>" : f.path;
                all.addAll(analyzer.analyze(f.code, path));
            }
        }
        if (req.paths != null) {
            for (String p : req.paths) {
                if (p == null || p.isBlank()) continue;
                Path path = Paths.get(p);
                if (Files.isDirectory(path)) {
                    try {
                        Files.walk(path)
                                .filter(fp -> matchExt(fp, req.language))
                                .forEach(fp -> all.addAll(readAndAnalyze(analyzer, fp)));
                    } catch (IOException ignored) {}
                } else if (Files.isRegularFile(path) && matchExt(path, req.language)) {
                    all.addAll(readAndAnalyze(analyzer, path));
                }
            }
        }

        return CodeStatsCore.buildResult(all);
    }
    
    /** 都统计 */
    private AnalyzeResult analyzeBoth(AnalyzeRequest req, CodeAnalyzer analyzer) {
        // 先执行代码量
        AnalyzeResult codeMetricsResult = analyzeCodeMetrics(req, analyzer);
        
        // 再执行函数长度
        AnalyzeResult functionLengthResult = analyzeFunctionLength(req, analyzer);
        
        // 合并结果
        AnalyzeResult result = new AnalyzeResult();
        result.codeMetrics = codeMetricsResult.codeMetrics;
        result.summary = functionLengthResult.summary;
        result.functions = functionLengthResult.functions;
        
        return result;
    }

    /** 判断文件扩展名是否匹配语言类型 */
    private boolean matchExt(Path path, Language lang) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return switch (lang) {
            case JAVA -> name.endsWith(".java");
            case PYTHON -> name.endsWith(".py");
            case C -> name.endsWith(".c") || name.endsWith(".h");
            case CPP -> name.endsWith(".cpp") || name.endsWith(".cc") || 
                       name.endsWith(".cxx") || name.endsWith(".hpp") || 
                       name.endsWith(".hh") || name.endsWith(".hxx");
            case CSHARP -> name.endsWith(".cs");
        };
    }

    /** 读取文件并分析函数 */
    private List<FunctionStat> readAndAnalyze(CodeAnalyzer analyzer, Path fp) {
        try {
            String code = Files.readString(fp, StandardCharsets.UTF_8);
            return analyzer.analyze(code, fp.toString());
        } catch (IOException e) {
            return List.of();
        }
    }
    
    /** 读取文件并分析代码量 */
    private CodeMetrics readAndAnalyzeMetrics(CodeAnalyzer analyzer, Path fp) {
        try {
            String code = Files.readString(fp, StandardCharsets.UTF_8);
            return analyzer.analyzeCodeMetrics(code, fp.toString());
        } catch (IOException e) {
            return new CodeMetrics();
        }
    }

}

