package com.myapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

//衣柜处理器

public class WardrobeHandler {
    private final DuckGUI gui;
    
    public WardrobeHandler(DuckGUI gui) {
        this.gui = gui;
    }

    //分类换装
    public void showDressUpDialog(DuckComponent duck) {
        // 创建美化的分类换装对话框
        JDialog wardrobeDialog = new JDialog(gui, "👗 " + duck.getName() + "的衣橱", true);
        // 保存对话框引用 - 软工思想：状态管理
        // 好处：便于后续操作中更新对话框状态
        final JDialog dialog = wardrobeDialog;
        wardrobeDialog.setSize(600, 700);
        wardrobeDialog.setLocationRelativeTo(gui);
        wardrobeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // 创建主面板 - 布局管理
        // 使用选项卡面板，分类清晰
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(240, 248, 255));
        
        // 创建冬季服装面板
        JPanel winterPanel = createClothingCategoryPanel(duck, "冬装", 
            new String[]{"大衣", "毛衣"}, 
            new String[]{"🧥", "👕"}, 
            new String[]{"dayi", "maoyi"});
        
        //创建夏季服装面板
        JPanel summerPanel = createClothingCategoryPanel(duck, "夏装", 
            new String[]{"背带裤", "明袍"}, 
            new String[]{"👖", "🎋"}, 
            new String[]{"beidaiku", "mingpao"});
        
        // 创建装饰品面板 
        JPanel accessoryPanel = createClothingCategoryPanel(duck, "装饰", 
            new String[]{"帽子", "蝴蝶结"}, 
            new String[]{"🎩", "🎀"}, 
            new String[]{"hat", "hudiejie"});
        
        // 添加选项卡，清晰的分类导航
        tabbedPane.addTab("❄️ 冬装", winterPanel);
        tabbedPane.addTab("☀️ 夏装", summerPanel);
        tabbedPane.addTab("✨ 装饰", accessoryPanel);
        
        // 创建状态显示面板 
        // 好处：显示当前穿着状态
        JPanel statusPanel = createStatusPanel(duck);
        
        //创建按钮面板
        JPanel buttonPanel = createButtonPanel(wardrobeDialog, duck);
        
        //布局组装
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        wardrobeDialog.add(mainPanel, BorderLayout.CENTER);
        wardrobeDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        //美化对话框外观
        wardrobeDialog.getContentPane().setBackground(new Color(240, 248, 255));
        
