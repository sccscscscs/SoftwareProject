package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * 唐老鸭和小鸭子应用主界面
 * 
 * 功能说明：
 * 1. 点击唐老鸭：弹出对话框，用户可以输入需求
 *    - 输入包含"代码量"：进入代码统计功能（支持Java/Python/C/C++/C#）
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
    private CodeStatsHandler codeStatsHandler;
    private ExportHandler exportHandler;
    private DuckComponent selectedDuck = null; // 当前选中的小鸭子
    
    public DuckGUI() {
        setTitle("🦆 唐老鸭和小鸭子 - 多功能应用");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 初始化服务
        aiService = new AIService();
        codeStatsHandler = new CodeStatsHandler(this);
        exportHandler = new ExportHandler(this);
        
        initUI();
    }
    
    public ExportHandler getExportHandler() {
        return exportHandler;
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
                    0, 0, new Color(175, 220, 255),
                    0, h, new Color(255, 240, 200)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, w, h);
                
                // 绘制云朵装饰
                g2d.setColor(new Color(255, 255, 255, 180));
                for (int i = 0; i < 3; i++) {
                    int x = 100 + i * 300;
                    int y = 50 + (i % 2) * 30;
                    g2d.fillOval(x, y, 60, 30);
                    g2d.fillOval(x + 10, y - 10, 40, 40);
                    g2d.fillOval(x + 30, y - 5, 50, 35);
                }
                
                // 绘制草地
                g2d.setColor(new Color(150, 220, 100, 120));
                int grassHeight = 80;
                g2d.fillRoundRect(0, h - grassHeight, w, grassHeight, 0, 0);
                
                // 绘制草叶细节
                g2d.setColor(new Color(120, 200, 80));
                g2d.setStroke(new BasicStroke(1));
                for (int i = 0; i < w; i += 15) {
                    int height = 10 + (i % 3) * 5;
                    g2d.drawLine(i, h - grassHeight, i, h - grassHeight - height);
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
                g2d.setColor(new Color(100, 100, 100));
                String subtitle = "点击唐老鸭体验功能 | 点击小鸭子换装打扮";
                int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
                g2d.drawString(subtitle, (w - subWidth) / 2, 75);
            }
        };
        
        // 使用GridBagLayout实现自适应布局
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 创建唐老鸭
        DuckComponent donaldDuck = new DuckComponent("唐老鸭", true);
        donaldDuck.setCursor(new Cursor(Cursor.HAND_CURSOR));
        donaldDuck.setToolTipText("点击我可以进行代码统计、玩红包雨游戏、AI对话！");
        donaldDuck.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 唐老鸭点击直接显示输入对话框
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
        
        // 设置唐老鸭约束
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 50); // 右边距
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(donaldDuck, gbc);
        ducks.add(donaldDuck);
        
        // 创建一个小鸭子容器面板，用于水平排列三只小鸭子
        JPanel duckRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 将间距从30减小到10
        duckRowPanel.setOpaque(false);
        
        // 创建三只小鸭子
        String[] duckNames = {"小鸭1号", "小鸭2号", "小鸭3号"};
        for (int i = 0; i < duckNames.length; i++) {
            DuckComponent duck = new DuckComponent(duckNames[i], false);
            duck.setCursor(new Cursor(Cursor.HAND_CURSOR));
            duck.setToolTipText("点击我可以换装打扮！");
            final int index = i; // 保存索引用于事件处理
            duck.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 取消之前选中鸭子的选中状态
                    if (selectedDuck != null) {
                        selectedDuck.setSelected(false);
                    }
                    // 设置当前鸭子为选中状态
                    duck.setSelected(true);
                    selectedDuck = duck;
                    
                    // 显示换装对话框
                    showDressUpDialog(duck);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    duck.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 3));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedDuck != duck) { // 如果不是当前选中的鸭子才移除边框
                        duck.setBorder(null);
                    }
                }
            });
            duckRowPanel.add(duck);
            ducks.add(duck);
        }
        
        // 设置小鸭子行约束
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(duckRowPanel, gbc);
        
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
        
        // 美化对话框
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        Object[] message = {
            "请输入您的需求：",
            "• 输入\"代码量\"进行代码统计",
            "• 输入\"红包雨\"开始游戏",
            "• 输入其他内容进行AI对话",
            scrollPane
        };
        
        // 加载唐老鸭头像作为图标
        Icon donaldIcon = null;
        try {
            BufferedImage originalImage = ImageIO.read(getClass().getResource("/images/largeduck.png"));
            // 裁剪出头部区域（根据图片实际比例调整）
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            BufferedImage headImage = originalImage.getSubimage(
                width / 4,  // x偏移
                height / 8, // y偏移
                width / 2,  // 宽度
                height / 3  // 高度
            );
            // 缩放到合适大小
            Image scaledImage = headImage.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            donaldIcon = new ImageIcon(scaledImage);
        } catch (IOException e) {
            // 如果加载失败，使用默认图标
            System.err.println("加载唐老鸭头像失败: " + e.getMessage());
            donaldIcon = UIManager.getIcon("OptionPane.questionIcon");
        }
        
        // 创建自定义对话框
        JOptionPane optionPane = new JOptionPane(
            message, 
            JOptionPane.QUESTION_MESSAGE, 
            JOptionPane.OK_CANCEL_OPTION,
            donaldIcon // 使用唐老鸭头像作为图标
        );
        
        JDialog dialog = optionPane.createDialog(this, "唐老鸭对话框");
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        // 美化对话框
        dialog.setBackground(new Color(240, 248, 255));
        
        dialog.setVisible(true);
        
        Object value = optionPane.getValue();
        if (value != null && value.equals(JOptionPane.OK_OPTION)) {
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
            codeStatsHandler.showLanguageSelection();
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
        
        // 美化进度对话框
        JLabel progressLabel = new JLabel("AI正在思考，请稍候...", JLabel.CENTER);
        progressLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        progressDialog.add(progressPanel);
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
                    responseArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    responseArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    JScrollPane scrollPane = new JScrollPane(responseArea);
                    scrollPane.setPreferredSize(new Dimension(500, 300));
                    
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
     * 显示换装对话框
     * 使用分类衣柜界面让用户选择服装和配饰
     */
    private void showDressUpDialog(DuckComponent duck) {
        JDialog wardrobeDialog = new JDialog(this, "给 " + duck.getName() + " 换装", true);
        wardrobeDialog.setLayout(new BorderLayout(10, 10));
        wardrobeDialog.setSize(500, 500);
        wardrobeDialog.setLocationRelativeTo(this);
        
        // 衣柜标题
        JLabel titleLabel = new JLabel("👗 时尚衣柜 👗", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // 上衣面板
        JPanel topPanel = createClothingPanel(duck, new String[][]{
            {"T恤", "👕"}, {"衬衫", "👔"}, {"西装", "🤵"}, 
            {"雨衣", "🌧️"}, {"羽绒服", "🥼"}
        }, "top");
        tabbedPane.addTab("上衣", topPanel);
        
        // 下装面板
        JPanel bottomPanel = createClothingPanel(duck, new String[][]{
            {"牛仔短裤", "🩳"}, {"休闲长裤", "👖"}, {"百褶裙", "👗"}, 
            {"工装裤", "👖"}, {"运动裤", "🏃‍♂️"}, {"旗袍", "👘"}
        }, "bottom");
        tabbedPane.addTab("下装", bottomPanel);
        
        // 鞋子面板
        JPanel shoesPanel = createClothingPanel(duck, new String[][]{
            {"跑鞋", "👟"}, {"高跟鞋", "👠"}, {"雪地靴", "👢"}, 
            {"拖鞋", "👡"}, {"帆布鞋", "👟"}, {"登山鞋", "🥾"}
        }, "shoes");
        tabbedPane.addTab("鞋子", shoesPanel);
        
        // 配饰面板
        JPanel accessoriesPanel = createClothingPanel(duck, new String[][]{
            {"棒球帽", "🧢"}, {"太阳镜", "🕶️"}, {"围巾", "🧣"}, 
            {"手表", "⌚"}, {"背包", "🎒"}, {"耳机", "🎧"}
        }, "accessories");
        tabbedPane.addTab("配饰", accessoriesPanel);
        
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
        wardrobeDialog.add(tabbedPane, BorderLayout.CENTER);
        wardrobeDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        wardrobeDialog.setVisible(true);
    }
    
    /**
     * 创建服装面板
     */
    private JPanel createClothingPanel(DuckComponent duck, String[][] items, String category) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建服装选项网格
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        for (String[] item : items) {
            String itemName = item[0];
            String emoji = item[1];
            
            JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            JLabel itemLabel = new JLabel(emoji + " " + itemName);
            itemLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            
            JButton toggleButton = new JButton(
                duck.getClothing().contains(itemName) ? "✓ 已穿" : "穿上"
            );
            toggleButton.setFocusPainted(false);
            
            // 根据状态设置按钮颜色
            if (duck.getClothing().contains(itemName)) {
                toggleButton.setBackground(new Color(144, 238, 144));
            } else {
                toggleButton.setBackground(new Color(173, 216, 230));
            }
            
            toggleButton.addActionListener(e -> {
                if (duck.getClothing().contains(itemName)) {
                    duck.removeClothing(itemName);
                    toggleButton.setText("穿上");
                    toggleButton.setBackground(new Color(173, 216, 230));
                } else {
                    duck.addClothing(itemName);
                    toggleButton.setText("✓ 已穿");
                    toggleButton.setBackground(new Color(144, 238, 144));
                }
                duck.repaint();
            });
            
            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(toggleButton, BorderLayout.EAST);
            
            gridPanel.add(itemPanel);
        }
        
        panel.add(gridPanel, BorderLayout.CENTER);
        
        // 添加预览面板（支持上衣、下装、鞋子）
        if ("top".equals(category) || "bottom".equals(category) || "shoes".equals(category)) {
            JPanel previewPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // 绘制一个小的鸭子预览
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    
                    // 鸭子头部
                    g2d.setColor(new Color(255, 230, 100));
                    g2d.fillOval(centerX - 15, centerY - 25, 30, 30);
                    
                    // 鸭子眼睛
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(centerX - 8, centerY - 18, 5, 5);
                    g2d.fillOval(centerX + 3, centerY - 18, 5, 5);
                    
                    // 鸭子嘴巴
                    g2d.setColor(new Color(255, 140, 0));
                    int[] beakX = {centerX - 5, centerX + 5, centerX - 5};
                    int[] beakY = {centerY - 10, centerY - 10, centerY - 5};
                    g2d.fillPolygon(beakX, beakY, 3);
                    
                    // 鸭子身体
                    g2d.setColor(new Color(255, 230, 100));
                    g2d.fillOval(centerX - 20, centerY, 40, 40);
                    
                    // 根据类别和选中的服装绘制预览
                    for (String[] item : items) {
                        String itemName = item[0];
                        if (duck.getClothing().contains(itemName)) {
                            if ("top".equals(category)) {
                                // 绘制上衣预览
                                if (itemName.equals("T恤")) {
                                    g2d.setColor(new Color(220, 20, 60));
                                    g2d.fillRoundRect(centerX - 18, centerY + 5, 36, 25, 8, 8);
                                } else if (itemName.equals("衬衫")) {
                                    g2d.setColor(Color.WHITE);
                                    g2d.fillRoundRect(centerX - 18, centerY + 5, 36, 25, 8, 8);
                                    
                                    // 纽扣
                                    g2d.setColor(Color.YELLOW);
                                    for (int i = 0; i < 3; i++) {
                                        g2d.fillOval(centerX - 2, centerY + 8 + i * 6, 4, 4);
                                    }
                                } else if (itemName.equals("卫衣")) {
                                    g2d.setColor(new Color(255, 140, 0));
                                    g2d.fillRoundRect(centerX - 18, centerY + 5, 36, 25, 8, 8);
                                } else if (itemName.equals("西装")) {
                                    g2d.setColor(new Color(50, 50, 50));
                                    g2d.fillRoundRect(centerX - 18, centerY + 5, 36, 25, 8, 8);
                                } else if (itemName.equals("雨衣")) {
                                    g2d.setColor(new Color(173, 216, 230));
                                    g2d.fillRoundRect(centerX - 18, centerY + 5, 36, 25, 8, 8);
                                } else if (itemName.equals("羽绒服")) {
                                    g2d.setColor(new Color(255, 228, 196));
                                    g2d.fillRoundRect(centerX - 18, centerY + 5, 36, 25, 8, 8);
                                }
                            } 
                            else if ("bottom".equals(category)) {
                                // 绘制下装预览
                                if (itemName.equals("牛仔短裤")) {
                                    g2d.setColor(new Color(30, 144, 255));
                                    g2d.fillRect(centerX - 15, centerY + 20, 30, 15);
                                } else if (itemName.equals("休闲长裤")) {
                                    g2d.setColor(new Color(105, 105, 105));
                                    g2d.fillRect(centerX - 15, centerY + 20, 30, 25);
                                } else if (itemName.equals("百褶裙")) {
                                    g2d.setColor(new Color(255, 182, 193));
                                    g2d.fillRect(centerX - 15, centerY + 20, 30, 12);
                                    // 褶皱
                                    g2d.setColor(new Color(255, 105, 180));
                                    for (int i = 0; i < 5; i++) {
                                        g2d.drawLine(centerX - 12 + i*6, centerY + 20, centerX - 12 + i*6, centerY + 32);
                                    }
                                } else if (itemName.equals("工装裤")) {
                                    g2d.setColor(new Color(85, 107, 47));
                                    g2d.fillRect(centerX - 15, centerY + 20, 30, 25);
                                } else if (itemName.equals("运动裤")) {
                                    g2d.setColor(new Color(128, 128, 128));
                                    g2d.fillRect(centerX - 15, centerY + 20, 30, 25);
                                } else if (itemName.equals("旗袍")) {
                                    g2d.setColor(new Color(139, 0, 0));
                                    g2d.fillRect(centerX - 15, centerY + 20, 30, 20);
                                }
                            } 
                            else if ("shoes".equals(category)) {
                                // 绘制鞋子预览
                                if (itemName.equals("跑鞋")) {
                                    g2d.setColor(new Color(255, 255, 255));
                                    g2d.fillRoundRect(centerX - 18, centerY + 45, 36, 10, 8, 8);
                                    g2d.setColor(Color.RED);
                                    g2d.fillOval(centerX - 10, centerY + 50, 8, 8);
                                } else if (itemName.equals("高跟鞋")) {
                                    g2d.setColor(new Color(0, 0, 0));
                                    g2d.fillRoundRect(centerX - 15, centerY + 45, 30, 8, 6, 6);
                                    g2d.setColor(Color.GRAY);
                                    g2d.fillRect(centerX - 2, centerY + 53, 4, 12);
                                } else if (itemName.equals("雪地靴")) {
                                    g2d.setColor(new Color(255, 255, 255));
                                    g2d.fillRoundRect(centerX - 18, centerY + 45, 36, 12, 8, 8);
                                    g2d.setColor(new Color(139, 69, 19)); // 棕色
                                    g2d.fillRoundRect(centerX - 18, centerY + 45, 36, 6, 6, 6);
                                } else if (itemName.equals("拖鞋")) {
                                    g2d.setColor(new Color(255, 165, 0));
                                    g2d.fillRoundRect(centerX - 18, centerY + 45, 36, 8, 6, 6);
                                    g2d.setColor(Color.BLACK);
                                    g2d.fillOval(centerX, centerY + 48, 8, 8);
                                } else if (itemName.equals("帆布鞋")) {
                                    g2d.setColor(new Color(255, 255, 255));
                                    g2d.fillRoundRect(centerX - 18, centerY + 45, 36, 10, 8, 8);
                                    g2d.setColor(Color.BLUE);
                                    g2d.drawLine(centerX - 10, centerY + 50, centerX + 10, centerY + 50);
                                } else if (itemName.equals("登山鞋")) {
                                    g2d.setColor(new Color(101, 67, 33));
                                    g2d.fillRoundRect(centerX - 18, centerY + 45, 36, 12, 8, 8);
                                }
                            }
                            break; // 每个类别只绘制一件
                        }
                    }
                    
                    g2d.dispose();
                }
            };
            previewPanel.setPreferredSize(new Dimension(150, 150));
            previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
            panel.add(previewPanel, BorderLayout.SOUTH);
        }
        
        return panel;
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