package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 唐老鸭和小鸭子应用主界面
 * 
 * 功能说明：
 * 1. 点击唐老鸭：弹出对话框，用户可以输入需求
 *    - 输入包含"代码量"：进入代码统计功能（支持Java/Python/C/C++）
 *    - 输入包含"红包雨"：启动红包雨游戏
 *    - 其他输入：调用AI对话服务
 * 
 * 2. 点击小鸭子：给小鸭子换装（帽子、眼镜、围巾领带手杖等）
 * 
 * 设计模式：
 * - 使用装饰器模式实现小鸭子的配饰系统
 * - 使用策略模式处理不同的用户请求
 */
public class DuckGUI extends JFrame {
    private final List<DuckComponent> ducks = new ArrayList<>();
    private AIService aiService;
    
    public DuckGUI() {
        setTitle("🦆 唐老鸭和小鸭子 - 多功能应用");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 初始化AI服务
        aiService = new AIService();
        
        initUI();
    }
    
    private void initUI() {
        // 创建主面板
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // 清新的渐变背景
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(230, 240, 255),
                    0, h, new Color(255, 250, 240)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, w, h);
                
                // 绘制舞台地板
                g2d.setColor(new Color(210, 180, 140, 100));
                g2d.fillRoundRect(100, h - 150, w - 200, 120, 20, 20);
                
                // 地板光泽
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(120, h - 140, w - 240, 30, 15, 15);
                
                // 装饰性圆点
                g2d.setColor(new Color(100, 149, 237, 50));
                for (int i = 0; i < 5; i++) {
                    int x = 150 + i * 150;
                    int y = 80 + (i % 2) * 30;
                    g2d.fillOval(x, y, 40, 40);
                }
                
                // 标题
                g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
                g2d.setColor(new Color(70, 130, 180));
                String title = "🌟 时尚换装秀 🌟";
                FontMetrics fm = g2d.getFontMetrics();
                int titleWidth = fm.stringWidth(title);
                g2d.drawString(title, (w - titleWidth) / 2, 50);
                
