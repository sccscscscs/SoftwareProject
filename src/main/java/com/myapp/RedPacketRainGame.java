package com.myapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 红包雨游戏
 * 用户操控小人（方向键上下左右）收集从屏幕上方掉落的红包
 * 游戏时长10秒，实时显示倒计时和获得的总金额
 */
public class RedPacketRainGame extends JDialog {
    
    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    private static final int PLAYER_SIZE = 40;
    private static final int RED_PACKET_SIZE = 30;
    private static final int GAME_DURATION = 10000; // 10秒
    
    private GamePanel gamePanel;
    private Timer gameTimer;
    private Timer redPacketSpawnTimer;
    private long startTime;
    private double totalMoney = 0.0;
    private boolean gameRunning = false;
    
    private Player player;
    private List<RedPacket> redPackets;
    private Random random;
    
    public RedPacketRainGame(JFrame parent) {
        super(parent, "红包雨游戏", true);
        setSize(GAME_WIDTH, GAME_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        init();
    }
    
    private void init() {
        random = new Random();
        player = new Player(GAME_WIDTH / 2 - PLAYER_SIZE / 2, GAME_HEIGHT - 100);
        redPackets = new ArrayList<>();
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        // 键盘控制
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameRunning) return;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        player.moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        player.moveRight();
                        break;
                    case KeyEvent.VK_UP:
                        player.moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        player.moveDown();
                        break;
                }
            }
        });
        
        startGame();
    }
    
    /**
     * 开始游戏
     */
    private void startGame() {
        gameRunning = true;
        startTime = System.currentTimeMillis();
        totalMoney = 0.0;
        redPackets.clear();
        
        // 游戏主循环（更新位置和检测碰撞）
        gameTimer = new Timer(20, e -> {
            if (!gameRunning) return;
            
            // 更新玩家位置
            player.update();
            
            // 更新红包位置
            for (int i = redPackets.size() - 1; i >= 0; i--) {
                RedPacket packet = redPackets.get(i);
                packet.update();
                
                // 移除超出屏幕的红包
                if (packet.y > GAME_HEIGHT) {
                    redPackets.remove(i);
                    continue;
                }
                
                // 检测碰撞
                if (player.getBounds().intersects(packet.getBounds())) {
                    totalMoney += packet.money;
                    redPackets.remove(i);
                }
            }
            
            // 更新界面
            gamePanel.repaint();
            
            // 检查游戏是否结束
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= GAME_DURATION) {
                endGame();
            }
        });
        gameTimer.start();
        
        // 红包生成定时器
        redPacketSpawnTimer = new Timer(300, e -> {
            if (!gameRunning) return;
            if (random.nextInt(100) < 30) { // 30%概率生成红包
                redPackets.add(new RedPacket(random.nextInt(GAME_WIDTH - RED_PACKET_SIZE), -RED_PACKET_SIZE));
            }
        });
        redPacketSpawnTimer.start();
    }
    
    /**
     * 结束游戏
     */
    private void endGame() {
        gameRunning = false;
        gameTimer.stop();
        redPacketSpawnTimer.stop();
        
        String message = String.format(
            "游戏结束！\n\n" +
            "总耗时: %.1f秒\n" +
            "获得金额: %.2f元\n\n" +
            "感谢参与红包雨游戏！",
            GAME_DURATION / 1000.0,
            totalMoney
        );
        
        JOptionPane.showMessageDialog(this, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
    
    /**
     * 玩家类
     */
    class Player {
        int x, y;
        int speed = 5;
        int dx = 0, dy = 0;
        
        public Player(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void moveLeft() {
            dx = -speed;
        }
        
        public void moveRight() {
            dx = speed;
        }
        
        public void moveUp() {
            dy = -speed;
        }
        
        public void moveDown() {
            dy = speed;
        }
        
        public void update() {
            x += dx;
            y += dy;
            
            // 边界检测
            if (x < 0) x = 0;
            if (x > GAME_WIDTH - PLAYER_SIZE) x = GAME_WIDTH - PLAYER_SIZE;
            if (y < 0) y = 0;
            if (y > GAME_HEIGHT - PLAYER_SIZE) y = GAME_HEIGHT - PLAYER_SIZE;
            
            // 减缓移动速度，使控制更平滑
            dx *= 0.8;
            dy *= 0.8;
        }
        
        public Rectangle getBounds() {
            return new Rectangle(x, y, PLAYER_SIZE, PLAYER_SIZE);
        }
    }
    
    /**
     * 红包类
     */
    class RedPacket {
        enum Shape { CIRCLE, SQUARE, TRIANGLE }
        
        int x, y;
        double money;
        int speed;
        Shape shape;
        
        public RedPacket(int x, int y) {
            this.x = x;
            this.y = y;
            this.money = (random.nextInt(1000) + 1) / 100.0; // 0.01 - 10.00元
            this.speed = random.nextInt(3) + 2; // 2-4像素/帧
            // 随机分配形状
            Shape[] shapes = Shape.values();
            this.shape = shapes[random.nextInt(shapes.length)];
        }
        
        public void update() {
            y += speed;
        }
        
        public Rectangle getBounds() {
            return new Rectangle(x, y, RED_PACKET_SIZE, RED_PACKET_SIZE);
        }
    }
    
    /**
     * 游戏面板类
     */
    class GamePanel extends JPanel {
        private Font infoFont = new Font("SansSerif", Font.BOLD, 16);
        private Font moneyFont = new Font("SansSerif", Font.BOLD, 20);
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制背景
            g2d.setColor(new Color(135, 206, 250)); // 天蓝色
            g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            
            // 绘制云朵装饰
            g2d.setColor(Color.WHITE);
            g2d.fillOval(50, 30, 60, 40);
            g2d.fillOval(80, 20, 70, 50);
            g2d.fillOval(120, 30, 60, 40);
            
            g2d.fillOval(600, 50, 60, 40);
            g2d.fillOval(630, 40, 70, 50);
            g2d.fillOval(670, 50, 60, 40);
            
            // 绘制玩家
            g2d.setColor(new Color(255, 165, 0)); // 橙色
            g2d.fillOval(player.x, player.y, PLAYER_SIZE, PLAYER_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(player.x, player.y, PLAYER_SIZE, PLAYER_SIZE);
            
            // 绘制眼睛
            g2d.fillOval(player.x + 10, player.y + 12, 6, 6);
            g2d.fillOval(player.x + 24, player.y + 12, 6, 6);
            
            // 绘制微笑
            g2d.drawArc(player.x + 10, player.y + 15, 20, 15, 0, -180);
            
            // 绘制红包
            for (RedPacket packet : redPackets) {
                // 根据形状绘制不同样式的红包
                switch (packet.shape) {
                    case CIRCLE:
                        g2d.setColor(new Color(255, 0, 0)); // 红色
                        g2d.fillOval(packet.x, packet.y, RED_PACKET_SIZE, RED_PACKET_SIZE);
                        break;
                    case SQUARE:
                        g2d.setColor(new Color(255, 0, 0)); // 红色
                        g2d.fillRoundRect(packet.x, packet.y, RED_PACKET_SIZE, RED_PACKET_SIZE, 8, 8);
                        break;
                    case TRIANGLE:
                        g2d.setColor(new Color(255, 0, 0)); // 红色
                        int[] xPoints = {packet.x + RED_PACKET_SIZE/2, packet.x, packet.x + RED_PACKET_SIZE};
                        int[] yPoints = {packet.y, packet.y + RED_PACKET_SIZE, packet.y + RED_PACKET_SIZE};
                        g2d.fillPolygon(xPoints, yPoints, 3);
                        break;
                }
                
                // 绘制金额
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                String moneyText = String.format("%.2f", packet.money);
                int textWidth = g2d.getFontMetrics().stringWidth(moneyText);
                g2d.drawString(moneyText, 
                    packet.x + (RED_PACKET_SIZE - textWidth) / 2, 
                    packet.y + RED_PACKET_SIZE / 2 + 5);
            }
            
            // 绘制游戏信息（改为黑色）
            long elapsed = System.currentTimeMillis() - startTime;
            long remaining = Math.max(0, GAME_DURATION - elapsed);
            double timeLeft = remaining / 1000.0;
            
            g2d.setFont(infoFont);
            g2d.setColor(Color.BLACK); // 改为黑色
            g2d.drawString(String.format("倒计时: %.1f秒", timeLeft), 20, 30);
            g2d.setFont(moneyFont);
            g2d.drawString(String.format("获得金额: %.2f元", totalMoney), 20, 60);
        }
    }
}
