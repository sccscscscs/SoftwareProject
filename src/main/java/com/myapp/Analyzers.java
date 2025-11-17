package com.myapp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.myapp.CodeStatsCore.CodeMetrics;
import com.myapp.CodeStatsCore.FunctionStat;
import com.myapp.CodeStatsCore.Language;

/** 
 * 通用接口 - 所有语言分析器都实现此接口
 * 提供函数统计和代码量统计两种功能
 */
interface CodeAnalyzer {
    /** 分析代码，返回函数统计信息 */
    List<FunctionStat> analyze(String code, String filePath);
    
    /** 统计代码量（代码行数、注释行数、空行数） */
    CodeMetrics analyzeCodeMetrics(String code, String filePath);
    
    /** 返回支持的语言类型 */
    Language language();
}

/** —— Java 解析：基于 JavaParser —— */
class JavaAnalyzer implements CodeAnalyzer {

    @Override public List<FunctionStat> analyze(String code, String filePath) {
        List<FunctionStat> out = new ArrayList<>();
        CompilationUnit cu = StaticJavaParser.parse(code);

        Deque<String> typeStack = new ArrayDeque<>();
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> traverseType(c, filePath, typeStack, out));
        cu.findAll(EnumDeclaration.class).forEach(e -> traverseEnum(e, filePath, typeStack, out));
        cu.findAll(RecordDeclaration.class).forEach(r -> traverseRecord(r, filePath, typeStack, out));
        return out;
    }

    private void traverseType(ClassOrInterfaceDeclaration type, String filePath,
                              Deque<String> stack, List<FunctionStat> out) {
        stack.push(type.getNameAsString());
        for (BodyDeclaration<?> m : type.getMembers()) {
            if (m instanceof CallableDeclaration<?> c) {
                addCallable(c, filePath, stack, out);
            } else if (m instanceof ClassOrInterfaceDeclaration nested) {
                traverseType(nested, filePath, stack, out);
            } else if (m instanceof EnumDeclaration en) {
                traverseEnum(en, filePath, stack, out);
            } else if (m instanceof RecordDeclaration rd) {
                traverseRecord(rd, filePath, stack, out);
            }
        }
        stack.pop();
    }

    private void traverseEnum(EnumDeclaration en, String filePath,
                              Deque<String> stack, List<FunctionStat> out) {
        stack.push(en.getNameAsString());
        for (BodyDeclaration<?> m : en.getMembers()) {
            if (m instanceof CallableDeclaration<?> c) addCallable(c, filePath, stack, out);
            else if (m instanceof ClassOrInterfaceDeclaration nested) traverseType(nested, filePath, stack, out);
        }
        stack.pop();
    }

    private void traverseRecord(RecordDeclaration rd, String filePath,
                                Deque<String> stack, List<FunctionStat> out) {
        stack.push(rd.getNameAsString());
        for (BodyDeclaration<?> m : rd.getMembers()) {
            if (m instanceof CallableDeclaration<?> c) addCallable(c, filePath, stack, out);
        }
        stack.pop();
    }

    private void addCallable(CallableDeclaration<?> c, String filePath,
                             Deque<String> stack, List<FunctionStat> out) {
        if (c.getRange().isEmpty()) return;
        int start = c.getRange().get().begin.line;
        int end   = c.getRange().get().end.line;
        String qual = String.join(".", reverse(stack)) + "." + c.getNameAsString();
        boolean isNested = stack.size() > 1;
        out.add(new FunctionStat(filePath, qual, start, end, true, isNested, false));
    }

    private List<String> reverse(Deque<String> stack) {
        List<String> list = new ArrayList<>(stack);
        Collections.reverse(list);
        return list;
    }

    @Override 
    public CodeMetrics analyzeCodeMetrics(String code, String filePath) {
        CodeMetrics metrics = new CodeMetrics();
        metrics.fileCount = 1;
        
        String[] lines = code.split("\\r?\\n", -1);
        metrics.totalLines = lines.length;
        
        boolean inBlockComment = false;
        for (String line : lines) {
            String trimmed = line.trim();
            
            // 空行
            if (trimmed.isEmpty()) {
                metrics.blankLines++;
                continue;
            }
            
            // 块注释处理
            if (inBlockComment) {
                metrics.commentLines++;
                if (trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }
            
            // 开始块注释
            if (trimmed.startsWith("/*")) {
                metrics.commentLines++;
                if (!trimmed.contains("*/")) {
                    inBlockComment = true;
                }
                continue;
            }
            
            // 单行注释
            if (trimmed.startsWith("//")) {
                metrics.commentLines++;
                continue;
            }
            
            // 代码行
            metrics.codeLines++;
        }
        
        return metrics;
    }
    
    @Override public Language language() { return Language.JAVA; }
}

