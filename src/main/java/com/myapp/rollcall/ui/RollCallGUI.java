package com.myapp.rollcall.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.myapp.rollcall.model.AttendanceStatus;
import com.myapp.rollcall.model.CallType;
import com.myapp.rollcall.model.RollCallRecord;
import com.myapp.rollcall.model.StrategyType;
import com.myapp.rollcall.model.Student;
import com.myapp.rollcall.model.StudentStatView;
import com.myapp.rollcall.service.NextCall;
import com.myapp.rollcall.service.RollCallService;
import com.myapp.rollcall.service.RollCallServiceImpl;

/**
 * 点名系统主界面类
 * 负责点名流程的UI展示和用户交互
 * 采用MVC设计模式，将界面逻辑与业务逻辑分离
 */
public class RollCallGUI extends JDialog {
    private final RollCallService rollCallService;
    private long currentSessionId = -1;
    private NextCall currentCall = null;
    private final AtomicBoolean isRollCalling = new AtomicBoolean(false);
    
    // UI组件
    private JLabel studentNameLabel;
    private JLabel studentIdLabel;
    private JLabel studentClassLabel;
    private JLabel photoLabel;
    private JButton startButton;
    private JButton attendButton;
    private JButton leaveButton;
    private JButton absentButton;
    private JButton lateButton;
    private JButton viewStatsButton;
    private JButton viewHistoryButton; // ⚠️脆鼠修改：添加查看历史记录按钮
    private JTextArea statusArea;
    
    // ⚠️脆鼠修改：右上角菜单相关组件
    private JButton menuButton; // 菜单按钮（类似微信加号）
    private JPopupMenu menuPopup; // 弹出菜单
    
    // 语音播报相关
    private boolean voiceEnabled = true;
    private JCheckBox voiceCheckBox;
    
    // ⚠️脆鼠修改：历史记录相关组件
    private JPanel historyPanel;
    private JScrollPane historyScrollPanel;
    private JPanel historyCardPanel;
    
