package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * 处理代码统计相关操作的类
 */
public class CodeStatsHandler {
    private final DuckGUI gui;
    
    public CodeStatsHandler(DuckGUI gui) {
        this.gui = gui;
    }
    
    /**
     * 显示语言选择对话框
     * 支持Java、Python、C、C++、C#五种语言，以及统计所有语言的选项
     */
    public void showLanguageSelection() {
        String[] options = {"Java", "Python", "C", "C++", "C#", "所有语言"};
        int choice = JOptionPane.showOptionDialog(
            gui,
            "请选择编程语言：",
            "语言选择",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != JOptionPane.CLOSED_OPTION) {
            // 如果选择"所有语言"选项
            if (choice == 5) {
                showAllLanguageStatModeSelection();
            } else {
                CodeStatsCore.Language language = switch (choice) {
                    case 0 -> CodeStatsCore.Language.JAVA;
                    case 1 -> CodeStatsCore.Language.PYTHON;
                    case 2 -> CodeStatsCore.Language.C;
                    case 3 -> CodeStatsCore.Language.CPP;
                    case 4 -> CodeStatsCore.Language.CSHARP;
                    default -> null;
                };
                
                if (language != null) {
                    showStatModeSelection(language);
                }
            }
        }
    }
    
    /**
     * 显示所有语言统计模式选择对话框
     */
    private void showAllLanguageStatModeSelection() {
        String[] options = {"代码量统计", "函数长度统计", "都统计"};
        int choice = JOptionPane.showOptionDialog(
            gui,
            "请选择统计模式：\n\n" +
            "• 代码量统计：统计文件数、代码行数、注释行数等\n" +
            "• 函数长度统计：统计函数的均值、最大值、最小值、中位数\n" +
            "• 都统计：同时进行代码量和函数长度统计",
            "统计模式选择",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != JOptionPane.CLOSED_OPTION) {
            int mode = switch (choice) {
                case 0 -> CodeStatsService.MODE_CODE_METRICS;
                case 1 -> CodeStatsService.MODE_FUNCTION_LENGTH;
                case 2 -> CodeStatsService.MODE_BOTH;
                default -> CodeStatsService.MODE_CODE_METRICS;
            };
            showAllLanguageFileSelectionDialog(mode);
        }
    }
    
