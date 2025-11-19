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
import java.awt.geom.AffineTransform;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * 鸭子组件 - 显示鸭子图片或手绘可爱的卡通鸭子
 * 
 * 支持加载图片资源显示鸭子，如果图片加载失败则使用手绘方式
 * 所有服装配件都使用手绘方式实现，不依赖外部图片资源
 * 支持多种服装风格：休闲装、正装、运动装等
 */
public class DuckComponent extends JComponent {
    private final String name;
    private final boolean isDonald;
    private final List<String> clothing = new ArrayList<>();
    private boolean isSelected = false; // 是否被选中（用于交互效果）
    
    // 服装风格类型
    public static final String STYLE_CASUAL = "休闲装";
    public static final String STYLE_FORMAL = "正装";
    public static final String STYLE_SPORTS = "运动装";
    
    private String currentStyle = STYLE_CASUAL; // 默认服装风格
    
    public DuckComponent(String name, boolean isDonald) {
        this.name = name;
        this.isDonald = isDonald;
        setPreferredSize(new Dimension(220, 300)); // 增大小鸭子尺寸
    }
    
    public String getName() {
        return name;
    }
    
    public void addClothing(String item) {
        // 确保服装和配饰之间互斥
        if (item.equals("背带裤")) {
            // 如果添加背带裤，则移除其他服装
            clothing.remove("休闲装");
            clothing.remove("正装");
            clothing.remove("运动装");
        } else if (item.equals("休闲装") || item.equals("正装") || item.equals("运动装")) {
            // 如果添加其他服装，则移除背带裤
            clothing.remove("背带裤");
        }
        
        // 确保帽子、眼镜和蝴蝶结不会重复穿戴
        if (item.equals("帽子")) {
            // 移除相同类型的物品
            clothing.removeIf(c -> c.equals("帽子"));
        } else if (item.equals("眼镜")) {
            // 移除相同类型的物品
            clothing.removeIf(c -> c.equals("眼镜"));
        } else if (item.equals("蝴蝶结")) {
            // 移除相同类型的物品
            clothing.removeIf(c -> c.equals("蝴蝶结"));
        }
        
        // 添加新物品
        clothing.add(item);
    }
    
    public void removeClothing(String item) {
        clothing.remove(item);
    }
    
    public List<String> getClothing() {
        return new ArrayList<>(clothing);
    }
    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setStyle(String style) {
        this.currentStyle = style;
        repaint();
    }
    
