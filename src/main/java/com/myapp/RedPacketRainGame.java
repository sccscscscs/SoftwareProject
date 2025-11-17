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
            
            // 检查游戏时间
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= GAME_DURATION) {
                endGame();
                return;
            }
            
            // 更新红包位置
            List<RedPacket> toRemove = new ArrayList<>();
            for (RedPacket rp : redPackets) {
                rp.fall();
                
                // 检测碰撞
                if (rp.collidesWith(player)) {
                    totalMoney += rp.money;
                    toRemove.add(rp);
                }
                
                // 红包掉出屏幕
                if (rp.y > GAME_HEIGHT) {
                    toRemove.add(rp);
                }
            }
            redPackets.removeAll(toRemove);
            
            gamePanel.repaint();
        });
        gameTimer.start();
        
        // 定时生成红包
        redPacketSpawnTimer = new Timer(500, e -> {
            if (!gameRunning) return;
            spawnRedPacket();
        });
        redPacketSpawnTimer.start();
    }
    
    /**
     * 生成红包
     */
    private void spawnRedPacket() {
        int x = random.nextInt(GAME_WIDTH - RED_PACKET_SIZE);
        double money = 0.01 + random.nextDouble() * 9.99; // 0.01 到 10.00元
        int shape = random.nextInt(3); // 0: 圆形, 1: 方形, 2: 菱形
        redPackets.add(new RedPacket(x, 0, money, shape));
    }
    
    /**
     * 结束游戏
     */
    private void endGame() {
        gameRunning = false;
        if (gameTimer != null) gameTimer.stop();
        if (redPacketSpawnTimer != null) redPacketSpawnTimer.stop();
        
        JOptionPane.showMessageDialog(this, 
            String.format("游戏结束！\n您共获得红包金额：%.2f 元", totalMoney),
            "游戏结束", 
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    /**
     * 玩家类
     */
    class Player {
        int x, y;
        int speed = 8;
        
        Player(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        void moveLeft() {
            x = Math.max(0, x - speed);
        }
        
        void moveRight() {
            x = Math.min(GAME_WIDTH - PLAYER_SIZE, x + speed);
        }
        
        void moveUp() {
            y = Math.max(0, y - speed);
        }
        
        void moveDown() {
            y = Math.min(GAME_HEIGHT - PLAYER_SIZE, y + speed);
        }
        
        Rectangle getBounds() {
            return new Rectangle(x, y, PLAYER_SIZE, PLAYER_SIZE);
        }
        
        void draw(Graphics2D g2d) {
            // 绘制小人（简单的火柴人）
            g2d.setColor(Color.BLUE);
            
            // 头
            int headSize = PLAYER_SIZE / 3;
            g2d.fillOval(x + PLAYER_SIZE / 2 - headSize / 2, y, headSize, headSize);
            
            // 身体
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(x + PLAYER_SIZE / 2, y + headSize, x + PLAYER_SIZE / 2, y + PLAYER_SIZE * 2 / 3);
            
            // 手臂
            g2d.drawLine(x + PLAYER_SIZE / 2, y + headSize + 5, x + PLAYER_SIZE / 4, y + PLAYER_SIZE / 2);
            g2d.drawLine(x + PLAYER_SIZE / 2, y + headSize + 5, x + PLAYER_SIZE * 3 / 4, y + PLAYER_SIZE / 2);
            
            // 腿
            g2d.drawLine(x + PLAYER_SIZE / 2, y + PLAYER_SIZE * 2 / 3, x + PLAYER_SIZE / 3, y + PLAYER_SIZE);
            g2d.drawLine(x + PLAYER_SIZE / 2, y + PLAYER_SIZE * 2 / 3, x + PLAYER_SIZE * 2 / 3, y + PLAYER_SIZE);
        }
    }
    
    /**
     * 红包类
     */
    class RedPacket {
        int x, y;
        double money;
        int shape; // 0: 圆形, 1: 方形, 2: 菱形
        int fallSpeed;
        Color color;
        
        RedPacket(int x, int y, double money, int shape) {
            this.x = x;
            this.y = y;
            this.money = money;
            this.shape = shape;
            this.fallSpeed = 2 + random.nextInt(3); // 速度2-4
            
            // 根据金额决定颜色（金额越大越偏金色）
            if (money > 8) {
                color = new Color(255, 215, 0); // 金色
            } else if (money > 5) {
                color = new Color(255, 140, 0); // 橙色
            } else if (money > 2) {
                color = new Color(255, 69, 0);  // 橙红
            } else {
                color = new Color(255, 0, 0);   // 红色
            }
        }
        
        void fall() {
            y += fallSpeed;
        }
        
        boolean collidesWith(Player player) {
            Rectangle rpBounds = new Rectangle(x, y, RED_PACKET_SIZE, RED_PACKET_SIZE);
            return rpBounds.intersects(player.getBounds());
        }
        
        void draw(Graphics2D g2d) {
            g2d.setColor(color);
            
            switch (shape) {
                case 0: // 圆形
                    g2d.fillOval(x, y, RED_PACKET_SIZE, RED_PACKET_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(x, y, RED_PACKET_SIZE, RED_PACKET_SIZE);
                    break;
                    
                case 1: // 方形
                    g2d.fillRect(x, y, RED_PACKET_SIZE, RED_PACKET_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, RED_PACKET_SIZE, RED_PACKET_SIZE);
                    break;
                    
                case 2: // 菱形
                    int[] xPoints = {
                        x + RED_PACKET_SIZE / 2,
                        x + RED_PACKET_SIZE,
                        x + RED_PACKET_SIZE / 2,
                        x
                    };
                    int[] yPoints = {
                        y,
                        y + RED_PACKET_SIZE / 2,
                        y + RED_PACKET_SIZE,
                        y + RED_PACKET_SIZE / 2
                    };
                    g2d.fillPolygon(xPoints, yPoints, 4);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(xPoints, yPoints, 4);
                    break;
            }
            
            // 绘制"￥"符号
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.drawString("￥", x + RED_PACKET_SIZE / 2 - 5, y + RED_PACKET_SIZE / 2 + 4);
        }
    }
    
    /**
     * 游戏面板
     */
    class GamePanel extends JPanel {
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 背景
            g2d.setColor(new Color(240, 248, 255));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // 绘制红包
            for (RedPacket rp : redPackets) {
                rp.draw(g2d);
            }
            
            // 绘制玩家
            player.draw(g2d);
            
            // 绘制倒计时和金额
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            int remainingSeconds = (int) Math.ceil((GAME_DURATION - elapsedTime) / 1000.0);
            if (remainingSeconds < 0) remainingSeconds = 0;
            
            g2d.drawString("倒计时: " + remainingSeconds + "秒", 20, 30);
            g2d.drawString(String.format("金额: %.2f元", totalMoney), 20, 60);
            
            // 提示信息
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2d.drawString("使用方向键↑↓←→控制小人移动", GAME_WIDTH - 250, 30);
            
            g2d.dispose();
        }
    }
}

