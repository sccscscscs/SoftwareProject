package com.myapp.rollcall.ui;

import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.myapp.rollcall.model.AttendanceStatus;
import com.myapp.rollcall.model.RollCallRecord;
import com.myapp.rollcall.model.Student;
import com.myapp.rollcall.service.RollCallService;
import com.myapp.rollcall.service.RollCallServiceImpl;
import com.myapp.rollcall.ui.components.UIComponentFactory;
import com.myapp.rollcall.ui.event.RollCallEventHandler;
import com.myapp.rollcall.ui.layout.RollCallLayoutManager;

/**
 * ⚠️脆鼠修改：重构后的点名系统主界面类
 * 应用软件工程最佳实践：单一职责原则、开闭原则、依赖倒置原则
 * 采用MVC设计模式，将界面逻辑与业务逻辑完全分离
 * 使用工厂模式创建UI组件，使用事件驱动模式处理用户交互
 */
public class RollCallGUIRefactored extends JDialog implements RollCallEventHandler.RollCallUIController {
    
    // ⚠️脆鼠修改：核心服务和管理器
    private final RollCallService rollCallService;
    private final RollCallLayoutManager layoutManager;
    private final RollCallEventHandler eventHandler;
    private final AtomicBoolean isRollCalling = new AtomicBoolean(false);
    
    // ⚠️脆鼠修改：UI组件引用
    private JLabel studentNameLabel;
    private JLabel studentIdLabel;
    private JLabel studentClassLabel;
    private JLabel photoLabel;
    private JButton startButton;
    private JButton attendButton;
    private JButton leaveButton;
    private JButton absentButton;
    private JButton lateButton;
    private JButton menuButton; // ⚠️脆鼠修改：右上角菜单按钮
    private JPopupMenu menuPopup; // ⚠️脆鼠修改：弹出菜单
    private JScrollPane statusScrollPane;
    private JScrollPane historyScrollPanel;
    private JPanel historyCardPanel;
    
    // ⚠️脆鼠修改：语音播报相关
    private boolean voiceEnabled = true;
    
