package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 图表可视化面板
 */
public class ChartPanel extends JPanel {
    
    private List<Integer> data;
    private String chartType; // "bar" 或 "pie"
    private String title;
    
    /**
     * 构造函数
     * @param data 要显示的数据列表
     * @param chartType 图表类型：bar(柱状图) 或 pie(饼图)
     * @param title 图表标题
     */
    public ChartPanel(List<Integer> data, String chartType, String title) {
        this.data = data;
        this.chartType = chartType;
        this.title = title;
        setPreferredSize(new Dimension(500, 400));
        setBackground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 标题
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, 30);
        
        if (data == null || data.isEmpty()) {
            g2d.drawString("无数据", getWidth() / 2 - 20, getHeight() / 2);
            g2d.dispose();
            return;
        }
        
        if ("bar".equals(chartType)) {
            drawBarChart(g2d);
        } else if ("pie".equals(chartType)) {
            drawPieChart(g2d);
        }
        
        g2d.dispose();
    }
    
    /**
     * 绘制柱状图
     * 将函数长度分组，显示每个长度范围的函数数量
     */
    private void drawBarChart(Graphics2D g2d) {
        // 将数据分成5个区间
        int max = data.stream().max(Integer::compareTo).orElse(100);
        int min = data.stream().min(Integer::compareTo).orElse(0);
        int range = max - min;
        int bucketSize = Math.max(1, range / 5);
        
        // 统计每个区间的数量
        int[] buckets = new int[5];
        String[] labels = new String[5];
        
        for (int i = 0; i < 5; i++) {
            int bucketStart = min + i * bucketSize;
            int bucketEnd = (i == 4) ? max : bucketStart + bucketSize;
            labels[i] = bucketStart + "-" + bucketEnd;
            
            for (int value : data) {
                if (i == 4) {
                    if (value >= bucketStart && value <= bucketEnd) {
                        buckets[i]++;
                    }
                } else {
                    if (value >= bucketStart && value < bucketEnd) {
                        buckets[i]++;
                    }
                }
            }
        }
        
        // 绘制柱状图
        int chartX = 60;
        int chartY = 60;
        int chartWidth = getWidth() - 100;
        int chartHeight = getHeight() - 120;
        
        int maxBucket = 0;
        for (int b : buckets) {
            if (b > maxBucket) maxBucket = b;
        }
        if (maxBucket == 0) maxBucket = 1;
        
        // 绘制坐标轴
        g2d.setColor(Color.BLACK);
        g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight); // X轴
        g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight); // Y轴
        
        // 绘制柱子
        int barWidth = chartWidth / (buckets.length * 2);
        Color[] colors = {
            new Color(255, 99, 71),   // 红色
            new Color(255, 165, 0),   // 橙色
            new Color(255, 215, 0),   // 金色
            new Color(60, 179, 113),  // 绿色
            new Color(65, 105, 225)   // 蓝色
        };
        
        for (int i = 0; i < buckets.length; i++) {
            int barHeight = (int) ((buckets[i] / (double) maxBucket) * chartHeight);
            int x = chartX + (i * 2 + 1) * barWidth;
            int y = chartY + chartHeight - barHeight;
            
            g2d.setColor(colors[i]);
            g2d.fillRect(x, y, barWidth, barHeight);
            
            // 绘制边框
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, barWidth, barHeight);
            
            // 绘制数值
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String valueStr = String.valueOf(buckets[i]);
            int valueWidth = g2d.getFontMetrics().stringWidth(valueStr);
            g2d.drawString(valueStr, x + (barWidth - valueWidth) / 2, y - 5);
            
            // 绘制标签
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
            int labelWidth = g2d.getFontMetrics().stringWidth(labels[i]);
            g2d.drawString(labels[i], x + (barWidth - labelWidth) / 2, chartY + chartHeight + 15);
        }
        
        // 绘制Y轴标签
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2d.drawString("函数数量", 10, chartY - 10);
        g2d.drawString("函数长度范围", chartX + chartWidth / 2 - 30, getHeight() - 20);
    }
    
    /**
     * 绘制饼图
     * 按函数长度范围分组，显示占比
     */
    private void drawPieChart(Graphics2D g2d) {
        // 将数据分成5个区间
        int max = data.stream().max(Integer::compareTo).orElse(100);
        int min = data.stream().min(Integer::compareTo).orElse(0);
        int range = max - min;
        int bucketSize = Math.max(1, range / 5);
        
        // 统计每个区间的数量
        int[] buckets = new int[5];
        String[] labels = new String[5];
        
        for (int i = 0; i < 5; i++) {
            int bucketStart = min + i * bucketSize;
            int bucketEnd = (i == 4) ? max : bucketStart + bucketSize;
            labels[i] = bucketStart + "-" + bucketEnd;
            
            for (int value : data) {
                if (i == 4) {
                    if (value >= bucketStart && value <= bucketEnd) {
                        buckets[i]++;
                    }
                } else {
                    if (value >= bucketStart && value < bucketEnd) {
                        buckets[i]++;
                    }
                }
            }
        }
        
        int total = data.size();
        if (total == 0) return;
        
        // 绘制饼图
        int centerX = getWidth() / 2;
        int centerY = (getHeight() + 30) / 2;
        int radius = Math.min(getWidth(), getHeight() - 100) / 3;
        
        Color[] colors = {
            new Color(255, 99, 71),   // 红色
            new Color(255, 165, 0),   // 橙色
            new Color(255, 215, 0),   // 金色
            new Color(60, 179, 113),  // 绿色
            new Color(65, 105, 225)   // 蓝色
        };
        
        int startAngle = 0;
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] == 0) continue;
            
            int arcAngle = (int) Math.round((buckets[i] / (double) total) * 360);
            
            // 绘制扇形
            g2d.setColor(colors[i]);
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                       startAngle, arcAngle);
            
            // 绘制边框
            g2d.setColor(Color.BLACK);
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                       startAngle, arcAngle);
            
            startAngle += arcAngle;
        }
        
        // 绘制图例
        int legendX = centerX + radius + 30;
        int legendY = centerY - radius;
        
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] == 0) continue;
            
            // 颜色块
            g2d.setColor(colors[i]);
            g2d.fillRect(legendX, legendY + i * 25, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, legendY + i * 25, 15, 15);
            
            // 标签和百分比
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            double percentage = (buckets[i] / (double) total) * 100;
            String label = String.format("%s: %d (%.1f%%)", labels[i], buckets[i], percentage);
            g2d.drawString(label, legendX + 20, legendY + i * 25 + 12);
        }
    }
}

