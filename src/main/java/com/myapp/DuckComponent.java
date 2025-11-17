package com.myapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * 鸭子组件 - 手绘可爱的卡通鸭子
 */
public class DuckComponent extends JComponent {
    private final String name;
    private final boolean isDonald;
    private final List<String> clothing = new ArrayList<>();
    
    public DuckComponent(String name, boolean isDonald) {
        this.name = name;
        this.isDonald = isDonald;
        setPreferredSize(new Dimension(200, 280));
    }
    
    public String getName() {
        return name;
    }
    
    public void addClothing(String item) {
        if (!clothing.contains(item)) {
            clothing.add(item);
        }
    }
    
    public void removeClothing(String item) {
        clothing.remove(item);
    }
    
    public List<String> getClothing() {
        return new ArrayList<>(clothing);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        
        // 绘制鸭子
        if (isDonald) {
            drawDonaldDuck(g2d, centerX, 60);
        } else {
            drawBabyDuck(g2d, centerX, 70);
        }
        
        // 绘制名字标签
        g2d.setColor(new Color(255, 255, 255, 230));
        Font font = new Font("SansSerif", Font.BOLD, 14);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        int labelX = (width - nameWidth - 20) / 2;
        int labelY = height - 30;
        
        g2d.fillRoundRect(labelX, labelY, nameWidth + 20, 25, 12, 12);
        g2d.setColor(new Color(70, 130, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(labelX, labelY, nameWidth + 20, 25, 12, 12);
        
        g2d.setColor(isDonald ? new Color(0, 51, 153) : new Color(255, 140, 0));
        g2d.drawString(name, labelX + 10, labelY + 17);
        
        g2d.dispose();
    }
    
    /**
     * 绘制唐老鸭（蓝色）
     */
    private void drawDonaldDuck(Graphics2D g2d, int centerX, int startY) {
        
        // 身体
        g2d.setColor(new Color(255, 255, 255)); // 白色身体
        g2d.fillOval(centerX - 45, startY + 60, 90, 100);
        
        // 翅膀
        g2d.setColor(new Color(240, 240, 240));
        // 左翅膀
        int[] leftWingX = {centerX - 45, centerX - 60, centerX - 50, centerX - 40};
        int[] leftWingY = {startY + 80, startY + 90, startY + 110, startY + 100};
        g2d.fillPolygon(leftWingX, leftWingY, 4);
        // 右翅膀
        int[] rightWingX = {centerX + 45, centerX + 60, centerX + 50, centerX + 40};
        int[] rightWingY = {startY + 80, startY + 90, startY + 110, startY + 100};
        g2d.fillPolygon(rightWingX, rightWingY, 4);
        
        // 脖子
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillOval(centerX - 25, startY + 35, 50, 40);
        
        // 头部
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillOval(centerX - 35, startY - 10, 70, 70);
        
        // 帽子（如果有）
        if (clothing.contains("帽子")) {
            drawHat(g2d, centerX, startY - 15);
        }
        
        // 嘴巴
        g2d.setColor(new Color(255, 165, 0)); // 橙色嘴巴
        int[] beakX = {centerX - 35, centerX - 55, centerX - 35};
        int[] beakY = {startY + 20, startY + 30, startY + 40};
        g2d.fillPolygon(beakX, beakY, 3);
        
        // 眼睛背景
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - 20, startY + 10, 18, 18);
        g2d.fillOval(centerX + 2, startY + 10, 18, 18);
        
        // 眼镜（如果有）
        if (clothing.contains("眼镜")) {
            drawGlasses(g2d, centerX, startY + 15);
        } else {
            // 眼珠
            g2d.setColor(Color.BLACK);
            g2d.fillOval(centerX - 15, startY + 15, 8, 8);
            g2d.fillOval(centerX + 7, startY + 15, 8, 8);
            
            // 高光
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX - 13, startY + 16, 3, 3);
            g2d.fillOval(centerX + 9, startY + 16, 3, 3);
        }
        
        // 领带（如果有）
        if (clothing.contains("领带")) {
            drawTie(g2d, centerX, startY + 55);
        }
        
        // 腿和脚
        g2d.setColor(new Color(255, 165, 0));
        // 左腿
        g2d.fillRoundRect(centerX - 30, startY + 140, 15, 40, 10, 10);
        // 右腿
        g2d.fillRoundRect(centerX + 15, startY + 140, 15, 40, 10, 10);
        
        // 脚掌
        int[] leftFootX = {centerX - 40, centerX - 30, centerX - 20, centerX - 25};
        int[] leftFootY = {startY + 180, startY + 185, startY + 185, startY + 175};
        g2d.fillPolygon(leftFootX, leftFootY, 4);
        
        int[] rightFootX = {centerX + 10, centerX + 15, centerX + 25, centerX + 30};
        int[] rightFootY = {startY + 180, startY + 185, startY + 185, startY + 175};
        g2d.fillPolygon(rightFootX, rightFootY, 4);
        
        // 拐杖（如果有）
        if (clothing.contains("拐杖")) {
            drawCane(g2d, centerX + 60, startY + 100);
        }
    }
    
    
      //绘制小鸭子（黄色）
     
