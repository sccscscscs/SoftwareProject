package com.myapp;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.myapp.duckbehavior.DuckAction;


 //单一职责原则，将动画相关功能集中管理，提高代码可维护性
public class DuckAnimationHandler {
    private final DuckGUI gui;
    
    public DuckAnimationHandler(DuckGUI gui) {
        this.gui = gui;
    }

    //执行鸭子动画
     // @param duck 鸭子组件    @param action 鸭子动作
    public void executeDuckAnimation(DuckComponent duck, DuckAction action) {
        switch (action) {
            case SHAKE:
                executeShakeAnimation(duck);
                break;
            case HOP:
                executeHopAnimation(duck);
                break;
            case SPIN:
                executeSpinAnimation(duck);
                break;
            case WAVE:
                executeWaveAnimation(duck);
                break;
            default:
                // 默认执行摇晃动画
                executeShakeAnimation(duck);
                break;
        }
    }
    
    //执行摇晃动画
    //@param duck 鸭子组件
    public void executeShakeAnimation(DuckComponent duck) {
        final int[] shakeOffset = {0};
        final int maxOffset = 5; // 最大偏移量
        final int shakeSpeed = 50; // 摇晃速度（毫秒）
        
        Timer shakeTimer = new Timer(shakeSpeed, e -> {
            shakeOffset[0] += maxOffset;
            
            // 应用偏移量
            duck.setLocation(duck.getX() + (shakeOffset[0] % (maxOffset * 2)) - maxOffset, duck.getY());
            duck.repaint();
            
            // 动画持续一段时间后停止
            if (shakeOffset[0] >= maxOffset * 20) { // 摇晃20次后停止
                duck.setLocation(duck.getX() - (shakeOffset[0] % (maxOffset * 2)) + maxOffset, duck.getY());
                duck.repaint();
                ((Timer) e.getSource()).stop();
            }
        });
        
        shakeTimer.start();
    }
    //执行跳跃动画
    public void executeHopAnimation(DuckComponent duck) {
        final int[] hopOffset = {0};
        final int maxHopHeight = 20; // 最大跳跃高度
        final int hopSpeed = 30; // 跳跃速度（毫秒）
        
        Timer hopTimer = new Timer(hopSpeed, e -> {
// ⚠️脆鼠修改：实现上下跳跃效果 - 软工思想：抛物线动画
            // 好处：模拟真实的跳跃物理效果
            if (hopOffset[0] < maxHopHeight * 2) {
                int offsetY = (int) (maxHopHeight * Math.sin(Math.PI * hopOffset[0] / (maxHopHeight * 2)));
                duck.setLocation(duck.getX(), duck.getY() - offsetY);
                duck.repaint();
                hopOffset[0]++;
            } else {
                // 动画结束，恢复原始位置
                duck.setLocation(duck.getX(), duck.getY());
                duck.repaint();
                ((Timer) e.getSource()).stop();
            }
        });
        
        hopTimer.start();
    }

    //执行旋转动画
    public void executeSpinAnimation(DuckComponent duck) {
        final double[] rotationAngle = {0.0};
        final double rotationStep = 15.0; // 每帧旋转角度
        final int maxFrames = 24; // 旋转360度需要的帧数
        
        Timer spinTimer = new Timer(20, e -> {
            //确保旋转效果平滑
            rotationAngle[0] += rotationStep;
            
            //创建旋转变换
            AffineTransform transform = new AffineTransform();
            transform.rotate(Math.toRadians(rotationAngle[0]), 
                          duck.getWidth() / 2.0, 
                          duck.getHeight() / 2.0);
            
            //应用变换到鸭子组件
            // 注意：这里简化实现，实际可能需要更复杂的图形处理
            duck.repaint(); // 触发重绘，在paintComponent中应用变换
            
            if (rotationAngle[0] >= 360.0) {
                // 动画结束恢复原始状态
                rotationAngle[0] = 0.0;
                ((Timer) e.getSource()).stop();
            }
        });
        
        spinTimer.start();
    }
        
    //执行挥手动画
    public void executeWaveAnimation(DuckComponent duck) {
        // 挥手动画：小幅摇晃 +视觉提示
        executeShakeAnimation(duck);
        
        // 添加视觉提示
        SwingUtilities.invokeLater(() -> {
            // 可以在这里添加特殊的视觉效果，比如星星、音符等
            duck.repaint();
        });
    }
    
    //绘制星星装饰

    public void drawStar(Graphics2D g2d, int x, int y, int size) {
        //使用多边形绘制五角星
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * i / 5 - Math.PI / 2;
            if (i % 2 == 0) {
                // 外角点
                xPoints[i] = x + (int) (size * Math.cos(angle));
                yPoints[i] = y + (int) (size * Math.sin(angle));
            } else {
                // 内角点
                xPoints[i] = x + (int) (size * 0.4 * Math.cos(angle));
                yPoints[i] = y + (int) (size * 0.4 * Math.sin(angle));
            }
        }
        
        g2d.fillPolygon(xPoints, yPoints, 10);
    }
}