    /**
     * 构造函数，初始化点名界面
     * @param parent 父窗口
     */
    public RollCallGUI(Frame parent) {
        super(parent, "📚 智能点名系统", true);
        // ⚠️脆鼠修改：处理SQLException
        try {
            this.rollCallService = new RollCallServiceImpl();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "初始化服务失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        
        // 设置窗口属性
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 初始化UI组件
        initComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    /**
     * 初始化所有UI组件
     * 采用现代化设计风格，提升用户体验
     * 使用渐变色、圆角边框、阴影效果等视觉元素
     */
    private void initComponents() {
        // ⚠️脆鼠修改：定义现代化配色方案
        Color primaryColor = new Color(52, 152, 219);      // 主色调 - 蓝色
        Color successColor = new Color(46, 204, 113);      // 成功色 - 绿色
        Color warningColor = new Color(241, 196, 15);      // 警告色 - 黄色
        Color dangerColor = new Color(231, 76, 60);       // 危险色 - 红色
        Color infoColor = new Color(52, 152, 219);         // 信息色 - 蓝色
        Color lightBg = new Color(248, 249, 250);        // 浅色背景
        Color cardBg = new Color(255, 255, 255);         // 卡片背景
        Color textPrimary = new Color(44, 62, 80);       // 主文本色
        Color textSecondary = new Color(108, 117, 125);    // 次要文本色
        
        // ⚠️脆鼠修改：学生信息显示组件 - 使用卡片式设计
        studentNameLabel = new JLabel("等待点名...", JLabel.CENTER);
        studentNameLabel.setFont(new Font("苹方-简 中等", Font.BOLD, 28));
        studentNameLabel.setForeground(textPrimary);
        
        studentIdLabel = new JLabel("🆔 学号：", JLabel.CENTER);
        studentIdLabel.setFont(new Font("苹方-简 中等", Font.PLAIN, 16));
        studentIdLabel.setForeground(textSecondary);
        
        studentClassLabel = new JLabel("🏫 班级：", JLabel.CENTER);
        studentClassLabel.setFont(new Font("苹方-简 中等", Font.PLAIN, 16));
        studentClassLabel.setForeground(textSecondary);
        
        // ⚠️脆鼠修改：照片显示区域 - 圆角边框，不显示默认文字
        photoLabel = new JLabel("", JLabel.CENTER);
        photoLabel.setPreferredSize(new Dimension(220, 220));
        photoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        photoLabel.setBackground(cardBg);
        photoLabel.setOpaque(true);
        // ⚠️脆鼠修改：移除默认文字，只在照片加载失败时显示
        photoLabel.setFont(new Font("苹方-简 中等", Font.PLAIN, 14));
        photoLabel.setForeground(textSecondary);
        
        // ⚠️脆鼠修改：控制按钮 - 现代化按钮设计，带圆角和悬停效果
        startButton = createModernButton("🎯 开始点名", primaryColor, 16);
        
        attendButton = createModernButton("✅ 出勤", successColor, 14);
        attendButton.setEnabled(false);
        
        leaveButton = createModernButton("📄 请假", warningColor, 14);
        leaveButton.setEnabled(false);
        
        absentButton = createModernButton("❌ 旷课", dangerColor, 14);
        absentButton.setEnabled(false);
        
        // ⚠️脆鼠修改：迟到按钮，特殊处理需要先标记旷课
        lateButton = createModernButton("⏰ 转为迟到", new Color(230, 126, 34), 14);
        lateButton.setEnabled(false);
        
        viewStatsButton = createModernButton("📊 查看统计", infoColor, 14);
        
        // ⚠️脆鼠修改：初始化查看历史记录按钮
        viewHistoryButton = createModernButton("📝 查看点名历史", infoColor, 14);
        
        // ⚠️脆鼠修改：语音播报选项 - 现代化复选框
        voiceCheckBox = new JCheckBox("🔊 语音播报");
        voiceCheckBox.setFont(new Font("苹方-简 中等", Font.PLAIN, 14));
        voiceCheckBox.setSelected(voiceEnabled);
        voiceCheckBox.setBackground(cardBg);
        voiceCheckBox.setForeground(textPrimary);
        
        // ⚠️脆鼠修改：历史记录显示区域 - 本次点名学生列表
        statusArea = new JTextArea(8, 45);
        statusArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(248, 249, 250));
        statusArea.setForeground(textPrimary);
        statusArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        
        // ⚠️脆鼠修改：新增历史记录面板 - 显示本次点名已点名学生
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("本次点名记录"));
        historyScrollPanel = new JScrollPane(historyPanel);
        historyScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        historyScrollPanel.setPreferredSize(new Dimension(300, 400));
        
        // ⚠️脆鼠修改：历史记录卡片面板，用于显示学生信息
        historyCardPanel = new JPanel();
        historyCardPanel.setLayout(new BoxLayout(historyCardPanel, BoxLayout.Y_AXIS));
        historyScrollPanel.setViewportView(historyCardPanel);
    }
    