                // 副标题
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
                g2d.setColor(new Color(120, 120, 120));
                String subtitle = "点击唐老鸭体验功能 | 点击小鸭子换装打扮";
                int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
                g2d.drawString(subtitle, (w - subWidth) / 2, 75);
            }
        };
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 120));
        
        // 创建唐老鸭
        DuckComponent donaldDuck = new DuckComponent("唐老鸭", true);
        donaldDuck.setCursor(new Cursor(Cursor.HAND_CURSOR));
        donaldDuck.setToolTipText("点击我可以进行代码统计、玩红包雨游戏、AI对话！");
        donaldDuck.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showInputDialog();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                donaldDuck.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 3));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                donaldDuck.setBorder(null);
            }
        });
        mainPanel.add(donaldDuck);
        ducks.add(donaldDuck);
        
        // 创建三只小鸭子
        String[] duckNames = {"小鸭1号", "小鸭2号", "小鸭3号"};
        for (int i = 0; i < duckNames.length; i++) {
            DuckComponent duck = new DuckComponent(duckNames[i], false);
            duck.setCursor(new Cursor(Cursor.HAND_CURSOR));
            duck.setToolTipText("点击我可以换装打扮！");
            duck.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDressUpDialog(duck);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    duck.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 3));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    duck.setBorder(null);
                }
            });
            mainPanel.add(duck);
            ducks.add(duck);
        }
        
        add(mainPanel);
    }
    
    /**
     * 显示唐老鸭的对话框
     * 用户可以在这里输入各种需求
     */
    private void showInputDialog() {
        JTextArea textArea = new JTextArea(3, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        Object[] message = {
            "请输入您的需求：",
            "• 输入\"代码量\"进行代码统计",
            "• 输入\"红包雨\"开始游戏",
            "• 输入其他内容进行AI对话",
            scrollPane
        };
        
        int option = JOptionPane.showConfirmDialog(
            this, 
            message, 
            "唐老鸭对话框", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (option == JOptionPane.OK_OPTION) {
            String input = textArea.getText().trim();
            if (!input.isEmpty()) {
                processUserRequest(input);
            }
        }
    }
    
    /**
     * 处理用户请求
     * 根据关键词识别用户意图并调用相应功能
     */
    private void processUserRequest(String request) {
        // 检测代码统计需求
        if (CodeStatsService.isCodeStatIntent(request)) {
            showLanguageSelection();
        } 
        // 检测红包雨需求
        else if (request.contains("红包雨") || request.contains("红包")) {
            startRedPacketRainGame();
        } 
        // 其他需求调用AI服务
        else {
            callAIService(request);
        }
    }
    
    /**
     * 启动红包雨游戏
     */
    private void startRedPacketRainGame() {
        try {
            RedPacketRainGame game = new RedPacketRainGame(this);
            game.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "启动红包雨游戏失败：" + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 调用AI服务
     */
    private void callAIService(String userMessage) {
        // 创建进度对话框
        JDialog progressDialog = new JDialog(this, "AI思考中", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(new JLabel("AI正在思考，请稍候..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        
        // 在后台线程调用AI服务
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return aiService.chat(userMessage);
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    String response = get();
                    
                    // 使用文本域显示AI回复
                    JTextArea responseArea = new JTextArea(response);
                    responseArea.setEditable(false);
                    responseArea.setLineWrap(true);
                    responseArea.setWrapStyleWord(true);
                    responseArea.setRows(15);
                    responseArea.setColumns(40);
                    
                    JScrollPane scrollPane = new JScrollPane(responseArea);
                    JOptionPane.showMessageDialog(
                        DuckGUI.this, 
                        scrollPane, 
                        "AI回复", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        DuckGUI.this, 
                        "获取AI回复失败：" + e.getMessage(), 
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
     * 显示语言选择对话框
     * 支持Java、Python、C、C++四种语言
     */
    private void showLanguageSelection() {
        String[] options = {"Java", "Python", "C", "C++"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "请选择编程语言：",
            "语言选择",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != JOptionPane.CLOSED_OPTION) {
            CodeStatsCore.Language language = switch (choice) {
                case 0 -> CodeStatsCore.Language.JAVA;
                case 1 -> CodeStatsCore.Language.PYTHON;
                case 2 -> CodeStatsCore.Language.C;
                case 3 -> CodeStatsCore.Language.CPP;
                default -> null;
            };
            
            if (language != null) {
                showStatModeSelection(language);
            }
        }
    }
    
    /**
     * 显示统计模式选择对话框
     * 两种模式：代码量统计 或 函数长度统计
     */
    private void showStatModeSelection(CodeStatsCore.Language language) {
        String[] options = {"代码量统计", "函数长度统计"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "请选择统计模式：\n\n" +
            "• 代码量统计：统计文件数、代码行数、注释行数\n" +
            "• 函数长度统计：统计函数的均值、最大值、最小值、中位数",
            "统计模式选择",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != JOptionPane.CLOSED_OPTION) {
            int mode = (choice == 0) ? 
                CodeStatsService.MODE_CODE_METRICS : 
                CodeStatsService.MODE_FUNCTION_LENGTH;
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
        
        int result = fileChooser.showOpenDialog(this);
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
        };
    }
    
    /**
     * 执行代码分析
     * 根据模式显示不同的统计结果
     */
    private void performCodeAnalysis(CodeStatsCore.Language language, File file, int mode) {
        // 创建进度对话框
        JDialog progressDialog = new JDialog(this, "分析中", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(new JLabel("正在分析代码，请稍候..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        
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
                    } else {
                        showFunctionLengthResult(res);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        DuckGUI.this, 
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
            JOptionPane.showMessageDialog(this, 
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
        
        JOptionPane.showMessageDialog(this, 
            message, 
            "代码量统计结果", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示函数长度统计结果（带图表）
     */
    private void showFunctionLengthResult(CodeStatsCore.AnalyzeResult result) {
        if (result.summary == null || result.summary.count == 0) {
            JOptionPane.showMessageDialog(this, 
                "未找到任何函数或方法！", 
                "分析结果", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 创建结果对话框
        JDialog resultDialog = new JDialog(this, "函数长度统计结果", true);
        resultDialog.setLayout(new BorderLayout(10, 10));
        resultDialog.setSize(800, 600);
        resultDialog.setLocationRelativeTo(this);
        
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
        
        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> resultDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        resultDialog.add(infoPanel, BorderLayout.NORTH);
        resultDialog.add(chartTabbedPane, BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resultDialog.setVisible(true);
    }
    
    /**
     * 显示换装对话框
     * 使用类似衣柜的界面让用户选择配饰
     */
    private void showDressUpDialog(DuckComponent duck) {
        JDialog wardrobeDialog = new JDialog(this, "给 " + duck.getName() + " 换装", true);
        wardrobeDialog.setLayout(new BorderLayout(10, 10));
        wardrobeDialog.setSize(450, 400);
        wardrobeDialog.setLocationRelativeTo(this);
        
        // 衣柜标题
        JLabel titleLabel = new JLabel("🎨 时尚衣柜 🎨", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        // 配饰选项面板
        JPanel accessoriesPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        accessoriesPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        String[] accessories = {"帽子", "眼镜", "领带", "拐杖", "西装"};
        String[] emojis = {"🎩", "👓", "👔", "🎋", "🤵"};
        
        for (int i = 0; i < accessories.length; i++) {
            String accessory = accessories[i];
            String emoji = emojis[i];
            
            JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            JLabel itemLabel = new JLabel(emoji + " " + accessory);
            itemLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            
            JButton toggleButton = new JButton(
                duck.getClothing().contains(accessory) ? "✓ 已穿" : "穿上"
            );
            toggleButton.setFocusPainted(false);
            
            // 根据状态设置按钮颜色
            if (duck.getClothing().contains(accessory)) {
                toggleButton.setBackground(new Color(144, 238, 144));
            } else {
                toggleButton.setBackground(new Color(173, 216, 230));
            }
            
            toggleButton.addActionListener(e -> {
                if (duck.getClothing().contains(accessory)) {
                    duck.removeClothing(accessory);
                    toggleButton.setText("穿上");
                    toggleButton.setBackground(new Color(173, 216, 230));
                } else {
                    duck.addClothing(accessory);
                    toggleButton.setText("✓ 已穿");
                    toggleButton.setBackground(new Color(144, 238, 144));
                }
                duck.repaint();
            });
            
            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(toggleButton, BorderLayout.EAST);
            
            accessoriesPanel.add(itemPanel);
        }
        
        // 完成按钮
        JButton doneButton = new JButton("✓ 完成换装");
        doneButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        doneButton.setBackground(new Color(100, 149, 237));
        doneButton.setForeground(Color.WHITE);
        doneButton.setFocusPainted(false);
        doneButton.addActionListener(e -> wardrobeDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(doneButton);
        
        wardrobeDialog.add(titleLabel, BorderLayout.NORTH);
        wardrobeDialog.add(accessoriesPanel, BorderLayout.CENTER);
        wardrobeDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        wardrobeDialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new DuckGUI().setVisible(true);
        });
    }
}