    private void drawBabyDuck(Graphics2D g2d, int centerX, int startY) {
        // 绘制西装（如果有）- 先画，在底层
        if (clothing.contains("西装")) {
            drawSuit(g2d, centerX, startY + 70);
        }
        
        // 身体（毛茸茸的黄色）
        GradientPaint bodyGradient = new GradientPaint(
            centerX - 40, startY + 50, new Color(255, 230, 100),
            centerX + 40, startY + 130, new Color(255, 200, 50)
        );
        g2d.setPaint(bodyGradient);
        g2d.fillOval(centerX - 40, startY + 50, 80, 90);
        
        // 绒毛效果
        g2d.setColor(new Color(255, 220, 80, 100));
        for (int i = 0; i < 8; i++) {
            int angle = i * 45;
            int x = centerX + (int)(30 * Math.cos(Math.toRadians(angle)));
            int y = startY + 90 + (int)(35 * Math.sin(Math.toRadians(angle)));
            g2d.fillOval(x - 8, y - 8, 16, 16);
        }
        
        // 翅膀
        g2d.setColor(new Color(255, 210, 70));
        // 左翅膀
        g2d.fillOval(centerX - 48, startY + 70, 25, 35);
        // 右翅膀
        g2d.fillOval(centerX + 23, startY + 70, 25, 35);
        
        // 头部
        g2d.setColor(new Color(255, 230, 100));
        g2d.fillOval(centerX - 30, startY - 5, 60, 60);
        
        // 头顶绒毛
        g2d.setColor(new Color(255, 220, 80));
        for (int i = 0; i < 3; i++) {
            g2d.fillOval(centerX - 15 + i * 15, startY - 10, 10, 10);
        }
        
        // 帽子（
        if (clothing.contains("帽子")) {
            drawHat(g2d, centerX, startY - 5);
        }
        
        // 嘴巴
        g2d.setColor(new Color(255, 140, 0));
        int[] beakUpperX = {centerX - 25, centerX - 40, centerX - 25};
        int[] beakUpperY = {startY + 20, startY + 25, startY + 30};
        g2d.fillPolygon(beakUpperX, beakUpperY, 3);
        
        // 眼睛
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 18, startY + 12, 12, 12);
        g2d.fillOval(centerX + 6, startY + 12, 12, 12);
        
