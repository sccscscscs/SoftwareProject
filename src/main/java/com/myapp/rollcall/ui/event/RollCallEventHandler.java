package com.myapp.rollcall.ui.event;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import com.myapp.rollcall.model.AttendanceStatus;
import com.myapp.rollcall.model.CallType;
import com.myapp.rollcall.model.RollCallRecord;
import com.myapp.rollcall.model.StrategyType;
import com.myapp.rollcall.model.Student;
import com.myapp.rollcall.model.StudentStatView;
import com.myapp.rollcall.service.NextCall;
import com.myapp.rollcall.service.RollCallService;
import com.myapp.rollcall.ui.SessionHistoryDialog;
import com.myapp.rollcall.ui.SessionStatisticsDialog;
import com.myapp.rollcall.ui.StatisticsDialog;

/**
 * ⚠️脆鼠修改：点名事件处理器类
 * 负责处理所有用户交互事件
 * 应用事件驱动模式，将事件处理逻辑与UI展示分离
 */
public class RollCallEventHandler {
    
    private final RollCallService rollCallService;
    private final RollCallUIController uiController;
    private final AtomicBoolean isRollCalling;
    private long currentSessionId = -1;
    private NextCall currentCall = null;
    
    // ⚠️脆鼠修改：事件监听器接口
    public interface RollCallUIController {
        void updateUIForRollCallStart();
        void updateUIForRollCallEnd();
        void displayStudentInfo(Student student);
        void addToHistoryPanel(Student student, AttendanceStatus status);
        void appendToStatusArea(String message);
        void clearHistoryPanel();
        boolean isVoiceEnabled();
        void showRollCallConfigDialog();
    }
    
    public RollCallEventHandler(RollCallService rollCallService, RollCallUIController uiController, 
                              AtomicBoolean isRollCalling) {
        this.rollCallService = rollCallService;
        this.uiController = uiController;
        this.isRollCalling = isRollCalling;
    }
    
    /**
     * ⚠️脆鼠修改：创建开始/结束点名按钮事件监听器
     */
    public ActionListener createStartEndButtonListener() {
        return e -> {
            if (!isRollCalling.get()) {
                uiController.showRollCallConfigDialog();
            } else {
                endRollCall();
            }
        };
    }
    
    /**
     * ⚠️脆鼠修改：创建考勤状态按钮事件监听器
     */
    public ActionListener createAttendanceButtonListener(AttendanceStatus status) {
        return e -> markAttendance(status);
    }
    
    /**
     * ⚠️脆鼠修改：创建查看统计按钮事件监听器
     */
    public ActionListener createStatsButtonListener() {
        return e -> showStatistics();
    }
    
    /**
     * ⚠️脆鼠修改：创建查看历史记录按钮事件监听器
     */
    public ActionListener createHistoryButtonListener() {
        return e -> showSessionHistory();
    }
    
    /**
     * ⚠️脆鼠修改：创建菜单按钮鼠标监听器
     */
    public MouseAdapter createMenuButtonListener(JPopupMenu menuPopup) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // ⚠️脆鼠修改：显示弹出菜单
                menuPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        };
    }
    
    /**
     * ⚠️脆鼠修改：创建窗口关闭事件监听器
     */
    public WindowAdapter createWindowCloseListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isRollCalling.get()) {
                    int result = JOptionPane.showConfirmDialog(
                        null,
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
            }
        };
    }
    
    /**
     * ⚠️脆鼠修改：开始点名流程
     * @param callType 点名类型
     * @param selectedCount 抽点人数
     * @param strategy 点名策略
     */
    public void startRollCall(CallType callType, Integer selectedCount, StrategyType strategy) throws Exception {
        currentSessionId = rollCallService.startSession(callType, selectedCount, strategy);
        isRollCalling.set(true);
        
        // ⚠️脆鼠修改：更新UI状态
        uiController.updateUIForRollCallStart();
        
        // ⚠️脆鼠修改：清空状态区域
        uiController.appendToStatusArea("=== 开始点名 ===\n");
        uiController.appendToStatusArea("点名类型：" + (callType == CallType.ALL ? "全点" : "抽点(" + selectedCount + "人)") + "\n");
        uiController.appendToStatusArea("点名策略：" + getStrategyDescription(strategy) + "\n");
        uiController.appendToStatusArea("开始时间：" + new Timestamp(System.currentTimeMillis()) + "\n\n");
        
        // ⚠️脆鼠修改：开始点名流程
        nextStudent();
    }
    
    /**
     * ⚠️脆鼠修改：结束点名流程
     */
    private void endRollCall() {
        isRollCalling.set(false);
        currentSessionId = -1;
        currentCall = null;
        
        // ⚠️脆鼠修改：更新UI状态
        uiController.updateUIForRollCallEnd();
        
        uiController.appendToStatusArea("=== 点名结束 ===\n");
        
        // ⚠️脆鼠修改：自动显示本次点名统计结果
        showCurrentSessionStatistics();
        
        // ⚠️脆鼠修改：清空历史记录面板
        uiController.clearHistoryPanel();
    }
    
    /**
     * ⚠️脆鼠修改：点名下一个学生
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
                        JOptionPane.showMessageDialog(null, "点名已完成！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        endRollCall();
                        return;
                    }
                    
                    currentCall = next;
                    uiController.displayStudentInfo(next.getStudent());
                    
                    // 语音播报
                    if (uiController.isVoiceEnabled()) {
                        speakStudentName(next.getStudent().getName());
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "获取下一个学生失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * ⚠️脆鼠修改：标记考勤状态
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
                        
                        uiController.appendToStatusArea(record);
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "⚠️ 迟到功能使用说明：\n" +
                            "1. 先点击'旷课'按钮标记为旷课\n" +
                            "2. 然后点击'转为迟到'按钮转为迟到\n" +
                            "3. 只有在10分钟内才能转为迟到", 
                            "迟到功能说明", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "检查记录状态失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
                
                uiController.appendToStatusArea(record);
            }
            
            // ⚠️脆鼠修改：添加学生到历史记录面板
            uiController.addToHistoryPanel(currentCall.getStudent(), status);
            
            // 点名下一个学生（只有非迟到状态才继续）
            if (status != AttendanceStatus.LATE) {
                nextStudent();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "标记考勤状态失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * ⚠️脆鼠修改：显示统计信息
     */
    private void showStatistics() {
        try {
            List<StudentStatView> stats = rollCallService.getAllStudentStats();
            StatisticsDialog statsDialog = new StatisticsDialog(null, stats);
            statsDialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "获取统计信息失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * ⚠️脆鼠修改：显示点名历史记录
     */
    private void showSessionHistory() {
        try {
            SessionHistoryDialog dialog = new SessionHistoryDialog(null, rollCallService);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "查看历史记录失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * ⚠️脆鼠修改：显示当前会话的统计结果
     */
    private void showCurrentSessionStatistics() {
        try {
            if (currentSessionId != -1) {
                SessionStatisticsDialog dialog = new SessionStatisticsDialog(null, rollCallService, currentSessionId);
                dialog.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "获取点名统计失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * ⚠️脆鼠修改：语音播报学生姓名
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
     * ⚠️脆鼠修改：获取策略描述文本
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
