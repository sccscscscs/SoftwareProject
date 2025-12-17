package com.myapp.rollcall.ui;

import com.myapp.rollcall.model.*;
import com.myapp.rollcall.service.RollCallService;
import com.myapp.rollcall.service.RollCallServiceImpl;
import com.myapp.rollcall.service.NextCall;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ⚠️老鼠修改
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
    private JTextArea statusArea;
    
    // 语音播报相关
    private boolean voiceEnabled = true;
    private JCheckBox voiceCheckBox;
    
    /**
     * ⚠️老鼠修改
     * 构造函数，初始化点名界面
     * @param parent 父窗口
     */
    public RollCallGUI(Frame parent) {
        super(parent, "📚 智能点名系统", true);
        this.rollCallService = new RollCallServiceImpl();
        
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
     * ⚠️老鼠修改
     * 初始化所有UI组件
     * 采用组件化思想，便于维护和扩展
     */
    private void initComponents() {
        // 学生信息显示组件
        studentNameLabel = new JLabel("等待点名...", JLabel.CENTER);
        studentNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        studentNameLabel.setForeground(new Color(0, 102, 204));
        
        studentIdLabel = new JLabel("学号：", JLabel.CENTER);
        studentIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        
        studentClassLabel = new JLabel("班级：", JLabel.CENTER);
        studentClassLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        
        photoLabel = new JLabel("", JLabel.CENTER);
        photoLabel.setPreferredSize(new Dimension(200, 200));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        photoLabel.setBackground(Color.WHITE);
        photoLabel.setOpaque(true);
        
        // 控制按钮
        startButton = new JButton("🎯 开始点名");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        startButton.setBackground(new Color(76, 175, 80));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        
        attendButton = new JButton("✅ 出勤");
        attendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        attendButton.setBackground(new Color(76, 175, 80));
        attendButton.setForeground(Color.WHITE);
        attendButton.setEnabled(false);
        
        leaveButton = new JButton("📄 请假");
        leaveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        leaveButton.setBackground(new Color(255, 193, 7));
        leaveButton.setForeground(Color.WHITE);
        leaveButton.setEnabled(false);
        
        absentButton = new JButton("❌ 旷课");
        absentButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        absentButton.setBackground(new Color(244, 67, 54));
        absentButton.setForeground(Color.WHITE);
        absentButton.setEnabled(false);
        
        lateButton = new JButton("⏰ 迟到(10分钟内)");
        lateButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lateButton.setBackground(new Color(255, 152, 0));
        lateButton.setForeground(Color.WHITE);
        lateButton.setEnabled(false);
        
        viewStatsButton = new JButton("📊 查看统计");
        viewStatsButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        viewStatsButton.setBackground(new Color(33, 150, 243));
        viewStatsButton.setForeground(Color.WHITE);
        viewStatsButton.setFocusPainted(false);
        
        // 语音播报选项
        voiceCheckBox = new JCheckBox("🔊 启用语音播报");
        voiceCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        voiceCheckBox.setSelected(voiceEnabled);
        
        // 状态显示区域
        statusArea = new JTextArea(6, 40);
        statusArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(248, 248, 248));
        statusArea.setBorder(BorderFactory.createTitledBorder("点名状态记录"));
    }
    
    /**
     * ⚠️老鼠修改
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
        topPanel.add(controlPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中间面板 - 学生信息显示
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 照片
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.insets = new Insets(5, 5, 5, 20);
        centerPanel.add(photoLabel, gbc);
        
        // 学生姓名
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        centerPanel.add(studentNameLabel, gbc);
        
        // 学号
        gbc.gridy = 1;
        centerPanel.add(studentIdLabel, gbc);
        
        // 班级
        gbc.gridy = 2;
        centerPanel.add(studentClassLabel, gbc);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部面板 - 操作按钮和状态
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // 考勤状态按钮面板
        JPanel statusButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        statusButtonPanel.setBorder(BorderFactory.createTitledBorder("考勤状态"));
        statusButtonPanel.add(attendButton);
        statusButtonPanel.add(leaveButton);
        statusButtonPanel.add(absentButton);
        statusButtonPanel.add(lateButton);
        
        bottomPanel.add(statusButtonPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * ⚠️老鼠修改
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
     * ⚠️老鼠修改
     * 显示点名配置对话框
     * 让用户选择点名方式、人数和策略
     */
    private void showRollCallConfigDialog() {
        JDialog configDialog = new JDialog(this, "点名配置", true);
        configDialog.setLayout(new GridBagLayout());
        configDialog.setSize(400, 300);
        configDialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
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
        countLabel.setEnabled(false);
        configDialog.add(countLabel, gbc);
        
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JRadioButton radio10 = new JRadioButton("10人");
        JRadioButton radio15 = new JRadioButton("15人");
        JRadioButton radio20 = new JRadioButton("20人");
        JRadioButton radioCustom = new JRadioButton("自定义");
        JTextField customField = new JTextField(5);
        
        ButtonGroup countGroup = new ButtonGroup();
        countGroup.add(radio10);
        countGroup.add(radio15);
        countGroup.add(radio20);
        countGroup.add(radioCustom);
        
        countPanel.add(radio10);
        countPanel.add(radio15);
        countPanel.add(radio20);
        countPanel.add(radioCustom);
        countPanel.add(new JLabel("数量:"));
        countPanel.add(customField);
        
        gbc.gridx = 1;
        configDialog.add(countPanel, gbc);
        
        // 点名策略选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        configDialog.add(new JLabel("点名策略："), gbc);
        
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
        
        // 事件处理
        callTypeCombo.addActionListener(e -> {
            boolean isRandom = callTypeCombo.getSelectedItem() == CallType.RANDOM;
            countLabel.setEnabled(isRandom);
            radio10.setEnabled(isRandom);
            radio15.setEnabled(isRandom);
            radio20.setEnabled(isRandom);
            radioCustom.setEnabled(isRandom);
            customField.setEnabled(isRandom);
        });
        
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
                        selectedCount = Integer.parseInt(customText);
                        if (selectedCount <= 0) {
                            JOptionPane.showMessageDialog(configDialog, "人数必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(configDialog, "请选择抽点人数", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                StrategyType strategy = (StrategyType) strategyCombo.getSelectedItem();
                
                // 开始点名
                startRollCall(callType, selectedCount, strategy);
                configDialog.dispose();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(configDialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(configDialog, "启动点名失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> configDialog.dispose());
        
        configDialog.setVisible(true);
    }
    
    /**
     * ⚠️老鼠修改
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
     * ⚠️老鼠修改
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
    }
    
    /**
     * ⚠️老鼠修改
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
     * ⚠️老鼠修改
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
     * ⚠️老鼠修改
     * 标记考勤状态
     * @param status 考勤状态
     */
    private void markAttendance(AttendanceStatus status) {
        if (currentCall == null) return;
        
        try {
            Timestamp responseTime = new Timestamp(System.currentTimeMillis());
            
            if (status == AttendanceStatus.LATE) {
                // 迟到需要特殊处理
                rollCallService.convertAbsentToLateIfWithin10Min(currentCall.getRecordId(), responseTime);
            } else {
                rollCallService.markStatus(currentCall.getRecordId(), status, responseTime);
            }
            
            // 更新状态显示
            String statusText = switch (status) {
                case ATTEND -> "出勤";
                case LEAVE -> "请假";
                case ABSENT -> "旷课";
                case LATE -> "迟到";
                default -> "未知";
            };
            
            statusArea.append(currentCall.getStudent().getName() + " - " + statusText + "\n");
            
            // 点名下一个学生
            nextStudent();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "标记考勤状态失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * ⚠️老鼠修改
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
     * ⚠️老鼠修改
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
     * ⚠️老鼠修改
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
}