    /**
     * 显示所有语言文件选择对话框
     */
    private void showAllLanguageFileSelectionDialog(int mode) {
        // 创建文件选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择包含所有语言文件的目录");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            performAllLanguageCodeAnalysis(selectedDirectory, mode);
        }
    }
    
    /**
     * 执行所有语言的代码分析
     */
    private void performAllLanguageCodeAnalysis(File directory, int mode) {
        // 创建进度对话框
        JDialog progressDialog = new JDialog(gui, "分析中", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(new JLabel("正在分析所有语言的代码，请稍候..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(gui);
        
        // 在后台线程执行分析
        SwingWorker<Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult>, Void> worker = 
            new SwingWorker<Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult>, Void>() {
            
            @Override
            protected Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> doInBackground() throws Exception {
                CodeStatsService service = new CodeStatsService();
                Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results = new LinkedHashMap<>();
                
                // 按指定顺序定义所有要分析的语言
                CodeStatsCore.Language[] languages = {
                    CodeStatsCore.Language.C,
                    CodeStatsCore.Language.CPP,
                    CodeStatsCore.Language.JAVA,
                    CodeStatsCore.Language.PYTHON,
                    CodeStatsCore.Language.CSHARP
                };
                
                // 对每种语言执行分析
                for (CodeStatsCore.Language language : languages) {
                    CodeStatsService.AnalyzeRequest request = new CodeStatsService.AnalyzeRequest();
                    request.language = language;
                    request.paths = List.of(directory.getAbsolutePath());
                    request.mode = mode;
                    
                    CodeStatsCore.AnalyzeResult result = service.analyze(request);
                    results.put(language, result);
                }
                
                return results;
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results = get();
                    showAllLanguageCodeMetricsResult(results, directory, mode);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        gui, 
                        "分析过程中发生错误：" + ex.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * 显示所有语言的代码量统计结果
     */
    private void showAllLanguageCodeMetricsResult(Map<CodeStatsCore.Language, CodeStatsCore.AnalyzeResult> results, File directory, int mode) {
        // 创建结果对话框
        JDialog resultDialog = new JDialog(gui, "所有语言代码统计结果", true);
        resultDialog.setLayout(new BorderLayout());
        resultDialog.setSize(1000, 700);
        resultDialog.setLocationRelativeTo(gui);
        
        // 创建表格显示结果
        String[] columnNames = {"语言", "源文件数", "代码行数", "空行数", "注释行数", "函数个数", "最大值", "最小值", "均值", "中位数"};
        Object[][] data = new Object[results.size() + 1][10]; // +1 for 总计 row
        
        int index = 0;
        int totalFiles = 0;
        int totalCodeLines = 0;
        int totalBlankLines = 0;
        int totalCommentLines = 0;
        int totalFunctions = 0;
        int maxFunctionLength = 0;
        int minFunctionLength = Integer.MAX_VALUE;
        double totalMean = 0;
        double totalMedian = 0;
        
        // 按指定顺序填充表格数据
        CodeStatsCore.Language[] languages = {
            CodeStatsCore.Language.C,
            CodeStatsCore.Language.CPP,
            CodeStatsCore.Language.JAVA,
            CodeStatsCore.Language.PYTHON,
            CodeStatsCore.Language.CSHARP
        };
        
        for (CodeStatsCore.Language language : languages) {
            CodeStatsCore.AnalyzeResult result = results.get(language);
            
            data[index][0] = language.toString();
            
            if (result.codeMetrics != null) {
                CodeStatsCore.CodeMetrics metrics = result.codeMetrics;
                data[index][1] = metrics.fileCount;
                data[index][2] = metrics.codeLines;
                data[index][3] = metrics.blankLines;
                data[index][4] = metrics.commentLines;
                
                // 累计总计
                totalFiles += metrics.fileCount;
                totalCodeLines += metrics.codeLines;
                totalBlankLines += metrics.blankLines;
                totalCommentLines += metrics.commentLines;
            } else {
                data[index][1] = "/";
                data[index][2] = "/";
                data[index][3] = "/";
                data[index][4] = "/";
            }
            
            if (result.summary != null) {
                data[index][5] = result.summary.count;
                data[index][6] = result.summary.max;
                data[index][7] = result.summary.min;
                data[index][8] = String.format("%.2f", result.summary.mean);
                data[index][9] = String.format("%.2f", result.summary.median);
                
                // 累计总计
                totalFunctions += result.summary.count;
                maxFunctionLength = Math.max(maxFunctionLength, result.summary.max);
                minFunctionLength = Math.min(minFunctionLength, result.summary.min);
                totalMean += result.summary.mean;
                totalMedian += result.summary.median;
            } else {
                data[index][5] = "/";
                data[index][6] = "/";
                data[index][7] = "/";
                data[index][8] = "/";
                data[index][9] = "/";
            }
            
            index++;
        }
        
        // 添加总计行
        data[index][0] = "总计";
        // 根据模式决定显示哪些统计数据：
        // - 在纯代码量模式(MODE_CODE_METRICS)下，只显示代码量相关数据
        // - 在纯函数长度模式(MODE_FUNCTION_LENGTH)下，只显示函数相关数据  
        // - 在都统计模式(MODE_BOTH)下，显示所有数据
        boolean showCodeMetrics = mode != CodeStatsService.MODE_FUNCTION_LENGTH;
        boolean showFunctionMetrics = mode != CodeStatsService.MODE_CODE_METRICS;
        
        data[index][1] = showCodeMetrics ? String.valueOf(totalFiles) : "/";
        data[index][2] = showCodeMetrics ? String.valueOf(totalCodeLines) : "/";
        data[index][3] = showCodeMetrics ? String.valueOf(totalBlankLines) : "/";
        data[index][4] = showCodeMetrics ? String.valueOf(totalCommentLines) : "/";
        data[index][5] = showFunctionMetrics ? String.valueOf(totalFunctions) : "/";
        data[index][6] = showFunctionMetrics ? String.valueOf(maxFunctionLength) : "/";
        data[index][7] = showFunctionMetrics ? String.valueOf(minFunctionLength == Integer.MAX_VALUE ? 0 : minFunctionLength) : "/";
        data[index][8] = showFunctionMetrics ? String.format("%.2f", totalFunctions > 0 ? totalMean / results.size() : 0) : "/";
        data[index][9] = showFunctionMetrics ? String.format("%.2f", totalFunctions > 0 ? totalMedian / results.size() : 0) : "/";
        
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false); // 禁止编辑
        JScrollPane scrollPane = new JScrollPane(table);
        
        // 添加标题和目录信息
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(new JLabel("目录: " + directory.getAbsolutePath()));
        headerPanel.add(new JLabel(" "));
        
        // 导出和关闭按钮
        JButton exportButton = new JButton("导出结果");
        exportButton.addActionListener(e -> gui.getExportHandler().exportAllLanguageResults(results, directory, mode));
        
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> resultDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        
        resultDialog.add(headerPanel, BorderLayout.NORTH);
        resultDialog.add(scrollPane, BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resultDialog.setVisible(true);
    }
    
    /**
     * 显示统计模式选择对话框
     * 三种模式：代码量统计、函数长度统计 或 都统计
     */
    private void showStatModeSelection(CodeStatsCore.Language language) {
        String[] options = {"代码量统计", "函数长度统计", "都统计"};
        int choice = JOptionPane.showOptionDialog(
            gui,
            "请选择统计模式：\n\n" +
            "• 代码量统计：统计文件数、代码行数、注释行数等\n" +
            "• 函数长度统计：统计函数的均值、最大值、最小值、中位数\n" +
            "• 都统计：同时进行代码量和函数长度统计",
            "统计模式选择",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != JOptionPane.CLOSED_OPTION) {
            int mode = switch (choice) {
                case 0 -> CodeStatsService.MODE_CODE_METRICS;
                case 1 -> CodeStatsService.MODE_FUNCTION_LENGTH;
                case 2 -> CodeStatsService.MODE_BOTH;
                default -> CodeStatsService.MODE_CODE_METRICS;
            };
            showFileSelectionDialog(language, mode);
        }
    }
    
    /**
     * 显示文件选择对话框
     */
    private void showFileSelectionDialog(CodeStatsCore.Language language, int mode) {
        // 创建文件选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择 " + language + " 文件或目录");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        // 设置文件过滤器
        javax.swing.filechooser.FileFilter filter = createFileFilter(language);
        if (filter != null) {
            fileChooser.setFileFilter(filter);
        }
        
        int result = fileChooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            performCodeAnalysis(language, selectedFile, mode);
        }
    }
    
    /**
     * 创建文件过滤器
     */
    private javax.swing.filechooser.FileFilter createFileFilter(CodeStatsCore.Language language) {
        return switch (language) {
            case JAVA -> new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".java");
                }
                @Override
                public String getDescription() {
                    return "Java 文件 (*.java)";
                }
            };
            case PYTHON -> new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".py");
                }
                @Override
                public String getDescription() {
                    return "Python 文件 (*.py)";
                }
            };
            case C -> new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".c") || f.getName().endsWith(".h");
                }
                @Override
                public String getDescription() {
                    return "C 文件 (*.c, *.h)";
                }
            };
            case CPP -> new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".cpp") || 
                           f.getName().endsWith(".hpp") || f.getName().endsWith(".cc") ||
                           f.getName().endsWith(".cxx") || f.getName().endsWith(".hxx");
                }
                @Override
                public String getDescription() {
                    return "C++ 文件 (*.cpp, *.hpp, *.cc, *.cxx)";
                }
            };
            case CSHARP -> new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".cs");
                }
                @Override
                public String getDescription() {
                    return "C# 文件 (*.cs)";
                }
            };
            // 添加 default 分支来处理未预期的情况
            default -> new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "所有文件";
                }
            };
        };
    }
    
    /**
     * 执行代码分析
     * 根据模式显示不同的统计结果
     */
    private void performCodeAnalysis(CodeStatsCore.Language language, File file, int mode) {
        // 创建进度对话框
        JDialog progressDialog = new JDialog(gui, "分析中", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(new JLabel("正在分析代码，请稍候..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(gui);
        
        // 在后台线程执行分析
        SwingWorker<CodeStatsCore.AnalyzeResult, Void> worker = 
            new SwingWorker<CodeStatsCore.AnalyzeResult, Void>() {
            
            @Override
            protected CodeStatsCore.AnalyzeResult doInBackground() throws Exception {
                CodeStatsService service = new CodeStatsService();
                CodeStatsService.AnalyzeRequest request = new CodeStatsService.AnalyzeRequest();
                request.language = language;
                request.paths = List.of(file.getAbsolutePath());
                request.mode = mode;
                
                return service.analyze(request);
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    CodeStatsCore.AnalyzeResult res = get();
                    
                    if (mode == CodeStatsService.MODE_CODE_METRICS) {
                        showCodeMetricsResult(res);
                    } else if (mode == CodeStatsService.MODE_FUNCTION_LENGTH) {
                        showFunctionLengthResult(res);
                    } else {
                        showBothResult(res);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        gui, 
                        "分析过程中发生错误：" + ex.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * 显示代码量统计结果
     */
    private void showCodeMetricsResult(CodeStatsCore.AnalyzeResult result) {
        if (result.codeMetrics == null) {
            JOptionPane.showMessageDialog(gui, 
                "未找到任何文件！", 
                "分析结果", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CodeStatsCore.CodeMetrics metrics = result.codeMetrics;
        String message = String.format(
            "代码量统计结果:\n\n" +
            "文件数量: %d\n" +
            "总行数: %d\n" +
            "代码行数: %d\n" +
            "注释行数: %d\n" +
            "空行数: %d\n\n" +
            "代码占比: %.1f%%\n" +
            "注释占比: %.1f%%",
            metrics.fileCount,
            metrics.totalLines,
            metrics.codeLines,
            metrics.commentLines,
            metrics.blankLines,
            metrics.totalLines > 0 ? (metrics.codeLines * 100.0 / metrics.totalLines) : 0,
            metrics.totalLines > 0 ? (metrics.commentLines * 100.0 / metrics.totalLines) : 0
        );
        
        JOptionPane.showMessageDialog(gui, 
            message, 
            "代码量统计结果", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示函数长度统计结果（带图表）
     */
    private void showFunctionLengthResult(CodeStatsCore.AnalyzeResult result) {
        if (result.summary == null || result.summary.count == 0) {
            JOptionPane.showMessageDialog(gui, 
                "未找到任何函数或方法！", 
                "分析结果", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 创建结果对话框
        JDialog resultDialog = new JDialog(gui, "函数长度统计结果", true);
        resultDialog.setLayout(new BorderLayout(10, 10));
        resultDialog.setSize(800, 600);
        resultDialog.setLocationRelativeTo(gui);
        
        // 统计信息面板
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel(String.format("函数数量: %d", result.summary.count)));
        infoPanel.add(new JLabel(String.format("平均长度: %.2f 行", result.summary.mean)));
        infoPanel.add(new JLabel(String.format("最大长度: %d 行", result.summary.max)));
        infoPanel.add(new JLabel(String.format("最小长度: %d 行", result.summary.min)));
        infoPanel.add(new JLabel(String.format("中位数: %.2f 行", result.summary.median)));
        
        // 图表面板
        JTabbedPane chartTabbedPane = new JTabbedPane();
        
        // 收集所有函数长度
        List<Integer> lengths = new ArrayList<>();
        for (CodeStatsCore.FunctionStat func : result.functions) {
            lengths.add(func.length);
        }
        
        // 柱状图
        ChartPanel barChart = new ChartPanel(lengths, "bar", "函数长度分布（柱状图）");
        chartTabbedPane.addTab("柱状图", barChart);
        
        // 饼图
        ChartPanel pieChart = new ChartPanel(lengths, "pie", "函数长度分布（饼图）");
        chartTabbedPane.addTab("饼图", pieChart);
        
        // 导出和关闭按钮
        JButton exportButton = new JButton("导出结果");
        exportButton.addActionListener(e -> gui.getExportHandler().exportFunctionResults(result));
        
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> resultDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        
        resultDialog.add(infoPanel, BorderLayout.NORTH);
        resultDialog.add(chartTabbedPane, BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resultDialog.setVisible(true);
    }
    
    /**
     * 显示代码量和函数长度统计结果（都统计）
     */
    private void showBothResult(CodeStatsCore.AnalyzeResult result) {
        // 创建结果对话框
        JDialog resultDialog = new JDialog(gui, "代码量和函数长度统计结果", true);
        resultDialog.setLayout(new BorderLayout());
        resultDialog.setSize(1000, 700);
        resultDialog.setLocationRelativeTo(gui);
        
        // 创建表格显示结果
        String[] columnNames = {"语言", "源文件数", "代码行数", "空行数", "注释行数", "函数个数", "最大值", "最小值", "均值", "中位数"};
        Object[][] data = new Object[1][10];
        
        data[0][0] = "当前语言";
        
        if (result.codeMetrics != null) {
            CodeStatsCore.CodeMetrics metrics = result.codeMetrics;
            data[0][1] = metrics.fileCount;
            data[0][2] = metrics.codeLines;
            data[0][3] = metrics.blankLines;
            data[0][4] = metrics.commentLines;
        } else {
            data[0][1] = "/";
            data[0][2] = "/";
            data[0][3] = "/";
            data[0][4] = "/";
        }
        
        if (result.summary != null) {
            data[0][5] = result.summary.count;
            data[0][6] = result.summary.max;
            data[0][7] = result.summary.min;
            data[0][8] = String.format("%.2f", result.summary.mean);
            data[0][9] = String.format("%.2f", result.summary.median);
        } else {
            data[0][5] = "/";
            data[0][6] = "/";
            data[0][7] = "/";
            data[0][8] = "/";
            data[0][9] = "/";
        }
        
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false); // 禁止编辑
        JScrollPane scrollPane = new JScrollPane(table);
        
        // 导出和关闭按钮
        JButton exportButton = new JButton("导出结果");
        exportButton.addActionListener(e -> gui.getExportHandler().exportFunctionResults(result));
        
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> resultDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        
        resultDialog.add(scrollPane, BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resultDialog.setVisible(true);
    }
}