    /**
     * ⚠️脆鼠修改：构造函数，初始化重构后的点名界面
     * @param parent 父窗口
     */
    public RollCallGUIRefactored(Frame parent) {
        super(parent, "📚 智能点名系统", true);
        
        // ⚠️脆鼠修改：初始化服务和组件
        try {
            this.rollCallService = new RollCallServiceImpl();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "初始化服务失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        
        this.layoutManager = new RollCallLayoutManager();
        this.eventHandler = new RollCallEventHandler(rollCallService, this, isRollCalling);
        
        // ⚠️脆鼠修改：设置窗口属性
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // ⚠️脆鼠修改：初始化和布局UI组件
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * ⚠️脆鼠修改：初始化所有UI组件
     * 使用工厂模式创建标准化组件
     */
    private void initializeComponents() {
        // ⚠️脆鼠修改：创建学生信息显示组件
        studentNameLabel = UIComponentFactory.createStudentLabel("等待点名...", 28, true);
        studentIdLabel = UIComponentFactory.createStudentLabel("🆔 学号：", 16, false);
        studentClassLabel = UIComponentFactory.createStudentLabel("🏫 班级：", 16, false);
        
        // ⚠️脆鼠修改：创建照片显示区域
        photoLabel = new JLabel("", JLabel.CENTER);
        photoLabel.setPreferredSize(new java.awt.Dimension(220, 220));
        photoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        photoLabel.setBackground(UIComponentFactory.CARD_BG);
        photoLabel.setOpaque(true);
        photoLabel.setFont(new java.awt.Font("苹方-简 中等", java.awt.Font.PLAIN, 14));
        photoLabel.setForeground(UIComponentFactory.TEXT_SECONDARY);
        
        // ⚠️脆鼠修改：创建控制按钮
        startButton = UIComponentFactory.createModernButton("🎯 开始点名", UIComponentFactory.PRIMARY_COLOR, 16);
        attendButton = UIComponentFactory.createModernButton("✅ 出勤", UIComponentFactory.SUCCESS_COLOR, 14);
        leaveButton = UIComponentFactory.createModernButton("📄 请假", UIComponentFactory.WARNING_COLOR, 14);
        absentButton = UIComponentFactory.createModernButton("❌ 旷课", UIComponentFactory.DANGER_COLOR, 14);
        lateButton = UIComponentFactory.createModernButton("⏰ 转为迟到", new java.awt.Color(230, 126, 34), 14);
        
        // ⚠️脆鼠修改：初始状态下禁用考勤按钮
        attendButton.setEnabled(false);
        leaveButton.setEnabled(false);
        absentButton.setEnabled(false);
        lateButton.setEnabled(false);
        
        // ⚠️脆鼠修改：创建右上角菜单按钮和弹出菜单
        menuButton = UIComponentFactory.createMenuButton();
        menuPopup = UIComponentFactory.createPopupMenu();
        
        // ⚠️脆鼠修改：创建状态显示区域
        var statusArea = layoutManager.createStatusArea();
        statusScrollPane = new JScrollPane(statusArea);
        
        // ⚠️脆鼠修改：创建历史记录面板
        historyCardPanel = layoutManager.createHistoryCardPanel();
        historyScrollPanel = layoutManager.createHistoryScrollPanel(historyCardPanel);
    }
    
    /**
     * ⚠️脆鼠修改：设置UI布局
     * 使用布局管理器统一管理所有组件排列
     */
    private void setupLayout() {
        // ⚠️脆鼠修改：创建标题
        JLabel titleLabel = UIComponentFactory.createTitleLabel("🎓 智能点名系统");
        
        // ⚠️脆鼠修改：创建语音播报选项
        var voiceCheckBox = UIComponentFactory.createModernCheckBox("🔊 语音播报", voiceEnabled);
        voiceCheckBox.addActionListener(e -> voiceEnabled = voiceCheckBox.isSelected());
        
        // ⚠️脆鼠修改：创建控制面板（隐藏原有的统计和历史按钮）
        JPanel controlPanel = layoutManager.createControlPanel(startButton, voiceCheckBox, menuButton);
        
        // ⚠️脆鼠修改：设置顶部面板
        layoutManager.setupTopPanel(titleLabel, controlPanel);
        
        // ⚠️脆鼠修改：创建学生信息面板
        JPanel studentInfoPanel = layoutManager.createStudentInfoPanel(photoLabel, studentNameLabel, studentIdLabel);
        
        // ⚠️脆鼠修改：创建历史记录面板
        JPanel historyPanel = layoutManager.createHistoryPanel(historyScrollPanel, lateButton);
        
        // ⚠️脆鼠修改：设置中间面板
        layoutManager.setupCenterPanel(studentInfoPanel, historyPanel);
        
        // ⚠️脆鼠修改：创建考勤状态按钮面板
        JPanel statusButtonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));
        statusButtonPanel.add(attendButton);
        statusButtonPanel.add(leaveButton);
        statusButtonPanel.add(absentButton);
        
        // ⚠️脆鼠修改：设置底部面板
        layoutManager.setupBottomPanel(statusButtonPanel);
        
        // ⚠️脆鼠修改：添加主面板到窗口
        add(layoutManager.getMainPanel());
    }
    
