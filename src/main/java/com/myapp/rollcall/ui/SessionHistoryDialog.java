package com.myapp.rollcall.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.myapp.rollcall.model.CallType;
import com.myapp.rollcall.model.Session;
import com.myapp.rollcall.model.StrategyType;
import com.myapp.rollcall.service.RollCallService;
//点名历史记录对话框
public class SessionHistoryDialog extends JDialog {
    private RollCallService rollCallService;
    
    public SessionHistoryDialog(Dialog parent, RollCallService rollCallService) {
        super(parent, "点名历史记录", true);
        this.rollCallService = rollCallService;
        
        // 设置窗口属性
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        try {
            initUI();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载历史记录失败：" + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initUI() throws Exception {
        setLayout(new BorderLayout());
        
        // 获取所有会话
        List<Session> sessions = rollCallService.getAllSessions();
        
        // 创建表格模型
        String[] columnNames = {"序号", "会话ID", "点名时间", "点名类型", "点名人数", "点名策略"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 填充数据
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
        int index = 1;
        for (Session session : sessions) {
            String sessionName = String.format("第%d次点名", index);
            String sessionTime = sdf.format(session.getDate());
            String callType = session.getCallType() == CallType.ALL ? "全点" : "抽点";
            String selectedCount = session.getSelectedCount() != null ? 
                String.valueOf(session.getSelectedCount()) : "N/A";
            String strategy = getStrategyDescription(session.getStrategy());
            
            Object[] rowData = {
                sessionName,
                session.getSessionId(),
                sessionTime,
                callType,
                selectedCount,
                strategy
            };
            tableModel.addRow(rowData);
            index++;
        }
        
        JTable table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.setRowHeight(25);
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(100);  // 序号
        table.getColumnModel().getColumn(1).setPreferredWidth(80);   // 会话ID
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // 点名时间
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // 点名类型
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // 点名人数
        table.getColumnModel().getColumn(5).setPreferredWidth(150);  // 点名策略
        
        // 添加双击事件监听器
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        try {
                            long sessionId = (Long) table.getValueAt(selectedRow, 1);
                            SessionStatisticsDialog dialog = new SessionStatisticsDialog(
                                SessionHistoryDialog.this, rollCallService, sessionId);
                            dialog.setVisible(true);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(SessionHistoryDialog.this, 
                                "打开详细统计失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("历史点名记录"));
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部面板 - 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        closeButton.setBackground(new Color(158, 158, 158));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private String getStrategyDescription(StrategyType strategy) {
        switch (strategy) {
            case RANDOM: return "随机选择";
            case MOST_ABSENT: return "优先选择旷课次数最多的同学";
            case LEAST_CALLED: return "优先选择点到次数最少的同学";
            default: return "未知";
        }
    }
}