    public String getStyle() {
        return currentStyle;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        
        // 如果被选中，添加一些视觉效果
        if (isSelected) {
            // 添加发光效果
            g2d.setColor(new Color(255, 255, 200, 100));
            g2d.fillOval(centerX - 80, 10, 160, 260);
        }
        
        // 始终使用手绘方式绘制鸭子
        if (isDonald) {
            drawDonaldDuck(g2d, centerX, 60);
        } else {
            drawBabyDuck(g2d, centerX, 70);
        }
        
        // 绘制配饰
        drawAccessories(g2d, centerX, isDonald ? 60 : 70);
        
        // 绘制名字标签
        g2d.setColor(new Color(255, 255, 255, 230));
        Font font = new Font("SansSerif", Font.BOLD, 16);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        int labelX = (width - nameWidth - 20) / 2;
        int labelY = height - 35;
        
        g2d.fillRoundRect(labelX, labelY, nameWidth + 20, 30, 15, 15);
        g2d.setColor(new Color(70, 130, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(labelX, labelY, nameWidth + 20, 30, 15, 15);
        
        g2d.setColor(isDonald ? new Color(0, 51, 153) : new Color(255, 140, 0));
        g2d.drawString(name, labelX + 10, labelY + 20);
        
        g2d.dispose();
    }
    
    /**
     * 绘制配饰
     */
    private void drawAccessories(Graphics2D g2d, int centerX, int startY) {
        // 帽子（如果有）
        if (clothing.contains("帽子") || clothing.contains("棒球帽")) {
            drawHat(g2d, centerX, startY + (isDonald ? 15 : 20)); // 进一步降低帽子位置
        }
        
        // 眼镜（如果有）
        if (clothing.contains("眼镜") || clothing.contains("太阳镜")) {
            drawGlasses(g2d, centerX, startY + (isDonald ? 50 : 55)); // 显著降低眼镜位置
        }
        
        // 蝴蝶结（如果有）
        if (clothing.contains("蝴蝶结")) {
            drawBowtie(g2d, centerX, startY + (isDonald ? 90 : 95)); // 显著降低蝴蝶结位置
        }
        
        // 拐杖（如果有）
        if (clothing.contains("拐杖")) {
            drawCane(g2d, centerX + (isDonald ? 65 : 55), startY + (isDonald ? 110 : 105));
        }
        
        // 背带裤（如果有）
        if (clothing.contains("背带裤") || clothing.contains("工装裤")) {
            drawOveralls(g2d, centerX, startY + (isDonald ? 80 : 85)); // 调整背带裤位置
        }
        
        // T恤（如果有）
        if (clothing.contains("T恤")) {
            drawTShirt(g2d, centerX, startY + (isDonald ? 65 : 70));
        }
        
        // 衬衫（如果有）
        if (clothing.contains("衬衫")) {
            drawShirt(g2d, centerX, startY + (isDonald ? 65 : 70));
        }
        
        // 毛衣（如果有）
        if (clothing.contains("毛衣") || clothing.contains("卫衣")) {
            drawSweater(g2d, centerX, startY + (isDonald ? 65 : 70));
        }
        
        // 夹克（如果有）
        if (clothing.contains("夹克") || clothing.contains("西装") || 
            clothing.contains("雨衣") || clothing.contains("羽绒服")) {
            drawJacket(g2d, centerX, startY + (isDonald ? 65 : 70));
        }
        
        // 短裤（如果有）
        if (clothing.contains("短裤") || clothing.contains("牛仔短裤")) {
            drawShorts(g2d, centerX, startY + (isDonald ? 100 : 105));
        }
        
        // 长裤（如果有）
        if (clothing.contains("长裤") || clothing.contains("休闲长裤") || 
            clothing.contains("运动裤")) {
            drawLongPants(g2d, centerX, startY + (isDonald ? 100 : 105));
        }
        
        // 裙子（如果有）
        if (clothing.contains("裙子") || clothing.contains("百褶裙") || 
            clothing.contains("旗袍")) {
            drawSkirt(g2d, centerX, startY + (isDonald ? 100 : 105));
        }
        
        // 运动鞋（如果有）
        if (clothing.contains("运动鞋") || clothing.contains("跑鞋") || 
            clothing.contains("帆布鞋")) {
            drawSneakers(g2d, centerX, startY + (isDonald ? 130 : 135));
        }
        
        // 皮鞋（如果有）
        if (clothing.contains("皮鞋") || clothing.contains("高跟鞋") || 
            clothing.contains("登山鞋") || clothing.contains("雪地靴") || 
            clothing.contains("拖鞋")) {
            drawDressShoes(g2d, centerX, startY + (isDonald ? 130 : 135));
        }
    }
    
    /**
     * 绘制唐老鸭（蓝色）
     */
    private void drawDonaldDuck(Graphics2D g2d, int centerX, int startY) {
        // 身体
        g2d.setColor(new Color(255, 255, 255)); // 白色身体
        g2d.fillOval(centerX - 50, startY + 60, 100, 110);
        
        // 翅膀
        g2d.setColor(new Color(240, 240, 240));
        // 左翅膀
        int[] leftWingX = {centerX - 50, centerX - 65, centerX - 55, centerX - 45};
        int[] leftWingY = {startY + 85, startY + 95, startY + 115, startY + 105};
        g2d.fillPolygon(leftWingX, leftWingY, 4);
        // 右翅膀
        int[] rightWingX = {centerX + 50, centerX + 65, centerX + 55, centerX + 45};
        int[] rightWingY = {startY + 85, startY + 95, startY + 115, startY + 105};
        g2d.fillPolygon(rightWingX, rightWingY, 4);
        
        // 脖子
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillOval(centerX - 30, startY + 40, 60, 45);
        
        // 头部
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillOval(centerX - 40, startY - 10, 80, 80);
        
        // 帽子（如果有）
        if (clothing.contains("帽子")) {
            drawHat(g2d, centerX, startY - 15);
        }
        
        // 嘴巴
        g2d.setColor(new Color(255, 165, 0)); // 橙色嘴巴
        int[] beakX = {centerX - 40, centerX - 60, centerX - 40};
        int[] beakY = {startY + 25, startY + 35, startY + 45};
        g2d.fillPolygon(beakX, beakY, 3);
        
        // 眼睛背景
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - 25, startY + 15, 20, 20);
        g2d.fillOval(centerX + 5, startY + 15, 20, 20);
        
        // 眼镜（如果有）
        if (clothing.contains("眼镜")) {
            drawGlasses(g2d, centerX, startY + 20);
        } else {
            // 眼珠
            g2d.setColor(Color.BLACK);
            g2d.fillOval(centerX - 20, startY + 20, 10, 10);
            g2d.fillOval(centerX + 10, startY + 20, 10, 10);
            
            // 高光
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX - 18, startY + 21, 4, 4);
            g2d.fillOval(centerX + 12, startY + 21, 4, 4);
        }
        
        // 领带（如果有）
        if (clothing.contains("领带")) {
            drawBowtie(g2d, centerX, startY + 60);
        }
        
        // 腿和脚
        g2d.setColor(new Color(255, 165, 0));
        // 左腿
        g2d.fillRoundRect(centerX - 35, startY + 150, 18, 45, 12, 12);
        // 右腿
        g2d.fillRoundRect(centerX + 17, startY + 150, 18, 45, 12, 12);
        
        // 脚掌
        int[] leftFootX = {centerX - 45, centerX - 35, centerX - 25, centerX - 30};
        int[] leftFootY = {startY + 195, startY + 200, startY + 200, startY + 190};
        g2d.fillPolygon(leftFootX, leftFootY, 4);
        
        int[] rightFootX = {centerX + 10, centerX + 15, centerX + 25, centerX + 30};
        int[] rightFootY = {startY + 195, startY + 200, startY + 200, startY + 190};
        g2d.fillPolygon(rightFootX, rightFootY, 4);
        
        // 拐杖（如果有）
        if (clothing.contains("拐杖")) {
            drawCane(g2d, centerX + 65, startY + 110);
        }
    }
    
