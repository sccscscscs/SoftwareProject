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

import com.myapp.rollcall.model.StudentStatView;

/**
 * ⚠️老鼠修改
 * 统计信息显示对话框类
 * 负责展示学生的考勤统计数据
 * 采用表格形式展示，支持数据排序和筛选
 */
public class StatisticsDialog extends JDialog {
    
    private List<StudentStatView> stats; // ⚠️脆鼠修改：保存统计数据以便导出
    
    /**
     * ⚠️老鼠修改
     * 构造函数，初始化统计对话框
     * @param parent 父窗口
     * @param stats 学生统计数据列表
     */
    public StatisticsDialog(Dialog parent, List<StudentStatView> stats) {
        super(parent, "📊 考勤统计信息", true);
        this.stats = stats; // ⚠️脆鼠修改：保存统计数据
        
        // 设置窗口属性
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 初始化UI组件
        initComponents(stats);
        layoutComponents();
    }
    
    /**
     * ⚠️老鼠修改
     * 初始化UI组件
     * 创建表格模型并填充数据
     * @param stats 统计数据
     */
    private void initComponents(List<StudentStatView> stats) {
        // 创建表格模型
        String[] columnNames = {
            "学号", "姓名", "班级", "总点名次数", 
            "出勤次数", "请假次数", "旷课次数", "迟到次数", "出勤率"
        };
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex < 3 ? String.class : Integer.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };
        
        // 填充数据
        for (StudentStatView stat : stats) {
            Object[] rowData = {
                stat.getStudentId(),
                stat.getName(),
                stat.getClazz(),
                stat.getTotalCalls(),
                stat.getAttendanceCount(),
                stat.getLeaveCount(),
                stat.getAbsenceCount(),
                stat.getLateCount(),
                calculateAttendanceRate(stat)
            };
            model.addRow(rowData);
        }
        
        // 创建表格
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.setRowHeight(25);
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // 学号
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // 姓名
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // 班级
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // 总点名次数
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // 出勤次数
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // 请假次数
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // 旷课次数
        table.getColumnModel().getColumn(7).setPreferredWidth(80);  // 迟到次数
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // 出勤率
        
        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生考勤统计"));
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加统计摘要面板
        JPanel summaryPanel = createSummaryPanel(stats);
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // 添加导出和关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // ⚠️脆鼠修改：添加导出Excel按钮
        JButton exportButton = new JButton("导出为Excel");
        exportButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exportButton.setBackground(new Color(76, 175, 80));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportToExcel());
        buttonPanel.add(exportButton);
        
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        closeButton.setBackground(new Color(158, 158, 158));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * ⚠️老鼠修改
     * 布局UI组件
     * 使用BorderLayout进行整体布局
     */
    private void layoutComponents() {
        // 布局已在initComponents中完成
    }
    
    /**
     * ⚠️老鼠修改
     * 计算出勤率
     * @param stat 学生统计信息
     * @return 出勤率百分比字符串
     */
    private String calculateAttendanceRate(StudentStatView stat) {
        if (stat.getTotalCalls() == 0) {
            return "0%";
        }
        
        int attendedCount = stat.getAttendanceCount() + stat.getLeaveCount() + stat.getLateCount();
        double rate = (double) attendedCount / stat.getTotalCalls() * 100;
        return String.format("%.1f%%", rate);
    }
    
    /**
     * ⚠️老鼠修改
     * 创建统计摘要面板
     * 显示整体的统计信息
     * @param stats 统计数据列表
     * @return 统计摘要面板
     */
    private JPanel createSummaryPanel(List<StudentStatView> stats) {
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("统计摘要"));
        summaryPanel.setBackground(new Color(248, 248, 248));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 计算统计数据
        int totalStudents = stats.size();
        int totalCalls = 0;
        int totalAttendance = 0;
        int totalLeave = 0;
        int totalAbsence = 0;
        int totalLate = 0;
        
        for (StudentStatView stat : stats) {
            totalCalls += stat.getTotalCalls();
            totalAttendance += stat.getAttendanceCount();
            totalLeave += stat.getLeaveCount();
            totalAbsence += stat.getAbsenceCount();
            totalLate += stat.getLateCount();
        }
        
        // 添加统计信息
        gbc.gridx = 0;
        gbc.gridy = 0;
        summaryPanel.add(new JLabel("📊 总学生数：" + totalStudents), gbc);
        
        gbc.gridx = 1;
        summaryPanel.add(new JLabel("📋 总点名次数：" + totalCalls), gbc);
        
        gbc.gridx = 2;
        summaryPanel.add(new JLabel("✅ 总出勤次数：" + totalAttendance), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        summaryPanel.add(new JLabel("📄 总请假次数：" + totalLeave), gbc);
        
        gbc.gridx = 1;
        summaryPanel.add(new JLabel("❌ 总旷课次数：" + totalAbsence), gbc);
        
        gbc.gridx = 2;
        summaryPanel.add(new JLabel("⏰ 总迟到次数：" + totalLate), gbc);
        
        // 计算总体出勤率
        int totalAttended = totalAttendance + totalLeave + totalLate;
        double overallRate = totalCalls > 0 ? (double) totalAttended / totalCalls * 100 : 0;
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        JLabel rateLabel = new JLabel("📈 总体出勤率：" + String.format("%.1f%%", overallRate));
        rateLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        rateLabel.setForeground(new Color(0, 102, 204));
        summaryPanel.add(rateLabel, gbc);
        
        return summaryPanel;
    }
    
    // ⚠️脆鼠修改：实现Excel导出功能
    private void exportToExcel() {
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
            Sheet sheet = workbook.createSheet("考勤统计");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "班级", "总点名次数", 
                               "出勤次数", "请假次数", "旷课次数", "迟到次数", "出勤率"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            for (StudentStatView stat : stats) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(stat.getStudentId());
                row.createCell(1).setCellValue(stat.getName());
                row.createCell(2).setCellValue(stat.getClazz());
                row.createCell(3).setCellValue(stat.getTotalCalls());
                row.createCell(4).setCellValue(stat.getAttendanceCount());
                row.createCell(5).setCellValue(stat.getLeaveCount());
                row.createCell(6).setCellValue(stat.getAbsenceCount());
                row.createCell(7).setCellValue(stat.getLateCount());
                row.createCell(8).setCellValue(calculateAttendanceRate(stat));
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