        wardrobeDialog.setVisible(true);
    }
    
    //添加服装项目到面板
    public void addClothingItem(DuckComponent duck, JPanel panel, String itemName, String emoji, String imagePath) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        //创建包含图片的标签
        JLabel itemLabel = new JLabel(emoji + " " + itemName);
        itemLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        
        //尝试加载图片并添加到标签
        try {
            java.net.URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                // 缩放图片
                Image scaledImage = originalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                itemLabel.setIcon(scaledIcon);
            }
        } catch (Exception e) {
            System.err.println("无法加载图片: " + imagePath);
        }
        
        //预先确定按钮状态文本和颜色，避免点击两次才生效的问题
        boolean isWearing = duck.getClothing().contains(itemName);
        String buttonText = isWearing ? "✓ 已穿" : "穿上";
        Color buttonColor = isWearing ? new Color(144, 238, 144) : new Color(173, 216, 230);
        
        JButton toggleButton = new JButton(buttonText);
        toggleButton.setFocusPainted(false);
        
        //设置按钮的最大尺寸，避免按钮过大
        toggleButton.setMaximumSize(new Dimension(80, 30));
        toggleButton.setPreferredSize(new Dimension(80, 30));
        
        //设置按钮背景色
        toggleButton.setBackground(buttonColor);
        
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
        
        panel.add(itemPanel);
    }
    
    //创建服装分类面板
    private JPanel createClothingCategoryPanel(DuckComponent duck, String categoryName, 
            String[] clothingNames, String[] emojis, String[] imageFiles) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBackground(Color.WHITE);
        
        //分类标题
        JLabel categoryLabel = new JLabel(categoryName, JLabel.CENTER);
        categoryLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        categoryLabel.setOpaque(true);
        categoryLabel.setBackground(new Color(135, 206, 235));
        categoryLabel.setForeground(Color.WHITE);
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        categoryPanel.add(categoryLabel, BorderLayout.NORTH);
        
        //服装选项面板
        JPanel clothingPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        clothingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        clothingPanel.setBackground(Color.WHITE);
        
        //添加服装选项
        for (int i = 0; i < clothingNames.length; i++) {
            addClothingItem(duck, clothingPanel, clothingNames[i], emojis[i], "/images/" + imageFiles[i] + ".png");
        }
        
        categoryPanel.add(new JScrollPane(clothingPanel), BorderLayout.CENTER);
        
        return categoryPanel;
    }
    
    //创建状态显示面板
    private JPanel createStatusPanel(DuckComponent duck) {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statusPanel.setBackground(new Color(230, 240, 250));
        
        JLabel statusLabel = new JLabel("当前穿着：", JLabel.LEFT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        StringBuilder statusText = new StringBuilder();
        String currentClothing = duck.getCurrentClothing();
        if (currentClothing != null) {
            statusText.append("👔 ").append(currentClothing);
        } else {
            statusText.append("👕 未穿衣服");
        }
        
        java.util.List<String> accessories = duck.getCurrentAccessories();
        if (!accessories.isEmpty()) {
            statusText.append(" | ✨ ");
            for (String accessory : accessories) {
                statusText.append(accessory).append(" ");
            }
        }
        
        JLabel detailLabel = new JLabel(statusText.toString(), JLabel.LEFT);
        detailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detailLabel.setForeground(new Color(70, 130, 180));
        
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(detailLabel, BorderLayout.CENTER);
        
        return statusPanel;
    }
    
    //创建按钮面板
    private JPanel createButtonPanel(JDialog dialog, DuckComponent duck) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        //完成按钮
        JButton doneButton = new JButton("✅ 完成换装");
        doneButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        doneButton.setBackground(new Color(76, 175, 80));
        doneButton.setForeground(Color.WHITE);
        doneButton.setFocusPainted(false);
        doneButton.setPreferredSize(new Dimension(120, 35));
        doneButton.addActionListener(e -> dialog.dispose());
        
        //清除所有按钮
        JButton clearButton = new JButton("🗑️ 清除所有");
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.setBackground(new Color(220, 53, 69));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setPreferredSize(new Dimension(120, 35));
        clearButton.addActionListener(e -> {
            //清除所有服装
            for (String clothing : new java.util.ArrayList<>(duck.getClothing())) {
                duck.removeClothing(clothing);
            }
            duck.repaint();
            //刷新状态显示
            dialog.getContentPane().validate();
            dialog.getContentPane().repaint();
        });
        
        buttonPanel.add(doneButton);
        buttonPanel.add(clearButton);
        
        return buttonPanel;
    }
    
    //刷新所有按钮状态
    private void refreshAllButtons(JDialog dialog, DuckComponent duck) {
        //遍历对话框中的所有按钮
        refreshButtonsRecursive(dialog.getContentPane(), duck);
        
        //刷新整个对话框
        dialog.getContentPane().validate();
        dialog.getContentPane().repaint();
    }
    
    //递归刷新按钮状态 态
    private void refreshButtonsRecursive(Container container, DuckComponent duck) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                String buttonText = button.getText();
                
                //检查按钮是否对应某个服装
                if (buttonText.contains("大衣")) {
                    updateButtonState(button, "大衣", duck);
                } else if (buttonText.contains("毛衣")) {
                    updateButtonState(button, "毛衣", duck);
                } else if (buttonText.contains("背带裤")) {
                    updateButtonState(button, "背带裤", duck);
                } else if (buttonText.contains("明袍")) {
                    updateButtonState(button, "明袍", duck);
                } else if (buttonText.contains("帽子")) {
                    updateButtonState(button, "帽子", duck);
                } else if (buttonText.contains("蝴蝶结")) {
                    updateButtonState(button, "蝴蝶结", duck);
                }
            } else if (component instanceof Container) {
                // 递归处理子容器
                refreshButtonsRecursive((Container) component, duck);
            }
        }
    }
    
    //更新单个按钮状态
    private void updateButtonState(JButton button, String clothingName, DuckComponent duck) {
        if (duck.getClothing().contains(clothingName)) {
            button.setText("✓ 已穿");
            button.setBackground(new Color(144, 238, 144));
        } else {
            button.setText("穿上");
            button.setBackground(new Color(173, 216, 230));
        }
    }
    
    //查找父对话框
    private JDialog findParentDialog(Component component) {
        Container parent = component.getParent();
        while (parent != null) {
            if (parent instanceof JDialog) {
                return (JDialog) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}