      //绘制小鸭子（黄色）
     
    private void drawBabyDuck(Graphics2D g2d, int centerX, int startY) {
        // 根据不同风格绘制不同的身体（确保服装风格互斥）
        if (STYLE_CASUAL.equals(currentStyle)) {
            drawCasualBody(g2d, centerX, startY);
        } else if (STYLE_FORMAL.equals(currentStyle)) {
            drawFormalBody(g2d, centerX, startY);
        } else if (STYLE_SPORTS.equals(currentStyle)) {
            drawSportsBody(g2d, centerX, startY);
        } else {
            drawCasualBody(g2d, centerX, startY); // 默认休闲装
        }
        
        // 头部
        g2d.setColor(new Color(255, 230, 100));
        g2d.fillOval(centerX - 35, startY - 5, 70, 70);
        
        // 头顶绒毛
        g2d.setColor(new Color(255, 220, 80));
        for (int i = 0; i < 3; i++) {
            g2d.fillOval(centerX - 20 + i * 20, startY - 10, 12, 12);
        }
        
        // 帽子（如果有）
        if (clothing.contains("帽子")) {
            drawHat(g2d, centerX, startY - 5);
        }
        
        // 嘴巴
        g2d.setColor(new Color(255, 140, 0));
        int[] beakUpperX = {centerX - 30, centerX - 45, centerX - 30};
        int[] beakUpperY = {startY + 25, startY + 30, startY + 35};
        g2d.fillPolygon(beakUpperX, beakUpperY, 3);
        
        // 眼睛
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - 22, startY + 15, 14, 14);
        g2d.fillOval(centerX + 8, startY + 15, 14, 14);
        
