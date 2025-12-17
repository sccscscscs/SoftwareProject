package com.myapp.rollcall.ui.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.myapp.rollcall.ui.components.UIComponentFactory;

/**
 * ⚠️脆鼠修改：点名界面布局管理器类
 * 负责管理所有UI组件的布局和排列
 * 应用建造者模式，灵活配置不同布局方案
 */
public class RollCallLayoutManager {
    
    private final JPanel mainPanel;
    private final JPanel topPanel;
    private final JPanel centerPanel;
    private final JPanel bottomPanel;
    
    // ⚠️脆鼠修改：UI组件引用
    private JLabel titleLabel;
    private JPanel controlPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel statusButtonPanel;
    
    public RollCallLayoutManager() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        topPanel = new JPanel(new BorderLayout(10, 5));
        centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        initializeLayout();
    }
    
    /**
     * ⚠️脆鼠修改：初始化基础布局结构
     */
    private void initializeLayout() {
        // 设置顶部面板边距
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        // 设置中间面板边距
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 设置底部面板边距
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // 组装主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * ⚠️脆鼠修改：设置顶部面板
     * 包含标题和控制按钮区域
     */
    public void setupTopPanel(JLabel title, JPanel controls) {
        this.titleLabel = title;
        this.controlPanel = controls;
        
        // ⚠️脆鼠修改：添加标题到顶部面板
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        // ⚠️脆鼠修改：添加控制面板到顶部面板中心
        topPanel.add(controlPanel, BorderLayout.CENTER);
    }
    
    /**
     * ⚠️脆鼠修改：设置中间面板（左右分区）
     * 左侧为点名区，右侧为历史记录区
     */
    public void setupCenterPanel(JPanel leftArea, JPanel rightArea) {
        this.leftPanel = leftArea;
        this.rightPanel = rightArea;
        
        // 设置左侧面板为点名区
        leftPanel.setBorder(BorderFactory.createTitledBorder("点名区"));
        
        // 设置右侧面板为历史记录区
        rightPanel.setBorder(BorderFactory.createTitledBorder("历史记录区"));
        
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
    }
    
    /**
     * ⚠️脆鼠修改：设置底部面板
     * 包含考勤状态按钮
     */
    public void setupBottomPanel(JPanel statusButtons) {
        this.statusButtonPanel = statusButtons;
        statusButtonPanel.setBorder(BorderFactory.createTitledBorder("考勤状态"));
        
        bottomPanel.add(statusButtonPanel, BorderLayout.CENTER);
    }
    
    /**
     * ⚠️脆鼠修改：创建学生信息展示区域（左侧面板）
     * @return 配置好的学生信息面板
     */
    public JPanel createStudentInfoPanel(JLabel photoLabel, JLabel nameLabel, JLabel idLabel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // ⚠️脆鼠修改：照片放在上方，居中
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 0.7;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(photoLabel, gbc);
        
        // ⚠️脆鼠修改：学生姓名放在照片下方，居中
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 2, 10);
        panel.add(nameLabel, gbc);
        
        // ⚠️脆鼠修改：学号放在姓名下方，居中
        gbc.gridy = 2;
        gbc.weighty = 0.15;
        gbc.insets = new Insets(2, 10, 10, 10);
        panel.add(idLabel, gbc);
        
        return panel;
    }
    
    /**
     * ⚠️脆鼠修改：创建历史记录面板（右侧面板）
     * @return 配置好的历史记录面板
     */
    public JPanel createHistoryPanel(JScrollPane historyScroll, JButton convertButton) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 添加迟到转换按钮到面板顶部
        JPanel convertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        convertPanel.add(new JLabel("旷课学生可转为迟到："));
        convertPanel.add(convertButton);
        panel.add(convertPanel, BorderLayout.NORTH);
        
        // 添加历史记录滚动面板
        panel.add(historyScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * ⚠️脆鼠修改：创建控制面板
     * 包含开始点名、语音播报等控制按钮
     * @param startButton 开始点名按钮
     * @param voiceCheckBox 语音播报复选框
     * @param menuButton 菜单按钮（右上角）
     * @return 配置好的控制面板
     */
    public JPanel createControlPanel(JButton startButton, JCheckBox voiceCheckBox, JButton menuButton) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 左侧：主要控制按钮
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftControls.add(startButton);
        leftControls.add(voiceCheckBox);
        panel.add(leftControls, BorderLayout.WEST);
        
        // 右侧：菜单按钮
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightControls.add(menuButton);
        panel.add(rightControls, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * ⚠️脆鼠修改：创建状态显示区域
     * @return 配置好的状态文本区域
     */
    public JTextArea createStatusArea() {
        JTextArea statusArea = new JTextArea(8, 45);
        statusArea.setFont(new java.awt.Font("JetBrains Mono", java.awt.Font.PLAIN, 12));
        statusArea.setEditable(false);
        statusArea.setBackground(UIComponentFactory.LIGHT_BG);
        statusArea.setForeground(UIComponentFactory.TEXT_PRIMARY);
        statusArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        
        return statusArea;
    }
    
    /**
     * ⚠️脆鼠修改：创建历史记录卡片面板
     * @return 配置好的卡片面板
     */
    public JPanel createHistoryCardPanel() {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        return cardPanel;
    }
    
    /**
     * ⚠️脆鼠修改：创建历史记录滚动面板
     * @param cardPanel 卡片面板
     * @return 配置好的滚动面板
     */
    public JScrollPane createHistoryScrollPanel(JPanel cardPanel) {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("本次点名记录"));
        historyPanel.add(cardPanel, BorderLayout.CENTER);
        
        JScrollPane scrollPanel = new JScrollPane(historyPanel);
        scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPanel.setPreferredSize(new Dimension(300, 400));
        
        return scrollPanel;
    }
    
    /**
     * ⚠️脆鼠修改：获取主面板
     * @return 主面板
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * ⚠️脆鼠修改：获取顶部面板
     * @return 顶部面板
     */
    public JPanel getTopPanel() {
        return topPanel;
    }
    
    /**
     * ⚠️脆鼠修改：获取中间面板
     * @return 中间面板
     */
    public JPanel getCenterPanel() {
        return centerPanel;
    }
    
    /**
     * ⚠️脆鼠修改：获取底部面板
     * @return 底部面板
     */
    public JPanel getBottomPanel() {
        return bottomPanel;
    }
    
    /**
     * ⚠️脆鼠修改：更新控制面板布局
     * 用于在点名过程中更新UI状态
     */
    public void updateControlPanelForRolling(boolean isRolling) {
        if (controlPanel != null) {
            controlPanel.revalidate();
            controlPanel.repaint();
        }
    }
}
