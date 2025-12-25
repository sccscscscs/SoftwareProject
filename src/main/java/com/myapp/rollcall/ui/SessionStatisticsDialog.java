package com.myapp.rollcall.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.myapp.rollcall.model.AttendanceStatus;
import com.myapp.rollcall.model.CallType;
import com.myapp.rollcall.model.RollCallRecord;
import com.myapp.rollcall.model.Session;
import com.myapp.rollcall.model.StrategyType;
import com.myapp.rollcall.model.Student;
import com.myapp.rollcall.service.RollCallService;

/**
 * 单次点名统计结果对话框
 * 展示特定一次点名会话的详细统计信息
 */
public class SessionStatisticsDialog extends JDialog {
    private RollCallService rollCallService;
    private long sessionId;
    
    public SessionStatisticsDialog(Dialog parent, RollCallService rollCallService, long sessionId) {
        super(parent, "单次点名统计结果", true);
        this.rollCallService = rollCallService;
        this.sessionId = sessionId;
        
        // 设置窗口属性
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        try {
            initUI();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载统计信息失败：" + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initUI() throws Exception {
        // 获取会话信息和记录
        Session session = rollCallService.getSessionById(sessionId);
        List<RollCallRecord> records = rollCallService.getSessionRecords(sessionId);
        
        setLayout(new BorderLayout());
        
        // 顶部面板 - 会话基本信息
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("点名会话信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        headerPanel.add(new JLabel("会话ID: " + sessionId), gbc);
        
        gbc.gridx = 1;
        headerPanel.add(new JLabel("点名时间: " + sdf.format(session.getDate())), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        headerPanel.add(new JLabel("点名类型: " + (session.getCallType() == CallType.ALL ? "全点" : "抽点")), gbc);
        
        gbc.gridx = 1;
        if (session.getSelectedCount() != null) {
            headerPanel.add(new JLabel("抽点人数: " + session.getSelectedCount()), gbc);
        } else {
            headerPanel.add(new JLabel("抽点人数: N/A"), gbc);
        }
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        headerPanel.add(new JLabel("点名策略: " + getStrategyDescription(session.getStrategy())), gbc);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // 中间面板 - 统计表格
        String[] columnNames = {"序号", "学号", "姓名", "班级", "点名时间", "状态", "响应时间", "迟到分钟"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 填充数据
        int index = 1;
        for (RollCallRecord record : records) {
            Student student = rollCallService.getStudentById(record.getStudentId()); // ⚠️脆鼠修改：通过service获取学生信息
            Object[] rowData = {
                index++,
                student.getStudentId(),
                student.getName(),
                student.getClazz(),
                sdf.format(record.getCallTime()),
                getStatusText(record.getAttendanceStatus()),
                record.getResponseTime() != null ? sdf.format(record.getResponseTime()) : "",
                record.getLateTime() != null ? record.getLateTime() : ""
            };
            tableModel.addRow(rowData);
        }
        
        JTable table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.setRowHeight(25);
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // 序号
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // 学号
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // 姓名
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // 班级
        table.getColumnModel().getColumn(4).setPreferredWidth(150);  // 点名时间
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // 状态
        table.getColumnModel().getColumn(6).setPreferredWidth(150);  // 响应时间
        table.getColumnModel().getColumn(7).setPreferredWidth(80);   // 迟到分钟
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("点名详情"));
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部面板 - 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton exportButton = new JButton("导出为Excel");
        exportButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        exportButton.setBackground(new Color(76, 175, 80));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportToExcel(records, session));
        
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        closeButton.setBackground(new Color(158, 158, 158));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(exportButton);
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
    
    private String getStatusText(AttendanceStatus status) {
        switch (status) {
            case ATTEND: return "✅ 出勤";
            case LEAVE: return "📄 请假";
            case ABSENT: return "❌ 旷课";
            case LATE: return "⏰ 迟到";
            case PENDING: return "⏳ 待处理";
            default: return "❓ 未知";
        }
    }
    
    //实现Excel导出功能
    private void exportToExcel(List<RollCallRecord> records, Session session) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存Excel文件");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }
            
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }
            
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("点名记录");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "学号", "姓名", "班级", "点名时间", "状态", "响应时间", "迟到分钟"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            int index = 1;
            for (RollCallRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                Student student = rollCallService.getStudentById(record.getStudentId()); // ⚠️脆鼠修改：通过service获取学生信息
                
                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(student.getStudentId());
                row.createCell(2).setCellValue(student.getName());
                row.createCell(3).setCellValue(student.getClazz());
                row.createCell(4).setCellValue(sdf.format(record.getCallTime()));
                
                String statusText = getStatusText(record.getAttendanceStatus());
                row.createCell(5).setCellValue(statusText);
                
                row.createCell(6).setCellValue(
                    record.getResponseTime() != null ? sdf.format(record.getResponseTime()) : "");
                // ⚠️脆鼠修改：处理迟到时间字段
                if (record.getLateTime() != null) {
                    row.createCell(7).setCellValue(record.getLateTime());
                } else {
                    row.createCell(7).setCellValue("");
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入文件
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            
            JOptionPane.showMessageDialog(this, "导出成功！文件保存在：" + filePath, 
                "导出成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导出失败：" + ex.getMessage(), 
                "导出失败", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}