        // 眼镜（如果有）
        if (clothing.contains("眼镜")) {
            drawGlasses(g2d, centerX, startY + 20);
        } else {
            // 高光
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX - 18, startY + 17, 5, 5);
            g2d.fillOval(centerX + 12, startY + 17, 5, 5);
        }
        
        // 领带（在身体绘制之后绘制，确保在身体前面）
        if (clothing.contains("领带")) {
            drawBowtie(g2d, centerX, startY + 55);
        }
        
        // 腿和脚
        g2d.setColor(new Color(255, 140, 0));
        // 左腿
        g2d.fillRoundRect(centerX - 28, startY + 135, 14, 40, 9, 9);
        // 右腿
        g2d.fillRoundRect(centerX + 14, startY + 135, 14, 40, 9, 9);
        
        // 脚掌
        int[] leftFootX = {centerX - 36, centerX - 28, centerX - 20, centerX - 25};
        int[] leftFootY = {startY + 175, startY + 180, startY + 180, startY + 170};
        g2d.fillPolygon(leftFootX, leftFootY, 4);
        
        int[] rightFootX = {centerX + 6, centerX + 14, centerX + 22, centerX + 28};
        int[] rightFootY = {startY + 175, startY + 180, startY + 180, startY + 170};
        g2d.fillPolygon(rightFootX, rightFootY, 4);
        
        // 拐杖（如果有）
        if (clothing.contains("拐杖")) {
            drawCane(g2d, centerX + 55, startY + 105);
        }
    }
    
    /**
     * 绘制休闲装身体
     */
    private void drawCasualBody(Graphics2D g2d, int centerX, int startY) {
        // 身体（毛茸茸的黄色）
        GradientPaint bodyGradient = new GradientPaint(
            centerX - 45, startY + 55, new Color(255, 230, 100),
            centerX + 45, startY + 140, new Color(255, 200, 50)
        );
        g2d.setPaint(bodyGradient);
        g2d.fillOval(centerX - 45, startY + 55, 90, 100);
        
        // 绒毛效果
        g2d.setColor(new Color(255, 220, 80, 100));
        for (int i = 0; i < 8; i++) {
            int angle = i * 45;
            int x = centerX + (int)(35 * Math.cos(Math.toRadians(angle)));
            int y = startY + 100 + (int)(40 * Math.sin(Math.toRadians(angle)));
            g2d.fillOval(x - 10, y - 10, 20, 20);
        }
        
        // 翅膀
        g2d.setColor(new Color(255, 210, 70));
        // 左翅膀
        g2d.fillOval(centerX - 52, startY + 75, 28, 40);
        // 右翅膀
        g2d.fillOval(centerX + 24, startY + 75, 28, 40);
        
        // 如果有背带裤，绘制在身体前面
        if (clothing.contains("西装")) {
            drawOveralls(g2d, centerX, startY + 75);
        }
    }
    
    /**
     * 绘制正装身体
     */
    private void drawFormalBody(Graphics2D g2d, int centerX, int startY) {
        // 身体（更整洁的外观）
        g2d.setColor(new Color(255, 220, 80));
        g2d.fillOval(centerX - 42, startY + 55, 84, 95);
        
        // 西装细节
        g2d.setColor(new Color(50, 50, 150)); // 深蓝色西装
        g2d.fillOval(centerX - 38, startY + 60, 76, 35); // 西装上衣
        
        // 裤子
        g2d.fillRoundRect(centerX - 33, startY + 95, 66, 50, 18, 18);
        
        // 翅膀（较小，更贴身）
        g2d.setColor(new Color(255, 210, 70));
        g2d.fillOval(centerX - 50, startY + 75, 23, 30);
        g2d.fillOval(centerX + 27, startY + 75, 23, 30);
        
        // 如果有背带裤，绘制在身体前面
        if (clothing.contains("西装")) {
            drawOveralls(g2d, centerX, startY + 75);
        }
    }
    
    /**
     * 绘制运动装身体
     */
    private void drawSportsBody(Graphics2D g2d, int centerX, int startY) {
        // 运动背心
        g2d.setColor(new Color(255, 50, 50)); // 红色背心
        int[] vestX = {centerX - 40, centerX - 35, centerX + 35, centerX + 40};
        int[] vestY = {startY + 55, startY + 110, startY + 110, startY + 55};
        g2d.fillPolygon(vestX, vestY, 4);
        
        // 运动短裤
        g2d.setColor(new Color(50, 150, 255)); // 蓝色短裤
        g2d.fillOval(centerX - 35, startY + 110, 70, 35);
        
        // 身体（露出部分）
        g2d.setColor(new Color(255, 230, 100));
        g2d.fillOval(centerX - 35, startY + 55, 70, 45); // 露出的胸部
        g2d.fillOval(centerX - 40, startY + 90, 80, 45); // 露出的腹部
        
        // 翅膀（运动风格）
        g2d.setColor(new Color(255, 210, 70));
        g2d.fillOval(centerX - 50, startY + 75, 25, 32);
        g2d.fillOval(centerX + 25, startY + 75, 25, 32);
        
        // 如果有背带裤，绘制在身体前面
        if (clothing.contains("西装")) {
            drawOveralls(g2d, centerX, startY + 75);
        }
    }
    
    /**
     * 绘制帽子
     */
    private void drawHat(Graphics2D g2d, int centerX, int startY) {
        // 帽子顶部
        g2d.setColor(new Color(255, 0, 0)); // 红色帽子
        g2d.fillOval(centerX - 30, startY, 60, 18);
        
        // 帽子主体
        g2d.setColor(new Color(200, 0, 0));
        g2d.fillRoundRect(centerX - 25, startY + 5, 50, 25, 12, 12);
        
        // 帽子装饰
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(centerX - 7, startY + 12, 14, 14);
    }
    
    /**
     * 绘制眼镜
     */
    private void drawGlasses(Graphics2D g2d, int centerX, int startY) {
        g2d.setColor(new Color(50, 50, 150)); // 深蓝色镜框
        g2d.setStroke(new BasicStroke(3));
        
        // 左眼镜片
        g2d.drawOval(centerX - 23, startY - 3, 20, 20);
        // 右眼镜片
        g2d.drawOval(centerX + 3, startY - 3, 20, 20);
        
        // 镜架连接
        g2d.drawLine(centerX - 2, startY + 5, centerX + 2, startY + 5);
        
        // 镜片内部
        g2d.setColor(new Color(100, 100, 200, 100));
        g2d.fillOval(centerX - 20, startY, 14, 14);
        g2d.fillOval(centerX + 6, startY, 14, 14);
    }
    
    /**
     * 绘制蝴蝶结
     */
    private void drawBowtie(Graphics2D g2d, int centerX, int startY) {
        // 蝴蝶结左边
        g2d.setColor(new Color(220, 20, 60)); // 猩红色
        int[] leftBowX = {centerX - 12, centerX - 20, centerX - 12, centerX - 4};
        int[] leftBowY = {startY, startY + 8, startY + 16, startY + 8};
        g2d.fillPolygon(leftBowX, leftBowY, 4);
        
        // 蝴蝶结右边
        int[] rightBowX = {centerX + 12, centerX + 20, centerX + 12, centerX + 4};
        int[] rightBowY = {startY, startY + 8, startY + 16, startY + 8};
        g2d.fillPolygon(rightBowX, rightBowY, 4);
        
        // 蝴蝶结中心
        g2d.setColor(new Color(180, 0, 0));
        g2d.fillOval(centerX - 5, startY + 6, 10, 8);
        
        // 添加高光效果
        g2d.setColor(new Color(255, 100, 100, 150));
        g2d.fillOval(centerX - 3, startY + 7, 3, 3);
    }
    
    /**
     * 绘制裙子
     */
    private void drawSkirt(Graphics2D g2d, int centerX, int startY) {
        // 裙子主体
        g2d.setColor(new Color(255, 105, 180)); // 热粉色
        int[] skirtX = {centerX - 30, centerX - 40, centerX + 40, centerX + 30};
        int[] skirtY = {startY, startY + 30, startY + 30, startY};
        g2d.fillPolygon(skirtX, skirtY, 4);
        
        // 裙子褶皱
        g2d.setColor(new Color(255, 150, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(centerX - 20, startY + 5, centerX - 25, startY + 20);
        g2d.drawLine(centerX, startY + 5, centerX, startY + 25);
        g2d.drawLine(centerX + 20, startY + 5, centerX + 25, startY + 20);
    }
    
    /**
     * 绘制长裤
     */
    private void drawLongPants(Graphics2D g2d, int centerX, int startY) {
        // 长裤主体
        g2d.setColor(new Color(30, 144, 255)); // 道奇蓝
        g2d.fillRoundRect(centerX - 30, startY, 60, 40, 15, 15);
        
        // 裤腿
        g2d.fillRoundRect(centerX - 25, startY + 40, 20, 30, 10, 10);
        g2d.fillRoundRect(centerX + 5, startY + 40, 20, 30, 10, 10);
        
        // 裤缝
        g2d.setColor(new Color(20, 120, 220));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(centerX, startY + 5, centerX, startY + 35);
    }
    
    /**
     * 绘制短裤
     */
    private void drawShorts(Graphics2D g2d, int centerX, int startY) {
        // 短裤主体
        g2d.setColor(new Color(255, 215, 0)); // 金色
        g2d.fillRoundRect(centerX - 30, startY, 60, 25, 12, 12);
        
        // 裤腿
        g2d.fillRoundRect(centerX - 25, startY + 25, 20, 20, 8, 8);
        g2d.fillRoundRect(centerX + 5, startY + 25, 20, 20, 8, 8);
        
        // 裤袋
        g2d.setColor(new Color(230, 200, 50));
        g2d.fillOval(centerX - 20, startY + 5, 10, 8);
        g2d.fillOval(centerX + 10, startY + 5, 10, 8);
    }
    
    /**
     * 绘制皮鞋
     */
    private void drawDressShoes(Graphics2D g2d, int centerX, int startY) {
        // 左脚
        g2d.setColor(new Color(50, 50, 50)); // 黑色
        g2d.fillRoundRect(centerX - 35, startY, 25, 12, 6, 6);
        
        // 右脚
        g2d.fillRoundRect(centerX + 10, startY, 25, 12, 6, 6);
        
        // 鞋带
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) {
            g2d.fillOval(centerX - 30 + i * 10, startY + 3, 4, 3);
            g2d.fillOval(centerX + 15 + i * 10, startY + 3, 4, 3);
        }
    }
    
    /**
     * 绘制运动鞋
     */
    private void drawSneakers(Graphics2D g2d, int centerX, int startY) {
        // 左脚
        g2d.setColor(new Color(220, 220, 220)); // 灰色
        g2d.fillRoundRect(centerX - 35, startY - 2, 25, 15, 7, 7);
        
        // 右脚
        g2d.fillRoundRect(centerX + 10, startY - 2, 25, 15, 7, 7);
        
        // 鞋带
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(centerX - 32, startY + 2, centerX - 15, startY + 2);
        g2d.drawLine(centerX + 13, startY + 2, centerX + 30, startY + 2);
        
        // 鞋底
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRoundRect(centerX - 37, startY + 10, 30, 5, 2, 2);
        g2d.fillRoundRect(centerX + 8, startY + 10, 30, 5, 2, 2);
    }
    
    /**
     * 绘制拐杖
     */
    private void drawCane(Graphics2D g2d, int startX, int startY) {
        g2d.setColor(new Color(139, 69, 19)); // 棕色拐杖
        g2d.setStroke(new BasicStroke(4));
        
        // 拐杖主体
        g2d.drawLine(startX, startY, startX + 10, startY + 80);
        
        // 拐杖头部
        g2d.setStroke(new BasicStroke(6));
        g2d.drawLine(startX - 5, startY, startX + 15, startY);
    }
    
    /**
     * 绘制T恤
     */
    private void drawTShirt(Graphics2D g2d, int centerX, int startY) {
        // T恤主体
        g2d.setColor(new Color(255, 100, 100)); // 红色T恤
        int[] shirtX = {centerX - 35, centerX - 40, centerX + 40, centerX + 35};
        int[] shirtY = {startY, startY + 50, startY + 50, startY};
        g2d.fillPolygon(shirtX, shirtY, 4);
        
        // 领口
        g2d.setColor(new Color(200, 50, 50));
        g2d.fillOval(centerX - 20, startY - 5, 40, 15);
        
        // 袖子
        g2d.fillOval(centerX - 42, startY + 10, 15, 25);
        g2d.fillOval(centerX + 27, startY + 10, 15, 25);
    }
    
    /**
     * 绘制衬衫
     */
    private void drawShirt(Graphics2D g2d, int centerX, int startY) {
        // 衬衫主体
        g2d.setColor(new Color(100, 150, 255)); // 蓝色衬衫
        int[] shirtX = {centerX - 35, centerX - 40, centerX + 40, centerX + 35};
        int[] shirtY = {startY, startY + 50, startY + 50, startY};
        g2d.fillPolygon(shirtX, shirtY, 4);
        
        // 领子
        g2d.setColor(new Color(80, 130, 240));
        int[] collarLeftX = {centerX - 35, centerX - 25, centerX - 20, centerX - 30};
        int[] collarLeftY = {startY + 5, startY + 15, startY + 20, startY + 10};
        g2d.fillPolygon(collarLeftX, collarLeftY, 4);
        
        int[] collarRightX = {centerX + 35, centerX + 25, centerX + 20, centerX + 30};
        int[] collarRightY = {startY + 5, startY + 15, startY + 20, startY + 10};
        g2d.fillPolygon(collarRightX, collarRightY, 4);
        
        // 领带（如果有）
        if (clothing.contains("领带")) {
            drawBowtie(g2d, centerX, startY + 25);
        }
        
        // 袖口
        g2d.fillRoundRect(centerX - 42, startY + 30, 15, 20, 5, 5);
        g2d.fillRoundRect(centerX + 27, startY + 30, 15, 20, 5, 5);
    }
    
    /**
     * 绘制毛衣
     */
    private void drawSweater(Graphics2D g2d, int centerX, int startY) {
        // 毛衣主体
        g2d.setColor(new Color(100, 200, 100)); // 绿色毛衣
        int[] sweaterX = {centerX - 35, centerX - 40, centerX + 40, centerX + 35};
        int[] sweaterY = {startY, startY + 50, startY + 50, startY};
        g2d.fillPolygon(sweaterX, sweaterY, 4);
        
        // 高领
        g2d.setColor(new Color(80, 180, 80));
        g2d.fillRoundRect(centerX - 25, startY - 10, 50, 15, 5, 5);
        
        // 毛线纹理
        g2d.setColor(new Color(90, 190, 90));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 4; i++) {
            int offset = i * 15;
            g2d.drawArc(centerX - 30, startY + 5 + offset, 60, 20, 0, 180);
        }
    }
    
    /**
     * 绘制夹克
     */
    private void drawJacket(Graphics2D g2d, int centerX, int startY) {
        // 夹克主体
        g2d.setColor(new Color(150, 100, 200)); // 紫色夹克
        int[] jacketX = {centerX - 35, centerX - 40, centerX + 40, centerX + 35};
        int[] jacketY = {startY, startY + 50, startY + 50, startY};
        g2d.fillPolygon(jacketX, jacketY, 4);
        
        // 拉链
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, startY + 5, centerX, startY + 45);
        
        // 拉链头
        g2d.fillOval(centerX - 3, startY + 2, 6, 6);
        
        // 口袋
        g2d.setColor(new Color(130, 80, 180));
        g2d.fillRoundRect(centerX - 20, startY + 25, 40, 15, 7, 7);
    }
    
    /**
     * 绘制背带裤
     */
    private void drawOveralls(Graphics2D g2d, int centerX, int startY) {
        // 背带裤主体
        g2d.setColor(new Color(70, 130, 180)); // 钢蓝色背带裤
        int[] overallsX = {centerX - 35, centerX - 40, centerX + 40, centerX + 35};
        int[] overallsY = {startY, startY + 50, startY + 50, startY};
        g2d.fillPolygon(overallsX, overallsY, 4);
        
        // 背带
        g2d.setColor(new Color(50, 110, 160));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawLine(centerX - 25, startY, centerX - 25, startY - 30);
        g2d.drawLine(centerX + 25, startY, centerX + 25, startY - 30);
        
        // 肩带连接器
        g2d.fillOval(centerX - 28, startY - 32, 6, 6);
        g2d.fillOval(centerX + 22, startY - 32, 6, 6);
        
        // 裤腿
        g2d.fillRoundRect(centerX - 30, startY + 50, 20, 30, 8, 8);
        g2d.fillRoundRect(centerX + 10, startY + 50, 20, 30, 8, 8);
        
        // 背带扣
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(centerX - 30, startY - 10, 10, 10);
        g2d.drawOval(centerX + 20, startY - 10, 10, 10);
    }
}