        // 眼镜（如果有）
        if (clothing.contains("眼镜")) {
            drawGlasses(g2d, centerX, startY + 15);
        } else {
            // 高光
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX - 15, startY + 14, 4, 4);
            g2d.fillOval(centerX + 9, startY + 14, 4, 4);
        }
        
        // 领带
        if (clothing.contains("领带")) {
            drawTie(g2d, centerX, startY + 48);
        }
        
        // 腿和脚
        g2d.setColor(new Color(255, 140, 0));
        // 左腿
        g2d.fillRoundRect(centerX - 25, startY + 130, 12, 35, 8, 8);
        // 右腿
        g2d.fillRoundRect(centerX + 13, startY + 130, 12, 35, 8, 8);
        
        // 脚掌（可爱的三趾）
        g2d.setColor(new Color(255, 120, 0));
        // 左脚
        int[] leftFootX = {centerX - 30, centerX - 25, centerX - 20, centerX - 22};
        int[] leftFootY = {startY + 165, startY + 170, startY + 170, startY + 162};
        g2d.fillPolygon(leftFootX, leftFootY, 4);
        // 右脚
        int[] rightFootX = {centerX + 8, centerX + 13, centerX + 18, centerX + 20};
        int[] rightFootY = {startY + 165, startY + 170, startY + 170, startY + 162};
        g2d.fillPolygon(rightFootX, rightFootY, 4);
        
        // 拐杖（如果有）
        if (clothing.contains("拐杖")) {
            drawCane(g2d, centerX + 50, startY + 90);
        }
    }
    
    /**
     * 绘制礼帽
     */
    private void drawHat(Graphics2D g2d, int centerX, int y) {
        // 帽子主体
        g2d.setColor(Color.BLACK);
        g2d.fillRoundRect(centerX - 28, y, 56, 22, 10, 10);
        
        // 帽檐
        g2d.fillRoundRect(centerX - 35, y + 18, 70, 6, 5, 5);
        
        // 帽带
        g2d.setColor(new Color(178, 34, 34));
        g2d.fillRoundRect(centerX - 28, y + 14, 56, 6, 3, 3);
        
        // 高光
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRoundRect(centerX - 20, y + 5, 15, 8, 3, 3);
    }
    
    /**
     * 绘制眼镜
     */
    private void drawGlasses(Graphics2D g2d, int centerX, int y) {
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        
        // 左镜片
        g2d.drawOval(centerX - 28, y - 3, 22, 18);
        // 右镜片
        g2d.drawOval(centerX + 6, y - 3, 22, 18);
        // 鼻梁
        g2d.drawLine(centerX - 6, y + 6, centerX + 6, y + 6);
        // 镜腿
        g2d.drawLine(centerX - 28, y + 6, centerX - 38, y + 3);
        g2d.drawLine(centerX + 28, y + 6, centerX + 38, y + 3);
    }
    
    /**
     * 绘制领带
     */
    private void drawTie(Graphics2D g2d, int centerX, int y) {
        // 领结
        g2d.setColor(new Color(139, 0, 0));
        int[] bowX = {centerX - 12, centerX - 4, centerX, centerX + 4, centerX + 12, centerX + 4, centerX, centerX - 4};
        int[] bowY = {y, y - 3, y, y - 3, y, y + 3, y, y + 3};
        g2d.fillPolygon(bowX, bowY, 8);
        
        // 领带主体
        g2d.setColor(new Color(178, 34, 34));
        int[] tieX = {centerX - 6, centerX + 6, centerX + 8, centerX - 8};
        int[] tieY = {y + 3, y + 3, y + 45, y + 45};
        g2d.fillPolygon(tieX, tieY, 4);
        
        // 领带尖端
        int[] tipX = {centerX - 8, centerX + 8, centerX};
        int[] tipY = {y + 45, y + 45, y + 55};
        g2d.fillPolygon(tipX, tipY, 3);
        
        // 领带纹理
        g2d.setColor(new Color(200, 50, 50));
        for (int i = 0; i < 3; i++) {
            g2d.drawLine(centerX - 4, y + 10 + i * 12, centerX + 4, y + 18 + i * 12);
        }
    }
    
    /**
     * 绘制西装
     */
    private void drawSuit(Graphics2D g2d, int centerX, int y) {
        // 西装主体 - 分开的衣襟，中间留空给领带
        g2d.setColor(new Color(25, 25, 112));
        
        // 左衣襟
        int[] leftX = {centerX - 42, centerX - 8, centerX - 8, centerX - 45};
        int[] leftY = {y, y, y + 65, y + 65};
        g2d.fillPolygon(leftX, leftY, 4);
        
        // 右衣襟
        int[] rightX = {centerX + 8, centerX + 42, centerX + 45, centerX + 8};
        int[] rightY = {y, y, y + 65, y + 65};
        g2d.fillPolygon(rightX, rightY, 4);
        
        // 衣领
        g2d.setColor(new Color(15, 15, 82));
        int[] collarLX = {centerX - 42, centerX - 25, centerX - 8};
        int[] collarLY = {y, y - 5, y + 10};
        g2d.fillPolygon(collarLX, collarLY, 3);
        
        int[] collarRX = {centerX + 42, centerX + 25, centerX + 8};
        int[] collarRY = {y, y - 5, y + 10};
        g2d.fillPolygon(collarRX, collarRY, 3);
        
        // 金色纽扣
        g2d.setColor(new Color(255, 215, 0));
        for (int i = 0; i < 3; i++) {
            g2d.fillOval(centerX - 18, y + 15 + i * 18, 6, 6);
        }
        
        // 口袋
        g2d.setColor(new Color(20, 20, 100));
        g2d.fillRoundRect(centerX - 35, y + 20, 15, 12, 3, 3);
        g2d.fillRoundRect(centerX + 20, y + 20, 15, 12, 3, 3);
    }
    
    /**
     * 绘制拐杖
     */
    private void drawCane(Graphics2D g2d, int x, int y) {
        // 拐杖主体
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(5));
        g2d.drawLine(x, y, x, y + 70);
        
        // 弧形手柄
        g2d.drawArc(x - 12, y - 12, 24, 24, 0, 180);
        
        // 底部装饰
        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(x - 4, y + 66, 8, 8);
        
        // 手柄装饰
        g2d.fillOval(x - 3, y - 15, 6, 6);
    }
}