    /**
     * 创建现代化按钮
     * 统一按钮样式，包含圆角、字体、颜色等属性
     * @param text 按钮文本
     * @param bgColor 背景颜色
     * @param fontSize 字体大小
     * @return 现代化按钮
     */
    private JButton createModernButton(String text, Color bgColor, int fontSize) {
    JButton button = new JButton(text) {
        // ⚠️ 添加：重写绘制方法，实现圆角
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
    
    /**
     * 布局UI组件
     * 使用GridBagLayout实现灵活的响应式布局
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // 顶部面板 - 标题和控制
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("🎓 智能点名系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlPanel.add(startButton);
        controlPanel.add(voiceCheckBox);
        controlPanel.add(viewStatsButton);
        // ⚠️脆鼠修改：添加查看历史记录按钮
        controlPanel.add(viewHistoryButton);
        topPanel.add(controlPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中间主面板 - 分为左右两个区域
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ⚠️脆鼠修改！：左侧面板 - 点名区，重新调整布局
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("点名区"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // ⚠️脆鼠修改！：照片放在上方，居中
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // ⚠️脆鼠修改！：横跨两列
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER; // ⚠️脆鼠修改！：居中对齐
        gbc.weightx = 1.0;
        gbc.weighty = 0.7; // ⚠️脆鼠修改！：给照片区域更多垂直空间
        gbc.fill = GridBagConstraints.NONE; // ⚠️脆鼠修改！：不拉伸，保持原始比例
        gbc.insets = new Insets(10, 10, 10, 10);
        leftPanel.add(photoLabel, gbc);
        
        // ⚠️脆鼠修改！：学生姓名放在照片下方，居中
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // ⚠️脆鼠修改！：横跨两列
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER; // ⚠️脆鼠修改！：居中对齐
        gbc.weightx = 1.0;
        gbc.weighty = 0.15; // ⚠️脆鼠修改！：分配适当的垂直空间
        gbc.fill = GridBagConstraints.HORIZONTAL; // ⚠️脆鼠修改！：水平填满
        gbc.insets = new Insets(5, 10, 2, 10);
        leftPanel.add(studentNameLabel, gbc);
        
        // ⚠️脆鼠修改！：学号放在姓名下方，居中
        gbc.gridy = 2;
        gbc.weighty = 0.15; // ⚠️脆鼠修改！：分配适当的垂直空间
        gbc.insets = new Insets(2, 10, 10, 10);
        leftPanel.add(studentIdLabel, gbc);
        
        // ⚠️脆鼠修改！：班级信息暂时隐藏，因为界面空间有限
        // 如果需要显示，可以考虑在详细信息中展示
        //⚠️脆鼠修改
        // 右侧面板 - 历史记录区
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("历史记录区"));
        
        // 添加迟到转换按钮到右侧面板顶部
        JPanel convertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        convertPanel.add(new JLabel("旷课学生可转为迟到："));
        convertPanel.add(lateButton);
        rightPanel.add(convertPanel, BorderLayout.NORTH);
        
        // ⚠️脆鼠修改：使用新的历史记录面板显示本次点名学生
        rightPanel.add(historyScrollPanel, BorderLayout.CENTER);
        
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 底部面板 - 操作按钮
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // 考勤状态按钮面板
        JPanel statusButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        statusButtonPanel.setBorder(BorderFactory.createTitledBorder("考勤状态"));
        statusButtonPanel.add(attendButton);
        statusButtonPanel.add(leaveButton);
        statusButtonPanel.add(absentButton);
        
        bottomPanel.add(statusButtonPanel, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理器
     * 采用事件驱动编程模式，实现用户交互响应
     */
    private void setupEventHandlers() {
        // 开始点名按钮
        startButton.addActionListener(e -> {
            if (!isRollCalling.get()) {
                showRollCallConfigDialog();
            } else {
                endRollCall();
            }
        });
        
        // 考勤状态按钮
        attendButton.addActionListener(e -> markAttendance(AttendanceStatus.ATTEND));
        leaveButton.addActionListener(e -> markAttendance(AttendanceStatus.LEAVE));
        absentButton.addActionListener(e -> markAttendance(AttendanceStatus.ABSENT));
        lateButton.addActionListener(e -> markAttendance(AttendanceStatus.LATE));
        
        // 查看统计按钮
        viewStatsButton.addActionListener(e -> showStatistics());
        
        // ⚠️脆鼠修改：查看历史记录按钮
        viewHistoryButton.addActionListener(e -> showSessionHistory());
        
        // 语音播报选项
        voiceCheckBox.addActionListener(e -> voiceEnabled = voiceCheckBox.isSelected());
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isRollCalling.get()) {
                    int result = JOptionPane.showConfirmDialog(
                        RollCallGUI.this,
                        "点名正在进行中，确定要退出吗？",
                        "确认退出",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                    endRollCall();
                }
                dispose();
            }
        });
    }
    
    /**
     * 显示点名配置对话框
     * 让用户选择点名方式、人数和策略
     * 修复了全点时仍可选择策略和人数的问题
     * 添加了自定义人数输入验证
     */
    private void showRollCallConfigDialog() {
        JDialog configDialog = new JDialog(this, "点名配置", true);
        configDialog.setLayout(new GridBagLayout());
        configDialog.setSize(450, 350);
        configDialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 获取数据库中学生总数用于验证（添加默认值以防数据库访问失败）
        final int[] totalStudentCountRef = new int[1]; 
        try {
            var studentDao = new com.myapp.rollcall.dao.StudentDao();
            totalStudentCountRef[0] = studentDao.findAll().size();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "获取学生总数失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            totalStudentCountRef[0] = 100;
        }
        final int totalStudentCount = totalStudentCountRef[0];

        // 点名方式选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        configDialog.add(new JLabel("点名方式："), gbc);

        JComboBox<CallType> callTypeCombo = new JComboBox<>(new CallType[]{CallType.ALL, CallType.RANDOM});
        callTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        configDialog.add(callTypeCombo, gbc);

        // 抽点人数选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel countLabel = new JLabel("抽点人数：");
        configDialog.add(countLabel, gbc);

        // ⚠️脆鼠修改！：优化抽点人数选择面板布局，让自定义输入更明显
        JPanel countPanel = new JPanel(new BorderLayout(5, 0)); // ⚠️脆鼠修改！：使用BorderLayout让布局更清晰
        
        // 上半部分：固定选项
        JPanel fixedOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JRadioButton radio10 = new JRadioButton("10人");
        JRadioButton radio15 = new JRadioButton("15人");
        JRadioButton radio20 = new JRadioButton("20人");
        
        ButtonGroup countGroup = new ButtonGroup();
        countGroup.add(radio10);
        countGroup.add(radio15);
        countGroup.add(radio20);
        
        fixedOptionsPanel.add(radio10);
        fixedOptionsPanel.add(radio15);
        fixedOptionsPanel.add(radio20);
        
        // 下半部分：自定义选项（单独一行，更明显）
        JPanel customOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JRadioButton radioCustom = new JRadioButton("自定义");
        JTextField customField = new JTextField(15); // ⚠️脆鼠修改！：进一步增大输入框宽度
        customField.setEnabled(false); // 默认禁用
        customField.setToolTipText("请输入抽点人数"); // ⚠️脆鼠修改！：添加提示信息
        
        countGroup.add(radioCustom);
        
        customOptionsPanel.add(radioCustom);
        customOptionsPanel.add(new JLabel("人数:")); // ⚠️脆鼠修改！：更清晰的标签
        customOptionsPanel.add(customField);
        
        countPanel.add(fixedOptionsPanel, BorderLayout.NORTH);
        countPanel.add(customOptionsPanel, BorderLayout.SOUTH);

        gbc.gridx = 1;
        configDialog.add(countPanel, gbc);

        // 点名策略选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel strategyLabel = new JLabel("点名策略：");
        configDialog.add(strategyLabel, gbc);

        JComboBox<StrategyType> strategyCombo = new JComboBox<>(
            new StrategyType[]{StrategyType.RANDOM, StrategyType.MOST_ABSENT, StrategyType.LEAST_CALLED}
        );
        strategyCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        configDialog.add(strategyCombo, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton confirmButton = new JButton("开始点名");
        JButton cancelButton = new JButton("取消");

        confirmButton.setBackground(new Color(76, 175, 80));
        confirmButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        configDialog.add(buttonPanel, gbc);

        // 修复配置逻辑，全点时完全禁用策略选择和人数选择
        callTypeCombo.addActionListener(e -> {
            boolean isRandom = callTypeCombo.getSelectedItem() == CallType.RANDOM;

            countLabel.setEnabled(isRandom);
            radio10.setEnabled(isRandom);
            radio15.setEnabled(isRandom);
            radio20.setEnabled(isRandom);
            radioCustom.setEnabled(isRandom);
            customField.setEnabled(isRandom && radioCustom.isSelected());

            strategyLabel.setEnabled(isRandom);
            strategyCombo.setEnabled(isRandom);
        });

        // 自定义单选按钮状态变化监听
        radioCustom.addActionListener(e -> {
            customField.setEnabled(radioCustom.isSelected());
            if (radioCustom.isSelected()) {
                customField.requestFocusInWindow();
            }
        });

        // 初始化时设置正确的状态，默认选择全点
        callTypeCombo.setSelectedItem(CallType.ALL);
        countLabel.setEnabled(false);
        strategyLabel.setEnabled(false);
        strategyCombo.setEnabled(false);
        customField.setEnabled(false);

        confirmButton.addActionListener(e -> {
            try {
                CallType callType = (CallType) callTypeCombo.getSelectedItem();
                Integer selectedCount = null;

                if (callType == CallType.RANDOM) {
                    if (radio10.isSelected()) {
                        selectedCount = 10;
                    } else if (radio15.isSelected()) {
                        selectedCount = 15;
                    } else if (radio20.isSelected()) {
                        selectedCount = 20;
                    } else if (radioCustom.isSelected()) {
                        String customText = customField.getText().trim();
                        if (customText.isEmpty()) {
                            JOptionPane.showMessageDialog(configDialog, "请输入自定义人数", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            selectedCount = Integer.parseInt(customText);
                            if (selectedCount <= 0) {
                                JOptionPane.showMessageDialog(configDialog, "人数必须是正数", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            if (selectedCount >= totalStudentCount) {
                                JOptionPane.showMessageDialog(configDialog, "人数必须小于数据库总人数(" + totalStudentCount + "人)", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(configDialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(configDialog, "请选择抽点人数", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                StrategyType strategy = (StrategyType) strategyCombo.getSelectedItem();

                startRollCall(callType, selectedCount, strategy);
                configDialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(configDialog, "启动点名失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> configDialog.dispose());

        configDialog.setVisible(true);
    }
    
    /**
     * 开始点名流程
     * @param callType 点名类型
     * @param selectedCount 抽点人数
     * @param strategy 点名策略
     */
    private void startRollCall(CallType callType, Integer selectedCount, StrategyType strategy) throws Exception {
        currentSessionId = rollCallService.startSession(callType, selectedCount, strategy);
        isRollCalling.set(true);
        
        // 更新UI状态
        startButton.setText("🛑 结束点名");
        startButton.setBackground(new Color(244, 67, 54));
        attendButton.setEnabled(true);
        leaveButton.setEnabled(true);
        absentButton.setEnabled(true);
        lateButton.setEnabled(true);
        
        // 清空状态区域
        statusArea.setText("=== 开始点名 ===\n");
        statusArea.append("点名类型：" + (callType == CallType.ALL ? "全点" : "抽点(" + selectedCount + "人)") + "\n");
        statusArea.append("点名策略：" + getStrategyDescription(strategy) + "\n");
        statusArea.append("开始时间：" + new Timestamp(System.currentTimeMillis()) + "\n\n");
        
        // 开始点名流程
        nextStudent();
    }
    
    /**
     * 结束点名流程
     */
    private void endRollCall() {
        isRollCalling.set(false);
        currentSessionId = -1;
        currentCall = null;
        
        // 更新UI状态
        startButton.setText("🎯 开始点名");
        startButton.setBackground(new Color(76, 175, 80));
        attendButton.setEnabled(false);
        leaveButton.setEnabled(false);
        absentButton.setEnabled(false);
        lateButton.setEnabled(false);
        
        // 清空学生信息显示
        studentNameLabel.setText("点名已结束");
        studentIdLabel.setText("学号：");
        studentClassLabel.setText("班级：");
        photoLabel.setIcon(null);
        
        statusArea.append("=== 点名结束 ===\n");
        
        // ⚠️脆鼠修改：自动显示本次点名统计结果
        showCurrentSessionStatistics();
        
        // ⚠️脆鼠修改：清空历史记录面板
        historyCardPanel.removeAll();
        historyCardPanel.revalidate();
        historyCardPanel.repaint();
    }
    
    /**
     * ⚠️脆鼠修改：新增功能
     * 显示当前会话的统计结果
     */
    private void showCurrentSessionStatistics() {
        try {
            if (currentSessionId != -1) {
                SessionStatisticsDialog dialog = new SessionStatisticsDialog(this, rollCallService, currentSessionId);
                dialog.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "获取点名统计失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 点名下一个学生
     * 采用异步处理，避免UI阻塞
     */
    private void nextStudent() {
        if (!isRollCalling.get()) return;
        
        SwingWorker<NextCall, Void> worker = new SwingWorker<>() {
            @Override
            protected NextCall doInBackground() throws Exception {
                return rollCallService.nextStudent(currentSessionId);
            }
            
            @Override
            protected void done() {
                try {
                    NextCall next = get();
                    if (next == null) {
                        // 点名结束
                        JOptionPane.showMessageDialog(RollCallGUI.this, "点名已完成！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        endRollCall();
                        return;
                    }
                    
                    currentCall = next;
                    displayStudentInfo(next.getStudent());
                    
                    // 语音播报
                    if (voiceEnabled) {
                        speakStudentName(next.getStudent().getName());
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RollCallGUI.this, "获取下一个学生失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 显示学生信息
     * @param student 学生对象
     */
    private void displayStudentInfo(Student student) {
        studentNameLabel.setText(student.getName());
        studentIdLabel.setText("学号：" + student.getStudentId());
        studentClassLabel.setText("班级：" + student.getClazz());
        
        // 加载学生照片
        try {
            String photoPath = "/students_picture/" + new File(student.getPhotoPath()).getName();
            java.net.URL imageUrl = getClass().getResource(photoPath);
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                photoLabel.setText("无照片");
                photoLabel.setIcon(null);
            }
        } catch (Exception ex) {
            photoLabel.setText("照片加载失败");
            photoLabel.setIcon(null);
        }
        
        statusArea.append("正在点名：" + student.getName() + " (" + student.getStudentId() + ")\n");
    }
    
    /**
     * 标记考勤状态
     * 增强记录显示，包含详细的时间戳和状态信息
     * 迟到功能需要先标记为旷课，然后点击"转为迟到"按钮
     * @param status 考勤状态
     */
    private void markAttendance(AttendanceStatus status) {
        if (currentCall == null) return;
        
        try {
            Timestamp responseTime = new Timestamp(System.currentTimeMillis());
            
            if (status == AttendanceStatus.LATE) {
                // ⚠️脆鼠修改：迟到功能说明
                // 迟到需要先标记为旷课，然后点击"转为迟到"按钮
                // 这里通过RecordDao直接检查当前记录状态，只有旷课状态才能转为迟到
                try {
                    var recordDao = new com.myapp.rollcall.dao.RecordDao();
                    RollCallRecord currentRecord = recordDao.findById(currentCall.getRecordId());
                    if (currentRecord != null && currentRecord.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                        rollCallService.convertAbsentToLateIfWithin10Min(currentCall.getRecordId(), responseTime);
                        
                        String record = String.format("[%s] %s (%s) - %s\n", 
                            responseTime.toString().substring(11, 19),
                            currentCall.getStudent().getName(),
                            currentCall.getStudent().getStudentId(),
                            "⏰ 迟到");
                        
                        statusArea.append(record);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "⚠️ 迟到功能使用说明：\n" +
                            "1. 先点击'旷课'按钮标记为旷课\n" +
                            "2. 然后点击'转为迟到'按钮转为迟到\n" +
                            "3. 只有在10分钟内才能转为迟到", 
                            "迟到功能说明", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "检查记录状态失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                rollCallService.markStatus(currentCall.getRecordId(), status, responseTime);
                
                // ⚠️脆鼠修改：增强记录显示格式
                String statusText = "";
                switch (status) {
                    case ATTEND:
                        statusText = "✅ 出勤";
                        break;
                    case LEAVE:
                        statusText = "📄 请假";
                        break;
                    case ABSENT:
                        statusText = "❌ 旷课";
                        break;
                    default:
                        statusText = "❓ 未知";
                        break;
                }
                
                // 详细记录格式：时间 | 学生姓名 | 学号 | 状态
                String record = String.format("[%s] %s (%s) - %s\n", 
                    responseTime.toString().substring(11, 19), // 只显示时间部分
                    currentCall.getStudent().getName(),
                    currentCall.getStudent().getStudentId(),
                    statusText);
                
                statusArea.append(record);
            }
            
            // ⚠️脆鼠修改：添加学生到历史记录面板
            addStudentToHistoryPanel(currentCall.getStudent(), status);
            
            // 点名下一个学生（只有非迟到状态才继续）
            if (status != AttendanceStatus.LATE) {
                nextStudent();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "标记考勤状态失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示统计信息
     */
    private void showStatistics() {
        try {
            List<StudentStatView> stats = rollCallService.getAllStudentStats();
            StatisticsDialog statsDialog = new StatisticsDialog(this, stats);
            statsDialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "获取统计信息失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * ⚠️脆鼠修改：新增功能
     * 显示点名历史记录
     */
    private void showSessionHistory() {
        try {
            SessionHistoryDialog dialog = new SessionHistoryDialog(this, rollCallService);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "查看历史记录失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 语音播报学生姓名
     * 使用系统默认的语音合成功能
     * @param name 学生姓名
     */
    private void speakStudentName(String name) {
        // 在后台线程执行语音播报，避免阻塞UI
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // 使用macOS的say命令进行语音播报
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("mac")) {
                        ProcessBuilder pb = new ProcessBuilder("say", name);
                        pb.start();
                    } else if (os.contains("windows")) {
                        // Windows可以使用PowerShell的Add-Type
                        ProcessBuilder pb = new ProcessBuilder(
                            "powershell", "-Command", 
                            "Add-Type -AssemblyName System.Speech; " +
                            "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" + name + "')"
                        );
                        pb.start();
                    }
                    // Linux系统可以配置espeak或其他TTS引擎
                } catch (Exception ex) {
                    System.err.println("语音播报失败: " + ex.getMessage());
                }
                return null;
            }
        };
        
        worker.execute();
    }
    
    /**
     * 获取策略描述文本
     * @param strategy 策略类型
     * @return 策略描述
     */
    private String getStrategyDescription(StrategyType strategy) {
        return switch (strategy) {
            case RANDOM -> "随机选择";
            case MOST_ABSENT -> "优先选择旷课次数最多的同学";
            case LEAST_CALLED -> "优先选择点到次数最少的同学";
        };
    }
    
    /**
     * ⚠️脆鼠修改：新增功能
     * 添加学生到历史记录面板
     * 根据考勤状态显示不同的颜色和按钮
     * @param student 学生对象
     * @param status 考勤状态
     */
    private void addStudentToHistoryPanel(Student student, AttendanceStatus status) {
        // ⚠️脆鼠修改：创建学生卡片面板
        JPanel studentCard = new JPanel(new BorderLayout(10, 5));
        studentCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        studentCard.setBackground(Color.WHITE);
        studentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        
        // 左侧：学生信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(student.getName() + " (" + student.getStudentId() + ")");
        nameLabel.setFont(new Font("苹方-简 中等", Font.BOLD, 14));
        
        // ⚠️脆鼠修改：根据状态设置颜色
        switch (status) {
            case ATTEND:
                // 出勤学生不显示（跳过）
                return;
            case ABSENT:
                nameLabel.setForeground(Color.RED);
                infoPanel.add(nameLabel);
                
                // ⚠️脆鼠修改：旷课学生添加转为迟到按钮
                JButton convertButton = new JButton("转为迟到");
                convertButton.setFont(new Font("苹方-简 中等", Font.PLAIN, 12));
                convertButton.setBackground(new Color(230, 126, 34));
                convertButton.setForeground(Color.WHITE);
                convertButton.setFocusPainted(false);
                convertButton.setBorderPainted(false);
                convertButton.setOpaque(true);
                
                // ⚠️脆鼠修改：转为迟到按钮事件
                convertButton.addActionListener(e -> {
                    try {
                        // 获取当前学生的最新记录
                        var recordDao = new com.myapp.rollcall.dao.RecordDao();
                        var records = recordDao.findBySession(currentSessionId);
                        RollCallRecord targetRecord = null;
                        
                        for (RollCallRecord record : records) {
                            if (record.getStudentId().equals(student.getStudentId()) && 
                                record.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                                targetRecord = record;
                                break;
                            }
                        }
                        
                        if (targetRecord != null) {
                            Timestamp responseTime = new Timestamp(System.currentTimeMillis());
                            rollCallService.convertAbsentToLateIfWithin10Min(targetRecord.getRecordId(), responseTime);
                            
                            // ⚠️脆鼠修改：更新历史记录显示
                            nameLabel.setForeground(new Color(230, 126, 34)); // 橙色表示迟到
                            infoPanel.remove(convertButton);
                            infoPanel.add(new JLabel(" ⏰ 已转为迟到"));
                            infoPanel.revalidate();
                            infoPanel.repaint();
                            
                            statusArea.append(String.format("[%s] %s (%s) - ⏰ 转为迟到\n", 
                                responseTime.toString().substring(11, 19),
                                student.getName(),
                                student.getStudentId()));
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "转为迟到失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                infoPanel.add(convertButton);
                break;
            case LEAVE:
                nameLabel.setForeground(Color.BLUE);
                infoPanel.add(nameLabel);
                break;
            case LATE:
                nameLabel.setForeground(new Color(230, 126, 34)); // 橙色
                infoPanel.add(nameLabel);
                infoPanel.add(new JLabel(" ⏰ 迟到"));
                break;
            default:
                nameLabel.setForeground(Color.GRAY);
                infoPanel.add(nameLabel);
                break;
        }
        
        studentCard.add(infoPanel, BorderLayout.CENTER);
        
        // ⚠️脆鼠修改：添加到历史记录面板
        historyCardPanel.add(studentCard);
        historyCardPanel.add(Box.createVerticalStrut(5));
        
        // ⚠️脆鼠修改：滚动到最新记录
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = historyScrollPanel.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
        
        historyCardPanel.revalidate();
        historyCardPanel.repaint();
    }
}
