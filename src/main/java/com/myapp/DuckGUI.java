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

import com.myapp.rollcall.ui.RollCallGUI;
import com.myapp.duckbehavior.DuckBehaviorService;
import com.myapp.duckbehavior.DuckRole;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;

/**
 * 唐老鸭和小鸭子应用主界面
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
        
        // ⚠️脆鼠修改：创建三只小鸭子，添加点击交互功能
        String[] duckNames = {"小鸭1号", "小鸭2号", "小鸭3号"};
        for (int i = 0; i < duckNames.length; i++) {
            DuckComponent duck = new DuckComponent(duckNames[i], false);
            duck.setCursor(new Cursor(Cursor.HAND_CURSOR));
            duck.setToolTipText("点击我听叫声看动作！");
            final int index = i; // 保存索引用于事件处理
            
            // ⚠️脆鼠修改：添加小鸭子点击事件处理 - 声音播放 + 动画效果
            duck.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // ⚠️脆鼠修改：处理小鸭子点击 - 调用后端服务获取行为和声音
                    handleDuckClick(duck, duckNames[index]);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    // ⚠️脆鼠修改：鼠标悬停效果 - 添加发光边框提示可交互
                    duck.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 3));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    // ⚠️脆鼠修改：鼠标离开效果 - 移除边框，保持选中状态
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
            "• 输入\"点名\"开始点名",
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
     * ⚠️老鼠修改
     * 处理用户请求
     * 根据关键词识别用户意图并调用相应功能
     * 采用策略模式处理不同类型的用户请求
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
        // ⚠️老鼠修改 - 检测点名需求
        else if (request.contains("点名")) {
            startRollCallSystem();
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
     * ⚠️老鼠修改
     * 启动点名系统
     * 打开点名系统的主界面
     */
    private void startRollCallSystem() {
        try {
            RollCallGUI rollCallGUI = new RollCallGUI(this);
            rollCallGUI.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "启动点名系统失败：" + e.getMessage(), 
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
    
    /**
     * ⚠️脆鼠修改：处理小鸭子点击事件 - 核心交互功能
     * 集成后端行为服务，实现声音播放和动画效果，然后进入换装
     * 采用异步处理确保UI响应性
     * 
     * @param duck 被点击的小鸭子组件
     * @param duckName 小鸭子名称（用于显示信息）
     */
    private void handleDuckClick(DuckComponent duck, String duckName) {
        // ⚠️脆鼠修改：使用SwingWorker进行异步处理 - 软工思想：异步编程模式
        // 好处：避免UI阻塞，提升用户体验
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // ⚠️脆鼠修改：调用后端服务获取鸭子行为 - 软工思想：分层架构
                    // 好处：前后端分离，后端负责业务逻辑，前端负责展示
                    DuckBehaviorService behaviorService = new DuckBehaviorService();
                    DuckBehaviorService.DuckBehavior behavior = behaviorService.getBehavior(DuckRole.DUCKLING);
                    
                    // ⚠️脆鼠修改：根据声音类型设置情绪 - 软工思想：状态同步
                    // 好处：声音和情绪状态保持一致，提升用户体验
                    String emotionType = getEmotionFromSound(behavior.getSound());
                    duck.setEmotion(emotionType);
                    
                    // ⚠️脆鼠修改：播放声音 - 软工思想：多线程处理
                    // 好处：声音播放不阻塞UI线程
                    playDuckSound(behavior);
                    
                    // ⚠️脆鼠修改：执行动画 - 软工思想：状态机模式
                    // 好处：动画状态管理，确保动画流畅
                    executeDuckAnimation(duck, behavior.getAction());
                    
                    // ⚠️脆鼠修改：等待动画完成后询问是否换装 - 软工思想：用户体验设计
                    // 好处：动画作为过渡效果，然后询问用户是否换装
                    Thread.sleep(1000); // 等待动画完成
                    
                    // ⚠️脆鼠修改：询问是否要进行换装 - 软工思想：用户决策权
                    // 好处：给予用户选择权，提升用户体验
                    SwingUtilities.invokeLater(() -> {
                        // 设置当前选中的小鸭子
                        if (selectedDuck != null) {
                            selectedDuck.setSelected(false);
                        }
                        duck.setSelected(true);
                        selectedDuck = duck;
                        
                        // 显示询问对话框
                        int option = JOptionPane.showConfirmDialog(
                                DuckGUI.this,
                                "是否要进行换装？",
                                "小鸭子互动",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        
                        // 如果用户选择"是"，则进入换装界面
                        if (option == JOptionPane.YES_OPTION) {
                            showDressUpDialog(duck);
                        }
                    });
                    
                } catch (Exception e) {
                    // ⚠️脆鼠修改：异常处理 - 软工思想：防御性编程
                    // 好处：完善的异常处理，确保系统稳定性
                    System.err.println("处理小鸭子点击时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // ⚠️脆鼠修改：UI更新完成 - 软工思想：EDT线程安全
                // 好处：确保UI更新在事件分发线程中执行
                try {
                    get(); // 检查是否有异常
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DuckGUI.this,
                            "小鸭子互动失败：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * ⚠️脆鼠修改：播放鸭子声音 - 软工思想：音频处理模块
     * 
     * @param behavior 鸭子行为对象，包含声音文件路径
     */
    private void playDuckSound(DuckBehaviorService.DuckBehavior behavior) {
        try {
            // ⚠️脆鼠修改：获取声音文件路径 - 软工思想：资源管理
            // 好处：统一资源路径管理，便于维护
            String soundPath = behavior.getSoundWavPath();
            
            // ⚠️脆鼠修改：检查音频文件存在性 - 软工思想：防御性编程
            // 好处：避免文件不存在导致的异常
            java.net.URL audioUrl = getClass().getResource(soundPath);
            if (audioUrl == null) {
                System.err.println("音频文件未找到: " + soundPath);
                return;
            }
            
            // ⚠️脆鼠修改：使用Java Sound API播放音频 - 软工思想：标准API使用
            // 好处：使用Java标准音频API，确保跨平台兼容性
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioUrl);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            Clip audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);
            
            // ⚠️脆鼠修改：音频播放控制 - 软工思想：资源管理
            // 好处：自动资源清理，防止内存泄漏
            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    audioClip.close();
                }
                if (event.getType() == LineEvent.Type.CLOSE) {
                    try {
                        audioStream.close();
                    } catch (IOException e) {
                        System.err.println("关闭音频流时发生错误: " + e.getMessage());
                    }
                }
            });
            
            // ⚠️脆鼠修改：开始播放音频
            audioClip.start();
            
            // ⚠️脆鼠修改：2秒后停止播放声音
            // 好处：限制声音播放时长，避免过长影响用户体验
            Timer timer = new Timer(2000, e -> {
                if (audioClip.isActive()) {
                    audioClip.stop();
                    audioClip.close();
                }
            });
            timer.setRepeats(false);
            timer.start();
            
        } catch (Exception e) {
            // ⚠️脆鼠修改：音频播放异常处理 - 软工思想：优雅降级
            // 好处：音频播放失败不影响其他功能
            System.err.println("播放鸭子声音时发生错误: " + e.getMessage());
            // 可以考虑添加备用的声音提示或视觉反馈
        }
    }
    
    /**
     * ⚠️脆鼠修改：执行鸭子动画 - 软工思想：动画引擎设计
     * 
     * @param duck 鸭子组件
     * @param action 动作类型
     */
    private void executeDuckAnimation(DuckComponent duck, com.myapp.duckbehavior.DuckAction action) {
        // ⚠️脆鼠修改：在EDT线程中执行动画 - 软工思想：线程安全
        // 好处：确保UI组件的线程安全访问
        SwingUtilities.invokeLater(() -> {
            try {
                // ⚠️脆鼠修改：根据动作类型执行不同动画 - 软工思想：策略模式
                // 好处：每种动作对应特定的动画实现，便于扩展
                switch (action) {
                    case SHAKE:
                        executeShakeAnimation(duck);
                        break;
                    case HOP:
                        executeHopAnimation(duck);
                        break;
                    case SPIN:
                        executeSpinAnimation(duck);
                        break;
                    case WAVE:
                        executeWaveAnimation(duck);
                        break;
                    default:
                        System.out.println("未实现的动作类型: " + action.getText());
                        break;
                }
            } catch (Exception e) {
                System.err.println("执行鸭子动画时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * ⚠️脆鼠修改：执行摇晃动画 - 软工思想：动画算法实现
     * 
     * @param duck 鸭子组件
     */
    private void executeShakeAnimation(DuckComponent duck) {
        // ⚠️脆鼠修改：使用Timer实现动画 - 软工思想：定时器模式
        // 好处：使用Swing Timer确保动画的平滑性和线程安全
        final int[] shakeCount = {0};
        final int maxShakes = 6; // 摇晃次数
        final int originalX = duck.getX();
        final int originalY = duck.getY();
        
        Timer shakeTimer = new Timer(50, e -> {
            int offset = (shakeCount[0] % 2 == 0) ? 5 : -5; // 左右交替偏移
            duck.setLocation(originalX + offset, originalY);
            shakeCount[0]++;
            
            if (shakeCount[0] >= maxShakes) {
                // ⚠️脆鼠修改：动画结束恢复原位
                duck.setLocation(originalX, originalY);
                ((Timer) e.getSource()).stop();
            }
        });
        
        shakeTimer.start();
    }
    
    /**
     * ⚠️脆鼠修改：执行跳跃动画 - 软工思想：物理动画模拟
     * 
     * @param duck 鸭子组件
     */
    private void executeHopAnimation(DuckComponent duck) {
        final int originalY = duck.getY();
        final int hopHeight = 20; // 跳跃高度
        final int[] hopPhase = {0}; // 0=上升，1=下降
        final int[] hopCount = {0};
        final int maxHops = 6; // 动画帧数
        
        Timer hopTimer = new Timer(30, e -> {
            if (hopPhase[0] == 0) {
                // ⚠️脆鼠修改：上升阶段
                int newY = originalY - (hopHeight * hopCount[0] / 3);
                duck.setLocation(duck.getX(), newY);
                
                if (hopCount[0] >= 3) {
                    hopPhase[0] = 1; // 切换到下降阶段
                }
            } else {
                // ⚠️脆鼠修改：下降阶段
                int newY = originalY - hopHeight + (hopHeight * (hopCount[0] - 3) / 3);
                duck.setLocation(duck.getX(), newY);
            }
            
            hopCount[0]++;
            
            if (hopCount[0] >= maxHops) {
                // ⚠️脆鼠修改：动画结束恢复原位
                duck.setLocation(duck.getX(), originalY);
                ((Timer) e.getSource()).stop();
            }
        });
        
        hopTimer.start();
    }
    
    /**
     * ⚠️脆鼠修改：执行旋转动画 - 软工思想：旋转变换
     * 
     * @param duck 鸭子组件
     */
    private void executeSpinAnimation(DuckComponent duck) {
        final double[] rotationAngle = {0.0};
        final double rotationStep = 15.0; // 每帧旋转角度
        final int maxFrames = 24; // 旋转360度需要的帧数
        
        Timer spinTimer = new Timer(20, e -> {
            // ⚠️脆鼠修改：使用AffineTransform实现旋转 - 软工思想：2D图形变换
            // 好处：使用标准的2D变换API，确保旋转效果平滑
            rotationAngle[0] += rotationStep;
            
            // ⚠️脆鼠修改：创建旋转变换
            AffineTransform transform = new AffineTransform();
            transform.rotate(Math.toRadians(rotationAngle[0]), 
                          duck.getWidth() / 2.0, 
                          duck.getHeight() / 2.0);
            
            // ⚠️脆鼠修改：应用变换到鸭子组件
            // 注意：这里简化实现，实际可能需要更复杂的图形处理
            duck.repaint(); // 触发重绘，在paintComponent中应用变换
            
            if (rotationAngle[0] >= 360.0) {
                // ⚠️脆鼠修改：动画结束恢复原始状态
                rotationAngle[0] = 0.0;
                ((Timer) e.getSource()).stop();
            }
        });
        
        spinTimer.start();
    }
    
    /**
     * ⚠️脆鼠修改：执行挥手动画 - 软工思想：复合动画
     * 
     * @param duck 鸭子组件
     */
    private void executeWaveAnimation(DuckComponent duck) {
        // ⚠️脆鼠修改：挥手动画 = 小幅摇晃 + 视觉提示
        executeShakeAnimation(duck);
        
        // ⚠️脆鼠修改：添加视觉提示
        SwingUtilities.invokeLater(() -> {
            // 可以在这里添加特殊的视觉效果，比如星星、音符等
            duck.repaint();
        });
    }
    
    /**
     * ⚠️脆鼠修改：从声音类型获取情绪 - 软工思想：状态映射
     * 好处：建立声音和情绪的映射关系，确保状态一致性
     * 
     * @param sound 鸭子声音
     * @return 对应的情绪类型：happy/sad/confident
     */
    private String getEmotionFromSound(com.myapp.duckbehavior.DuckSound sound) {
        // ⚠️脆鼠修改：根据声音枚举映射情绪 - 软工思想：策略模式
        // 好处：统一的状态映射逻辑，便于维护
        if (sound == com.myapp.duckbehavior.DuckSound.DUCKLING_HAPPY) {
            return "happy";
        } else if (sound == com.myapp.duckbehavior.DuckSound.DUCKLING_SAD) {
            return "sad";
        } else if (sound == com.myapp.duckbehavior.DuckSound.DUCKLING_CONFIDENT) {
            return "confident";
        }
        // ⚠️脆鼠修改：默认返回开心状态 - 软工思想：防御性编程
        // 好处：确保总有返回值，避免空指针异常
        return "happy";
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