/** —— Python 解析：按缩进规则的轻量实现 —— */
class PythonAnalyzer implements CodeAnalyzer {
    private static final Pattern DEF_PATTERN =
            Pattern.compile("^\\s*(async\\s+def|def)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

    @Override public List<FunctionStat> analyze(String code, String filePath) {
        List<FunctionStat> out = new ArrayList<>();
        String[] lines = code.split("\\r?\\n", -1);

        class Frame { String name; int indent; boolean cls; boolean fn;
            Frame(String n,int i,boolean c,boolean f){name=n;indent=i;cls=c;fn=f;} }
        Deque<Frame> stack = new ArrayDeque<>();

        for (int i=0;i<lines.length;i++){
            String line = lines[i];
            String trimmed = line.trim();
            int indent = leadingSpaces(line);

            // 缩进出栈
            while (!stack.isEmpty() && indent <= stack.peek().indent && !trimmed.isEmpty()) {
                stack.pop();
            }

            if (trimmed.startsWith("class ")) {
                String cls = trimmed.substring(6).split("[(:\\s]")[0];
                stack.push(new Frame(cls, indent, true, false));
                continue;
            }

            Matcher m = DEF_PATTERN.matcher(line);
            if (m.find()) {
                boolean isAsync = m.group(1).startsWith("async");
                String fname = m.group(2);
                int start = i + 1;
                int end = start;

                // 找到下一个缩进 <= 当前缩进 的非空非注释行的上一行
                for (int j=i+1;j<lines.length;j++){
                    String t = lines[j].trim();
                    if (t.isEmpty() || t.startsWith("#")) continue;
                    int ind2 = leadingSpaces(lines[j]);
                    if (ind2 <= indent) break;
                    end = j + 1;
                }

                List<String> parts = new ArrayList<>();
                for (Frame f : stack) if (f.cls || f.fn) parts.add(f.name);
                Collections.reverse(parts);
                parts.add(fname);
                String qual = String.join(".", parts);

                boolean isMethod = stack.stream().anyMatch(fr -> fr.cls);
                boolean isNested = stack.stream().anyMatch(fr -> fr.fn);

                out.add(new FunctionStat(filePath, qual, start, Math.max(end, start),
                        isMethod, isNested, isAsync));

                stack.push(new Frame(fname, indent, false, true));
            }
        }
        return out;
    }

    private int leadingSpaces(String s){ int i=0; while (i<s.length() && s.charAt(i)==' ') i++; return i; }

    @Override
    public CodeMetrics analyzeCodeMetrics(String code, String filePath) {
        CodeMetrics metrics = new CodeMetrics();
        metrics.fileCount = 1;
        
        String[] lines = code.split("\\r?\\n", -1);
        metrics.totalLines = lines.length;
        
        boolean inBlockComment = false;
        for (String line : lines) {
            String trimmed = line.trim();
            
            // 空行
            if (trimmed.isEmpty()) {
                metrics.blankLines++;
                continue;
            }
            
            // 多行字符串（文档字符串）处理
            if (trimmed.startsWith("\"\"\"") || trimmed.startsWith("'''")) {
                metrics.commentLines++;
                // 简化处理：如果同一行有结束标记，不改变状态
                String marker = trimmed.startsWith("\"\"\"") ? "\"\"\"" : "'''";
                int firstIdx = trimmed.indexOf(marker);
                int secondIdx = trimmed.indexOf(marker, firstIdx + 3);
                if (secondIdx == -1) {
                    inBlockComment = !inBlockComment;
                }
                continue;
            }
            
            if (inBlockComment) {
                metrics.commentLines++;
                if (trimmed.contains("\"\"\"") || trimmed.contains("'''")) {
                    inBlockComment = false;
                }
                continue;
            }
            
            // 单行注释
            if (trimmed.startsWith("#")) {
                metrics.commentLines++;
                continue;
            }
            
            // 代码行
            metrics.codeLines++;
        }
        
        return metrics;
    }
    
    @Override public Language language() { return Language.PYTHON; }
}

/** —— C/C++ 解析器 —— */
class CppAnalyzer implements CodeAnalyzer {
    // C/C++函数定义的正则表达式（简化版）
    private static final Pattern FUNCTION_PATTERN = 
        Pattern.compile("^\\s*(?:(?:inline|static|extern|virtual|explicit|friend|constexpr)\\s+)*" +
                       "(?:[\\w:<>,\\s\\*&]+)\\s+" +  // 返回类型
                       "([\\w:~]+)\\s*" +              // 函数名
                       "\\([^)]*\\)\\s*" +             // 参数列表
                       "(?:const|override|final|noexcept|throw)?\\s*" +  // 修饰符
                       "(?:\\{|;)");                   // 函数体开始或声明结束

