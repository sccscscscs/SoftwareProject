package com.myapp;

import java.util.*;
import java.util.stream.Collectors;

/** 
 * 数据模型 + 汇总工具
 * 核心代码统计模块，提供两种分析模式：
 * 1. 代码量统计（文件数、代码行数、注释行数）
 * 2. 函数长度统计（均值、最大值、最小值、中位数）
 */
public class CodeStatsCore {

    // 支持的编程语言
    public enum Language { JAVA, PYTHON, C, CPP }

    /** 单个函数/方法的统计 */
    public static class FunctionStat {
        public String filePath;
        public String qualName;   // 类名.方法名 / 呵嵌套路径
        public int startLine;     // 1-based
        public int endLine;       // 含该行
        public int length;        // end - start + 1
        public boolean isMethod;
        public boolean isNested;
        public boolean isAsync;

        public FunctionStat(String filePath, String qualName, int startLine, int endLine,
                            boolean isMethod, boolean isNested, boolean isAsync) {
            this.filePath = filePath;
            this.qualName = qualName;
            this.startLine = startLine;
            this.endLine = endLine;
            this.length = Math.max(0, endLine - startLine + 1);
            this.isMethod = isMethod;
            this.isNested = isNested;
            this.isAsync = isAsync;
        }
    }

    /** 汇总（给前端用的四个数 + count） */
    public static class Summary {
        public int count;
        public double mean;
        public int min;
        public int max;
        public double median;

        public static Summary of(List<Integer> lengths) {
            Summary s = new Summary();
            if (lengths == null || lengths.isEmpty()) {
                s.count = 0; s.mean = 0; s.min = 0; s.max = 0; s.median = 0;
                return s;
            }
            
            // 过滤掉长度为0的函数
            List<Integer> validLengths = lengths.stream()
                .filter(len -> len > 0)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                
            if (validLengths.isEmpty()) {
                s.count = 0; s.mean = 0; s.min = 0; s.max = 0; s.median = 0;
                return s;
            }
            
            List<Integer> arr = new ArrayList<>(validLengths);
            Collections.sort(arr);
            s.count = arr.size();
            s.min = arr.get(0);
            s.max = arr.get(arr.size() - 1);
            
            // 正确计算均值
            s.mean = arr.stream().mapToInt(Integer::intValue).average().orElse(0);
            
            // 正确计算中位数
            int size = arr.size();
            if (size % 2 == 0) {
                // 偶数个元素，取中间两个数的平均值
                s.median = (arr.get(size/2 - 1) + arr.get(size/2)) / 2.0;
            } else {
                // 奇数个元素，取中间的数
                s.median = arr.get(size/2);
            }
            
            return s;
        }
    }

    /** 代码量统计结果 */
    public static class CodeMetrics {
        public int fileCount;        // 文件数量
        public int codeLines;        // 代码行数（不含注释和空行）
        public int commentLines;     // 注释行数
        public int blankLines;       // 空行数
        public int totalLines;       // 总行数
        
        public CodeMetrics() {
            this.fileCount = 0;
            this.codeLines = 0;
            this.commentLines = 0;
            this.blankLines = 0;
            this.totalLines = 0;
        }
        
        public void merge(CodeMetrics other) {
            this.fileCount += other.fileCount;
            this.codeLines += other.codeLines;
            this.commentLines += other.commentLines;
            this.blankLines += other.blankLines;
            this.totalLines += other.totalLines;
        }
    }

    /** 统一返回体 */
    public static class AnalyzeResult {
        public Summary summary;
        public Map<String, Object> byFile;     // file -> { summary, functions }
        public List<FunctionStat> functions;   // 全局函数明细（已按长度降序）
        public CodeMetrics codeMetrics;        // 代码量统计
    }

    /** 由函数列表构造返回 */
    public static AnalyzeResult buildResult(List<FunctionStat> functions) {
        AnalyzeResult r = new AnalyzeResult();
        List<Integer> lengths = functions.stream().map(f -> f.length).collect(Collectors.toList());
        r.summary = Summary.of(lengths);

        Map<String, List<FunctionStat>> grouped = functions.stream()
                .collect(Collectors.groupingBy(f -> f.filePath, LinkedHashMap::new, Collectors.toList()));

        Map<String, Object> byFile = new LinkedHashMap<>();
        for (Map.Entry<String, List<FunctionStat>> e : grouped.entrySet()) {
            List<Integer> lens = e.getValue().stream().map(f -> f.length).collect(Collectors.toList());
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("summary", Summary.of(lens));
            List<FunctionStat> sorted = new ArrayList<>(e.getValue());
            sorted.sort(Comparator.<FunctionStat>comparingInt(f -> -f.length)
                    .thenComparingInt(f -> f.startLine));
            one.put("functions", sorted);
            byFile.put(e.getKey(), one);
        }
        r.byFile = byFile;

        r.functions = new ArrayList<>(functions);
        r.functions.sort(Comparator.<FunctionStat>comparingInt(f -> -f.length)
                .thenComparing((FunctionStat f) -> f.filePath)
                .thenComparingInt(f -> f.startLine));
        return r;
    }
}