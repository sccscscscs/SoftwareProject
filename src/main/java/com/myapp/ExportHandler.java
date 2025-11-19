package com.myapp;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.GsonBuilder;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 处理文件导出相关操作的类
 */
public class ExportHandler {
    private final DuckGUI gui;
    
    public ExportHandler(DuckGUI gui) {
        this.gui = gui;
    }
    
    /**
     * 导出所有语言统计结果
     */
    public void exportAllLanguageResults(Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results, File directory, int mode) {
        // 创建文件选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出所有语言统计结果");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // 添加文件过滤器
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV文件 (*.csv)";
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            
            @Override
            public String getDescription() {
                return "JSON文件 (*.json)";
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }
            
            @Override
            public String getDescription() {
                return "Excel文件 (*.xlsx)";
            }
        });
        
        // 设置默认过滤器
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int option = fileChooser.showSaveDialog(gui);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            javax.swing.filechooser.FileFilter chosenFilter = fileChooser.getFileFilter();
            
            // 确保文件有正确的扩展名
            String fileName = selectedFile.getAbsolutePath();
            if (chosenFilter.getDescription().contains("CSV") && !fileName.toLowerCase().endsWith(".csv")) {
                fileName += ".csv";
                selectedFile = new File(fileName);
            } else if (chosenFilter.getDescription().contains("JSON") && !fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json";
                selectedFile = new File(fileName);
            } else if (chosenFilter.getDescription().contains("Excel") && !fileName.toLowerCase().endsWith(".xlsx")) {
                fileName += ".xlsx";
                selectedFile = new File(fileName);
            }
            
            // 执行导出操作
            try {
                if (fileName.toLowerCase().endsWith(".csv")) {
                    exportToCSV(results, selectedFile, directory, mode);
                } else if (fileName.toLowerCase().endsWith(".json")) {
                    exportToJSON(results, selectedFile, directory, mode);
                } else if (fileName.toLowerCase().endsWith(".xlsx")) {
                    exportToExcel(results, selectedFile, directory, mode);
                }
                
                JOptionPane.showMessageDialog(gui, 
                    "结果已成功导出到:\n" + selectedFile.getAbsolutePath(), 
                    "导出成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(gui, 
                    "导出失败: " + e.getMessage(), 
                    "导出失败", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 导出为CSV格式
     */
    private void exportToCSV(Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results, File file, File directory, int mode) throws Exception {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            // 写入头部
            writer.println("目录: " + directory.getAbsolutePath());
            writer.println("语言,源文件数,代码行数,空行数,注释行数,函数个数,最大值,最小值,均值,中位数");
            
            // 写入数据
            int totalFiles = 0;
            int totalCodeLines = 0;
            int totalBlankLines = 0;
            int totalCommentLines = 0;
            int totalFunctions = 0;
            int maxFunctionLength = 0;
            int minFunctionLength = Integer.MAX_VALUE;
            double totalMean = 0;
            double totalMedian = 0;
            
            // 按指定顺序写入数据
            CodeStatsCore.Language[] languages = {
                CodeStatsCore.Language.C,
                CodeStatsCore.Language.CPP,
                CodeStatsCore.Language.JAVA,
                CodeStatsCore.Language.PYTHON,
                CodeStatsCore.Language.CSHARP
            };
            
            for (CodeStatsCore.Language language : languages) {
                CodeStatsCore.AnalyzeResult result = results.get(language);
                
                String fileCount = "/";
                String codeLines = "/";
                String blankLines = "/";
                String commentLines = "/";
                String functionCount = "/";
                String maxLen = "/";
                String minLen = "/";
                String mean = "/";
                String median = "/";
                
                if (result.codeMetrics != null) {
                    CodeStatsCore.CodeMetrics metrics = result.codeMetrics;
                    fileCount = String.valueOf(metrics.fileCount);
                    codeLines = String.valueOf(metrics.codeLines);
                    blankLines = String.valueOf(metrics.blankLines);
                    commentLines = String.valueOf(metrics.commentLines);
                    
                    // 累计总计
                    totalFiles += metrics.fileCount;
                    totalCodeLines += metrics.codeLines;
                    totalBlankLines += metrics.blankLines;
                    totalCommentLines += metrics.commentLines;
                }
                
                if (result.summary != null) {
                    functionCount = String.valueOf(result.summary.count);
                    maxLen = String.valueOf(result.summary.max);
                    minLen = String.valueOf(result.summary.min);
                    mean = String.format("%.2f", result.summary.mean);
                    median = String.format("%.2f", result.summary.median);
                    
                    // 累计总计
                    totalFunctions += result.summary.count;
                    maxFunctionLength = Math.max(maxFunctionLength, result.summary.max);
                    minFunctionLength = Math.min(minFunctionLength, result.summary.min);
                    totalMean += result.summary.mean;
                    totalMedian += result.summary.median;
                }
                
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    language.toString(),
                    fileCount,
                    codeLines,
                    blankLines,
                    commentLines,
                    functionCount,
                    maxLen,
                    minLen,
                    mean,
                    median);
            }
            
            // 写入总计行
            String totalFileCount = (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : String.valueOf(totalFiles);
            String totalCodeLineCount = (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : String.valueOf(totalCodeLines);
            String totalBlankLineCount = (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : String.valueOf(totalBlankLines);
            String totalCommentLineCount = (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : String.valueOf(totalCommentLines);
            String totalFunctionCount = (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : String.valueOf(totalFunctions);
            String totalMaxLen = (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : String.valueOf(maxFunctionLength);
            String totalMinLen = (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : String.valueOf(minFunctionLength == Integer.MAX_VALUE ? 0 : minFunctionLength);
            String totalMeanValue = (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : String.format("%.2f", totalMean / results.size());
            String totalMedianValue = (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : String.format("%.2f", totalMedian / results.size());
            
            writer.printf("总计,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                totalFileCount,
                totalCodeLineCount,
                totalBlankLineCount,
                totalCommentLineCount,
                totalFunctionCount,
                totalMaxLen,
                totalMinLen,
                totalMeanValue,
                totalMedianValue);
        }
    }
    
    /**
     * 导出为JSON格式
     */
    private void exportToJSON(Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results, File file, File directory, int mode) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // 构建JSON对象
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("directory", directory.getAbsolutePath());
            
            // 添加各语言统计信息
            Map<String, Object> languagesData = new LinkedHashMap<>();
            int totalFiles = 0;
            int totalCodeLines = 0;
            int totalBlankLines = 0;
            int totalCommentLines = 0;
            int totalFunctions = 0;
            int maxFunctionLength = 0;
            int minFunctionLength = Integer.MAX_VALUE;
            double totalMean = 0;
            double totalMedian = 0;
            
            // 按指定顺序处理数据
            CodeStatsCore.Language[] languages = {
                CodeStatsCore.Language.C,
                CodeStatsCore.Language.CPP,
                CodeStatsCore.Language.JAVA,
                CodeStatsCore.Language.PYTHON,
                CodeStatsCore.Language.CSHARP
            };
            
            for (CodeStatsCore.Language language : languages) {
                CodeStatsCore.AnalyzeResult result = results.get(language);
                
                Map<String, Object> languageData = new LinkedHashMap<>();
                
                if (result.codeMetrics != null) {
                    CodeStatsCore.CodeMetrics metrics = result.codeMetrics;
                    languageData.put("fileCount", metrics.fileCount);
                    languageData.put("codeLines", metrics.codeLines);
                    languageData.put("blankLines", metrics.blankLines);
                    languageData.put("commentLines", metrics.commentLines);
                    
                    // 累计总计
                    totalFiles += metrics.fileCount;
                    totalCodeLines += metrics.codeLines;
                    totalBlankLines += metrics.blankLines;
                    totalCommentLines += metrics.commentLines;
                }
                
                if (result.summary != null) {
                    Map<String, Object> functionStats = new LinkedHashMap<>();
                    functionStats.put("count", result.summary.count);
                    functionStats.put("max", result.summary.max);
                    functionStats.put("min", result.summary.min);
                    functionStats.put("mean", result.summary.mean);
                    functionStats.put("median", result.summary.median);
                    languageData.put("functionStats", functionStats);
                    
                    // 累计总计
                    totalFunctions += result.summary.count;
                    maxFunctionLength = Math.max(maxFunctionLength, result.summary.max);
                    minFunctionLength = Math.min(minFunctionLength, result.summary.min);
                    totalMean += result.summary.mean;
                    totalMedian += result.summary.median;
                }
                
                languagesData.put(language.toString(), languageData);
            }
            
            json.put("languages", languagesData);
            
            // 添加总计信息
            Map<String, Object> totalData = new LinkedHashMap<>();
            totalData.put("fileCount", (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : totalFiles);
            totalData.put("codeLines", (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : totalCodeLines);
            totalData.put("blankLines", (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : totalBlankLines);
            totalData.put("commentLines", (mode == CodeStatsService.MODE_FUNCTION_LENGTH) ? "/" : totalCommentLines);
            totalData.put("functionCount", (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : totalFunctions);
            totalData.put("maxFunctionLength", (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : maxFunctionLength);
            totalData.put("minFunctionLength", (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : (minFunctionLength == Integer.MAX_VALUE ? 0 : minFunctionLength));
            totalData.put("meanFunctionLength", (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : totalMean / results.size());
            totalData.put("medianFunctionLength", (mode == CodeStatsService.MODE_CODE_METRICS) ? "/" : totalMedian / results.size());
            json.put("total", totalData);
            
            // 写入文件
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
        }
    }
    
    /**
     * 导出为Excel格式
     */
    private void exportToExcel(Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results, File file, File directory, int mode) throws Exception {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("所有语言代码统计结果");
        
        // 创建标题行
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("目录: " + directory.getAbsolutePath());
        
        // 创建列标题行
        Row headerRow = sheet.createRow(1);
        String[] headers = {"语言", "源文件数", "代码行数", "空行数", "注释行数", "函数个数", "最大值", "最小值", "均值", "中位数"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 填充数据
        int rowNum = 2;
        int totalFiles = 0;
        int totalCodeLines = 0;
        int totalBlankLines = 0;
        int totalCommentLines = 0;
        int totalFunctions = 0;
        int maxFunctionLength = 0;
        int minFunctionLength = Integer.MAX_VALUE;
        double totalMean = 0;
        double totalMedian = 0;
        
        // 按指定顺序填充数据
        CodeStatsCore.Language[] languages = {
            CodeStatsCore.Language.C,
            CodeStatsCore.Language.CPP,
            CodeStatsCore.Language.JAVA,
            CodeStatsCore.Language.PYTHON,
            CodeStatsCore.Language.CSHARP
        };
        
        for (CodeStatsCore.Language language : languages) {
            CodeStatsCore.AnalyzeResult result = results.get(language);
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(language.toString());
            
            // 代码量统计数据
            if (result.codeMetrics != null) {
                CodeStatsCore.CodeMetrics metrics = result.codeMetrics;
                row.createCell(1).setCellValue(metrics.fileCount);
                row.createCell(2).setCellValue(metrics.codeLines);
                row.createCell(3).setCellValue(metrics.blankLines);
                row.createCell(4).setCellValue(metrics.commentLines);
                
                // 累计总计
                totalFiles += metrics.fileCount;
                totalCodeLines += metrics.codeLines;
                totalBlankLines += metrics.blankLines;
                totalCommentLines += metrics.commentLines;
            } else {
                row.createCell(1).setCellValue("/");
                row.createCell(2).setCellValue("/");
                row.createCell(3).setCellValue("/");
                row.createCell(4).setCellValue("/");
            }
            
            // 函数统计数据
            if (result.summary != null) {
                row.createCell(5).setCellValue(result.summary.count);
                row.createCell(6).setCellValue(result.summary.max);
                row.createCell(7).setCellValue(result.summary.min);
                row.createCell(8).setCellValue(result.summary.mean);
                row.createCell(9).setCellValue(result.summary.median);
                
                // 累计总计
                totalFunctions += result.summary.count;
                maxFunctionLength = Math.max(maxFunctionLength, result.summary.max);
                minFunctionLength = Math.min(minFunctionLength, result.summary.min);
                totalMean += result.summary.mean;
                totalMedian += result.summary.median;
            } else {
                row.createCell(5).setCellValue("/");
                row.createCell(6).setCellValue("/");
                row.createCell(7).setCellValue("/");
                row.createCell(8).setCellValue("/");
                row.createCell(9).setCellValue("/");
            }
        }
        
        // 添加总计行
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("总计");
        
        if (mode == CodeStatsService.MODE_FUNCTION_LENGTH) {
            totalRow.createCell(1).setCellValue("/");
            totalRow.createCell(2).setCellValue("/");
            totalRow.createCell(3).setCellValue("/");
            totalRow.createCell(4).setCellValue("/");
        } else {
            totalRow.createCell(1).setCellValue(totalFiles);
            totalRow.createCell(2).setCellValue(totalCodeLines);
            totalRow.createCell(3).setCellValue(totalBlankLines);
            totalRow.createCell(4).setCellValue(totalCommentLines);
        }
        
        if (mode == CodeStatsService.MODE_CODE_METRICS) {
            totalRow.createCell(5).setCellValue("/");
            totalRow.createCell(6).setCellValue("/");
            totalRow.createCell(7).setCellValue("/");
            totalRow.createCell(8).setCellValue("/");
            totalRow.createCell(9).setCellValue("/");
        } else {
            totalRow.createCell(5).setCellValue(totalFunctions);
            totalRow.createCell(6).setCellValue(maxFunctionLength);
            totalRow.createCell(7).setCellValue(minFunctionLength == Integer.MAX_VALUE ? 0 : minFunctionLength);
            totalRow.createCell(8).setCellValue(totalMean / results.size());
            totalRow.createCell(9).setCellValue(totalMedian / results.size());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 写入文件
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        
        // 关闭工作簿
        workbook.close();
    }
    
    /**
     * 导出函数统计结果
     */
    public void exportFunctionResults(CodeStatsCore.AnalyzeResult result) {
        // 创建文件选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出函数统计结果");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // 添加文件过滤器
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV文件 (*.csv)";
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            
            @Override
            public String getDescription() {
                return "JSON文件 (*.json)";
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }
            
            @Override
            public String getDescription() {
                return "Excel文件 (*.xlsx)";
            }
        });
        
        // 设置默认过滤器
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int option = fileChooser.showSaveDialog(gui);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            javax.swing.filechooser.FileFilter chosenFilter = fileChooser.getFileFilter();
            
            // 确保文件有正确的扩展名
            String fileName = selectedFile.getAbsolutePath();
            if (chosenFilter.getDescription().contains("CSV") && !fileName.toLowerCase().endsWith(".csv")) {
                fileName += ".csv";
                selectedFile = new File(fileName);
            } else if (chosenFilter.getDescription().contains("JSON") && !fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json";
                selectedFile = new File(fileName);
            } else if (chosenFilter.getDescription().contains("Excel") && !fileName.toLowerCase().endsWith(".xlsx")) {
                fileName += ".xlsx";
                selectedFile = new File(fileName);
            }
            
            // 执行导出操作
            try {
                if (fileName.toLowerCase().endsWith(".csv")) {
                    exportFunctionToCSV(result, selectedFile);
                } else if (fileName.toLowerCase().endsWith(".json")) {
                    exportFunctionToJSON(result, selectedFile);
                } else if (fileName.toLowerCase().endsWith(".xlsx")) {
                    exportFunctionToExcel(result, selectedFile);
                }
                
                JOptionPane.showMessageDialog(gui, 
                    "结果已成功导出到:\n" + selectedFile.getAbsolutePath(), 
                    "导出成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(gui, 
                    "导出失败: " + e.getMessage(), 
                    "导出失败", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 导出函数为CSV格式
     */
    private void exportFunctionToCSV(CodeStatsCore.AnalyzeResult result, File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            // 写入头部
            writer.println("文件路径,函数名,起始行,结束行,函数长度,是否为方法,是否为嵌套,是否为异步");
            
            // 写入数据
            for (CodeStatsCore.FunctionStat func : result.functions) {
                writer.printf("%s,%s,%d,%d,%d,%s,%s,%s%n",
                    func.filePath,
                    func.qualName,
                    func.startLine,
                    func.endLine,
                    func.length,
                    func.isMethod,
                    func.isNested,
                    func.isAsync);
            }
        }
    }
    
    /**
     * 导出函数为JSON格式
     */
    private void exportFunctionToJSON(CodeStatsCore.AnalyzeResult result, File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // 构建JSON对象
            Map<String, Object> json = new LinkedHashMap<>();
            
            // 添加汇总信息
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("count", result.summary.count);
            summary.put("mean", result.summary.mean);
            summary.put("min", result.summary.min);
            summary.put("max", result.summary.max);
            summary.put("median", result.summary.median);
            json.put("summary", summary);
            
            // 添加函数列表
            List<Map<String, Object>> functions = new ArrayList<>();
            for (CodeStatsCore.FunctionStat func : result.functions) {
                Map<String, Object> funcMap = new LinkedHashMap<>();
                funcMap.put("filePath", func.filePath);
                funcMap.put("qualName", func.qualName);
                funcMap.put("startLine", func.startLine);
                funcMap.put("endLine", func.endLine);
                funcMap.put("length", func.length);
                funcMap.put("isMethod", func.isMethod);
                funcMap.put("isNested", func.isNested);
                funcMap.put("isAsync", func.isAsync);
                functions.add(funcMap);
            }
            json.put("functions", functions);
            
            // 写入文件
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
        }
    }
    
    /**
     * 导出函数为Excel格式
     */
    private void exportFunctionToExcel(CodeStatsCore.AnalyzeResult result, File file) throws Exception {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("函数统计结果");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"文件路径", "函数名", "起始行", "结束行", "函数长度", "是否为方法", "是否为嵌套", "是否为异步"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 填充数据
        int rowNum = 1;
        for (CodeStatsCore.FunctionStat func : result.functions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(func.filePath);
            row.createCell(1).setCellValue(func.qualName);
            row.createCell(2).setCellValue(func.startLine);
            row.createCell(3).setCellValue(func.endLine);
            row.createCell(4).setCellValue(func.length);
            row.createCell(5).setCellValue(func.isMethod);
            row.createCell(6).setCellValue(func.isNested);
            row.createCell(7).setCellValue(func.isAsync);
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 写入文件
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        
        // 关闭工作簿
        workbook.close();
    }
}