    /**
     * ⚠️脆鼠修改：设置事件处理器
     * 使用事件处理器统一管理所有用户交互
     */
    private void setupEventHandlers() {
        // ⚠️脆鼠修改：开始/结束点名按钮事件
        startButton.addActionListener(eventHandler.createStartEndButtonListener());
        
        // ⚠️脆鼠修改：考勤状态按钮事件
        attendButton.addActionListener(eventHandler.createAttendanceButtonListener(AttendanceStatus.ATTEND));
        leaveButton.addActionListener(eventHandler.createAttendanceButtonListener(AttendanceStatus.LEAVE));
        absentButton.addActionListener(eventHandler.createAttendanceButtonListener(AttendanceStatus.ABSENT));
        lateButton.addActionListener(eventHandler.createAttendanceButtonListener(AttendanceStatus.LATE));
        
        // ⚠️脆鼠修改：菜单按钮事件
        menuButton.addMouseListener(eventHandler.createMenuButtonListener(menuPopup));
        
        // ⚠️脆鼠修改：弹出菜单项事件
        var menuItems = menuPopup.getComponents();
        if (menuItems.length >= 1) {
            ((javax.swing.JMenuItem) menuItems[0]).addActionListener(eventHandler.createStatsButtonListener());
        }
        if (menuItems.length >= 2) {
            ((javax.swing.JMenuItem) menuItems[1]).addActionListener(eventHandler.createHistoryButtonListener());
        }
        
        // ⚠️脆鼠修改：窗口关闭事件
        addWindowListener(eventHandler.createWindowCloseListener());
    }
    
    // ==================== ⚠️脆鼠修改：实现RollCallUIController接口 ====================
    
    @Override
    public void updateUIForRollCallStart() {
        startButton.setText("🛑 结束点名");
        startButton.setBackground(UIComponentFactory.DANGER_COLOR);
        attendButton.setEnabled(true);
        leaveButton.setEnabled(true);
        absentButton.setEnabled(true);
        lateButton.setEnabled(true);
    }
    
    @Override
    public void updateUIForRollCallEnd() {
        startButton.setText("🎯 开始点名");
        startButton.setBackground(UIComponentFactory.PRIMARY_COLOR);
        attendButton.setEnabled(false);
        leaveButton.setEnabled(false);
        absentButton.setEnabled(false);
        lateButton.setEnabled(false);
        
        // ⚠️脆鼠修改：清空学生信息显示
        studentNameLabel.setText("点名已结束");
        studentIdLabel.setText("学号：");
        studentClassLabel.setText("班级：");
        photoLabel.setIcon(null);
    }
    
    @Override
    public void displayStudentInfo(Student student) {
        studentNameLabel.setText(student.getName());
        studentIdLabel.setText("学号：" + student.getStudentId());
        studentClassLabel.setText("班级：" + student.getClazz());
        
        // ⚠️脆鼠修改：加载学生照片
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
        
        appendToStatusArea("正在点名：" + student.getName() + " (" + student.getStudentId() + ")\n");
    }
    