    @Override
    public List<FunctionStat> analyze(String code, String filePath) {
        List<FunctionStat> out = new ArrayList<>();
        String[] lines = code.split("\\r?\\n", -1);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            
            // 跳过注释和预处理指令
            if (trimmed.startsWith("//") || trimmed.startsWith("#") || 
                trimmed.startsWith("/*") || trimmed.isEmpty()) {
                continue;
            }
            
            Matcher m = FUNCTION_PATTERN.matcher(line);
            if (m.find()) {
                String funcName = m.group(1);
                int start = i + 1;
                int end = start;
                
                // 如果是函数定义（有大括号），找到函数结束位置
                if (line.contains("{")) {
                    int braceCount = 1;
                    for (int j = i + 1; j < lines.length && braceCount > 0; j++) {
                        String l = lines[j];
                        for (char c : l.toCharArray()) {
                            if (c == '{') braceCount++;
                            else if (c == '}') braceCount--;
                        }
                        if (braceCount == 0) {
                            end = j + 1;
                            break;
                        }
                        end = j + 1;
                    }
                    
                    out.add(new FunctionStat(filePath, funcName, start, end, 
                            false, false, false));
                }
            }
        }
        
        return out;
    }
    
    @Override
    public CodeMetrics analyzeCodeMetrics(String code, String filePath) {
        CodeMetrics metrics = new CodeMetrics();
        metrics.fileCount = 1;
        
        String[] lines = code.split("\\r?\\n", -1);
        metrics.totalLines = lines.length;
        
        boolean inBlockComment = false;
        for (String line : lines) {
            String trimmed = line.trim();
            
            // 空行
            if (trimmed.isEmpty()) {
                metrics.blankLines++;
                continue;
            }
            
            // 块注释处理
            if (inBlockComment) {
                metrics.commentLines++;
                if (trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }
            
            // 开始块注释
            if (trimmed.startsWith("/*")) {
                metrics.commentLines++;
                if (!trimmed.contains("*/")) {
                    inBlockComment = true;
                }
                continue;
            }
            
            // 单行注释
            if (trimmed.startsWith("//")) {
                metrics.commentLines++;
                continue;
            }
            
            // 代码行
            metrics.codeLines++;
        }
        
        return metrics;
    }
    
    @Override 
    public Language language() { 
        return Language.CPP; // C和C++使用相同的分析器
    }
}
