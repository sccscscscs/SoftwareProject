package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.myapp.rollcall.ui.RollCallGUI;
//唐老鸭和小鸭子应用主界面
public class DuckGUI extends JFrame {
    private final List<DuckComponent> ducks = new ArrayList<>();
    private AIService aiService;
    private CodeStatsHandler codeStatsHandler;
    private ExportHandler exportHandler;
    private DuckComponent selectedDuck = null; // 当前选中的小鸭子
    
    //复杂功能拆分到专门的类中，提高可维护性
    private DuckAnimationHandler animationHandler;
    private DuckSoundHandler soundHandler;
    private DuckEventHandler eventHandler;
    private WardrobeHandler wardrobeHandler;
    
    public DuckGUI() {
        setTitle("🦆 唐老鸭和小鸭子 - 多功能应用");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 初始化服务
        aiService = new AIService();
        codeStatsHandler = new CodeStatsHandler(this);
        exportHandler = new ExportHandler(this);
        
        //初始化新的处理器
        animationHandler = new DuckAnimationHandler(this);
        soundHandler = new DuckSoundHandler(this);
        wardrobeHandler = new WardrobeHandler(this);
        eventHandler = new DuckEventHandler(this, animationHandler, soundHandler);
        
        initUI();
    }
    
    public ExportHandler getExportHandler() {
        return exportHandler;
    }
    
    //添加getter方法
    public List<DuckComponent> getDucks() {
        return ducks;
    }
    
    public void setSelectedDuck(DuckComponent duck) {
        this.selectedDuck = duck;
    }
    
    //公开换装对话框方法
    public void showDressUpDialog(DuckComponent duck) {
        wardrobeHandler.showDressUpDialog(duck);
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
                
                //美化标题显示
                
                // 主标题背景
                g2d.setColor(new Color(255, 255, 255, 200));
                int titleBgWidth = 400;
                int titleBgHeight = 60;
                int titleBgX = (w - titleBgWidth) / 2;
                int titleBgY = 25;
                g2d.fillRoundRect(titleBgX, titleBgY, titleBgWidth, titleBgHeight, 20, 20);
                
                // 主标题边框
                g2d.setColor(new Color(70, 130, 180));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(titleBgX, titleBgY, titleBgWidth, titleBgHeight, 20, 20);
                
                // 主标题文字
                g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
                g2d.setColor(new Color(255, 140, 0));
                String title = "🌟 时尚换装秀 🌟";
                FontMetrics fm = g2d.getFontMetrics();
                int titleWidth = fm.stringWidth(title);
                g2d.drawString(title, (w - titleWidth) / 2, 60);
                
                // 副标题背景
                g2d.setColor(new Color(240, 248, 255, 180));
                int subtitleBgWidth = 450;
                int subtitleBgHeight = 35;
                int subtitleBgX = (w - subtitleBgWidth) / 2;
                int subtitleBgY = 95;
                g2d.fillRoundRect(subtitleBgX, subtitleBgY, subtitleBgWidth, subtitleBgHeight, 15, 15);
                
                // 副标题文字
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                g2d.setColor(new Color(70, 130, 180));
                String subtitle = "✨ 点击唐老鸭体验功能 ✨ 点击小鸭子换装打扮 ✨";
                int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
                g2d.drawString(subtitle, (w - subWidth) / 2, 115);
                
                //添加装饰性元素
                // 添加星星装饰
                g2d.setColor(new Color(255, 215, 0, 150));
                for (int i = 0; i < 8; i++) {
                    int starX = 50 + (i * 120);
                    int starY = 140 + (i % 2) * 20;
                    animationHandler.drawStar(g2d, starX, starY, 8);
                }
                
                // 添加彩虹装饰线
                Color[] rainbowColors = {
                    new Color(255, 0, 0, 100),      // 红色
                    new Color(255, 165, 0, 100),   // 橙色  
                    new Color(255, 255, 0, 100),   // 黄色
                    new Color(0, 255, 0, 100),     // 绿色
                    new Color(0, 127, 255, 100),   // 蓝色
                    new Color(75, 0, 130, 100)     // 紫色
                };
                
                int rainbowY = h - 100;
                for (int i = 0; i < rainbowColors.length; i++) {
                    g2d.setColor(rainbowColors[i]);
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawLine(50 + i * 150, rainbowY, 150 + i * 150, rainbowY);
                }
            }
        };
        
        // 使用GridBagLayout实现自适应布局
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();


        DuckComponent donaldDuck = new DuckComponent("唐老鸭", true);
        donaldDuck.setCursor(new Cursor(Cursor.HAND_CURSOR));
        donaldDuck.setToolTipText("点击我可以进行代码统计、玩红包雨游戏、AI对话！");
        
        // 设置处理器
        donaldDuck.setAnimationHandler(animationHandler);
        donaldDuck.setSoundHandler(soundHandler);
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
        
        //创建一个小鸭子容器面板，用于水平排列三只小鸭子
        JPanel duckRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        duckRowPanel.setOpaque(false);
        
        //创建三只小鸭子，添加点击交互功能
        String[] duckNames = {"小鸭1号", "小鸭2号", "小鸭3号"};
        for (int i = 0; i < duckNames.length; i++) {
            DuckComponent duck = new DuckComponent(duckNames[i], false);
            duck.setCursor(new Cursor(Cursor.HAND_CURSOR));
            duck.setToolTipText("你要干啥！");
            
            // 设置处理器
            duck.setAnimationHandler(animationHandler);
            duck.setSoundHandler(soundHandler);
            
            final int index = i; // 保存索引用于事件处理
            
            //添加小鸭子点击事件处理
            duck.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //执行随机情绪动画
                    duck.performRandomEmotionAnimation();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    //移除鼠标悬停边框效果
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    //移除鼠标离开边框处理
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
    
    //显示唐老鸭的对话框 用户可以在这里输入各种需求
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
            // 裁剪出头部区域
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
    
    //处理用户请求，根据关键词识别用户意图并调用相应功能，采用策略模式处理不同类型的用户请求
    private void processUserRequest(String request) {
        //添加更多关键词匹配
        if (request.contains("代码") || request.contains("统计")) {
            //委托给CodeStatsHandler处理
            codeStatsHandler.showLanguageSelection();
        } else if (request.contains("红包") || request.contains("游戏")) {
            startRedPacketRainGame();
        } else if (request.contains("点名")) {
            //委托给事件处理器
            eventHandler.startRollCallSystem();
        } else {
            callAIService(request);
        }
    }
    
    //启动红包雨游戏
    private void startRedPacketRainGame() {
        SwingUtilities.invokeLater(() -> {
            try {
                new RedPacketRainGame(this).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "启动红包雨游戏时发生错误: " + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }
    
    //调用AI服务
    private void callAIService(String userMessage) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return aiService.chat(userMessage);
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    JOptionPane.showMessageDialog(DuckGUI.this, 
                        "<html><body style='width: 300px; word-wrap: break-word;'>" + 
                        "<b>🤖 唐老鸭的AI回复：</b><br/>" + response + "</body></html>",
                        "AI 回复", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DuckGUI.this, 
                        "AI服务调用失败: " + ex.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        
        worker.execute();
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