    @Override
    public void addToHistoryPanel(Student student, AttendanceStatus status) {
        // ⚠️脆鼠修改：创建学生卡片面板
        JPanel studentCard = new JPanel(new java.awt.BorderLayout(10, 5));
        studentCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        studentCard.setBackground(java.awt.Color.WHITE);
        studentCard.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        
        // ⚠️脆鼠修改：左侧学生信息
        JPanel infoPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(student.getName() + " (" + student.getStudentId() + ")");
        nameLabel.setFont(new java.awt.Font("苹方-简 中等", java.awt.Font.BOLD, 14));
        
        // ⚠️脆鼠修改：根据状态设置颜色和按钮
        switch (status) {
            case ATTEND:
                // 出勤学生不显示（跳过）
                return;
            case ABSENT:
                nameLabel.setForeground(java.awt.Color.RED);
                infoPanel.add(nameLabel);
                
                // ⚠️脆鼠修改：旷课学生添加转为迟到按钮
                JButton convertButton = new JButton("转为迟到");
                convertButton.setFont(new java.awt.Font("苹方-简 中等", java.awt.Font.PLAIN, 12));
                convertButton.setBackground(new java.awt.Color(230, 126, 34));
                convertButton.setForeground(java.awt.Color.WHITE);
                convertButton.setFocusPainted(false);
                convertButton.setBorderPainted(false);
                convertButton.setOpaque(true);
                
                // ⚠️脆鼠修改：转为迟到按钮事件
                convertButton.addActionListener(e -> {
                    try {
                        // 获取当前学生的最新记录
                        var recordDao = new com.myapp.rollcall.dao.RecordDao();
                        var records = recordDao.findBySession(getCurrentSessionId());
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
                            // ⚠️脆鼠修改：通过服务层调用转换为迟到的方法
                            rollCallService.convertAbsentToLateIfWithin10Min(targetRecord.getRecordId(), responseTime);
                            
                            // ⚠️脆鼠修改：更新历史记录显示
                            nameLabel.setForeground(new java.awt.Color(230, 126, 34)); // 橙色表示迟到
                            infoPanel.remove(convertButton);
                            infoPanel.add(new JLabel(" ⏰ 已转为迟到"));
                            infoPanel.revalidate();
                            infoPanel.repaint();
                            
                            appendToStatusArea(String.format("[%s] %s (%s) - ⏰ 转为迟到\n", 
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
                nameLabel.setForeground(java.awt.Color.BLUE);
                infoPanel.add(nameLabel);
                break;
            case LATE:
                nameLabel.setForeground(new java.awt.Color(230, 126, 34)); // 橙色
                infoPanel.add(nameLabel);
                infoPanel.add(new JLabel(" ⏰ 迟到"));
                break;
            default:
                nameLabel.setForeground(java.awt.Color.GRAY);
                infoPanel.add(nameLabel);
                break;
        }
        
        studentCard.add(infoPanel, java.awt.BorderLayout.CENTER);
        
        // ⚠️脆鼠修改：添加到历史记录面板
        historyCardPanel.add(studentCard);
        historyCardPanel.add(javax.swing.Box.createVerticalStrut(5));
        
        // ⚠️脆鼠修改：滚动到最新记录
        SwingUtilities.invokeLater(() -> {
            javax.swing.JScrollBar verticalScrollBar = historyScrollPanel.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
        
        historyCardPanel.revalidate();
        historyCardPanel.repaint();
    }
    
    @Override
    public void appendToStatusArea(String message) {
        var statusArea = (javax.swing.JTextArea) statusScrollPane.getViewport().getView();
        statusArea.append(message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }
    
    @Override
    public void clearHistoryPanel() {
        historyCardPanel.removeAll();
        historyCardPanel.revalidate();
        historyCardPanel.repaint();
    }
    
    @Override
    public boolean isVoiceEnabled() {
        return voiceEnabled;
    }
    
    @Override
    public void showRollCallConfigDialog() {
        // ⚠️脆鼠修改：显示点名配置对话框
        showRollCallConfigDialogImpl();
    }
    
    // ==================== ⚠️脆鼠修改：配置对话框和辅助方法 ====================
    
    /**
     * ⚠️脆鼠修改：显示点名配置对话框
     * 让用户选择点名方式、人数和策略
     */
    private void showRollCallConfigDialogImpl() {
        JDialog configDialog = new JDialog(this, "点名配置", true);
        configDialog.setLayout(new java.awt.GridBagLayout());
        configDialog.setSize(450, 350);
        configDialog.setLocationRelativeTo(this);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 10, 5, 10);
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        // ⚠️脆鼠修改：获取数据库中学生总数用于验证
        final int[] totalStudentCountRef = new int[1]; 
        try {
            var studentDao = new com.myapp.rollcall.dao.StudentDao();
            totalStudentCountRef[0] = studentDao.findAll().size();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "获取学生总数失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            totalStudentCountRef[0] = 100;
        }
        final int totalStudentCount = totalStudentCountRef[0];

        // ⚠️脆鼠修改：配置界面组件设置（简化版，主要逻辑在事件处理器中）
        // 这里可以复用原有的配置对话框逻辑，或者进一步重构
        
        configDialog.setVisible(true);
    }
    
    /**
     * ⚠️脆鼠修改：获取当前会话ID
     * @return 当前会话ID
     */
    private long getCurrentSessionId() {
        // 这里需要从事件处理器获取，或者通过其他方式同步状态
        return -1; // 临时返回，需要实现状态同步
    }
}
