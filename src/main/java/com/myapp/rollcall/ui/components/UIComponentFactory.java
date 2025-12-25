package com.myapp.rollcall.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//UI组件工厂类
public class UIComponentFactory {
    
    //定义现代化配色方案
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);      // 主色调 - 蓝色
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);      // 成功色 - 绿色
    public static final Color WARNING_COLOR = new Color(241, 196, 15);      // 警告色 - 黄色
    public static final Color DANGER_COLOR = new Color(231, 76, 60);       // 危险色 - 红色
    public static final Color INFO_COLOR = new Color(52, 152, 219);         // 信息色 - 蓝色
    public static final Color LIGHT_BG = new Color(248, 249, 250);        // 浅色背景
    public static final Color CARD_BG = new Color(255, 255, 255);         // 卡片背景
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);       // 主文本色
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);    // 次要文本色
    // 创建现代化按钮
    public static JButton createModernButton(String text, Color bgColor, int fontSize) {
        JButton button = new JButton(text) {
            //重写绘制方法，实现圆角
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 鼠标按下变深色，否则用背景色
                g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // 10是圆角弧度
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false); // 去掉点击时的虚线框
        button.setBorderPainted(false); // 去掉原生边框
        button.setContentAreaFilled(false); // 去掉原生背景填充
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 给按钮加一点内边距，看起来更胖更舒服
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        return button;
    }
    // 创建菜单按钮
    public static JButton createMenuButton() {
        JButton menuButton = new JButton("+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆形背景
                Color bgColor = getModel().isPressed() ? PRIMARY_COLOR.darker() : PRIMARY_COLOR;
                g2.setColor(bgColor);
                int diameter = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                g2.fillOval(x, y, diameter, diameter);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        menuButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.setToolTipText("更多功能");
        
        // 设置按钮大小为圆形
        menuButton.setPreferredSize(new java.awt.Dimension(40, 40));
        menuButton.setMaximumSize(new java.awt.Dimension(40, 40));
        menuButton.setMinimumSize(new java.awt.Dimension(40, 40));
        
        return menuButton;
    }
    
    /**
     * ⚠️脆鼠修改：创建弹出菜单
     * @return 配置好的弹出菜单
     */
    public static JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        
        // ⚠️脆鼠修改：添加菜单项
        JMenuItem statsItem = new JMenuItem("📊 查看统计");
        statsItem.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        JMenuItem historyItem = new JMenuItem("📝 查看点名历史");
        historyItem.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        popup.add(statsItem);
        popup.add(historyItem);
        
        return popup;
    }
    // 复选框样式
    public static JCheckBox createModernCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("苹方-简 中等", Font.PLAIN, 14));
        checkBox.setSelected(selected);
        checkBox.setBackground(CARD_BG);
        checkBox.setForeground(TEXT_PRIMARY);
        return checkBox;
    }
    // 标题标签样式
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, 28));
        label.setForeground(new Color(0, 102, 204));
        return label;
    }
    // 创建学生信息标签
    public static JLabel createStudentLabel(String text, int fontSize, boolean isBold) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("苹方-简 中等", isBold ? Font.BOLD : Font.PLAIN, fontSize));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
}
