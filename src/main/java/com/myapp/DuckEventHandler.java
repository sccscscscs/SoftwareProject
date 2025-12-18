package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.myapp.duckbehavior.DuckBehaviorService;
import com.myapp.duckbehavior.DuckRole;
import com.myapp.rollcall.ui.RollCallGUI;

/**
 * ⚠️脆鼠修改：鸭子事件处理器 - 软工思想：事件驱动编程
 * 好处：将事件处理逻辑集中管理，提高代码可维护性
 */
public class DuckEventHandler {
    private final DuckGUI gui;
    private final DuckBehaviorService behaviorService;
    private final DuckAnimationHandler animationHandler;
    private final DuckSoundHandler soundHandler;
    
    public DuckEventHandler(DuckGUI gui, DuckAnimationHandler animationHandler, DuckSoundHandler soundHandler) {
        this.gui = gui;
        this.behaviorService = new DuckBehaviorService();
        this.animationHandler = animationHandler;
        this.soundHandler = soundHandler;
    }
    
    /**
     * ⚠️脆鼠修改：处理鸭子点击事件 - 软工思想：事件处理
     * 好处：统一处理鸭子点击逻辑，包括声音和动画
     * 
     * @param duck 鸭子组件
     * @param duckName 鸭子名称
     */
    public void handleDuckClick(DuckComponent duck, String duckName) {
        // ⚠️脆鼠修改：更新选中状态 - 软工思想：状态管理
        // 好处：确保只有一个鸭子处于选中状态
        gui.setSelectedDuck(duck);
        
        // ⚠️脆鼠修改：为所有鸭子移除边框 - 软工思想：一致性
        // 好处：确保界面视觉一致性
        for (DuckComponent d : gui.getDucks()) {
            d.setBorder(null);
            d.setSelected(false);
        }
        
        // ⚠️脆鼠修改：为当前鸭子添加选中效果 - 软工思想：视觉反馈
        // 好处：提供明确的视觉反馈，让用户知道哪个鸭子被选中
        duck.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 3));
        duck.setSelected(true);
        
        // ⚠️脆鼠修改：获取鸭子行为 - 软工思想：行为驱动
        // 好处：根据鸭子身份获取相应的行为模式
        DuckBehaviorService.DuckBehavior behavior = behaviorService.getBehavior(
            duckName.equals("唐老鸭") ? DuckRole.DONALD : DuckRole.DUCKLING
        );
        
        // ⚠️脆鼠修改：播放鸭子声音 - 软工思想：多媒体反馈
        // 好处：增强用户交互体验
        soundHandler.playDuckSound(behavior);
        
        // ⚠️脆鼠修改：设置鸭子情绪 - 软工思想：状态同步
        // 好处：确保鸭子外观与声音情绪一致
        if (!duckName.equals("唐老鸭")) {
            duck.setEmotion(soundHandler.getEmotionFromSound(behavior.getSound()));
        }
        
        // ⚠️脆鼠修改：执行鸭子动画 - 软工思想：动画反馈
        // 好处：提供生动的动画反馈，增强交互体验
        SwingUtilities.invokeLater(() -> {
            animationHandler.executeDuckAnimation(duck, behavior.getAction());
            
            // ⚠️脆鼠修改：动画结束后显示换装对话框 - 软工思想：顺序执行
            // 好处：确保动画播放完毕后再进行下一步操作
            Timer delayTimer = new Timer(1000, e -> { // 等待1秒动画完成
                // ⚠️脆鼠修改：显示确认对话框 - 软工思想：用户确认机制
                // 好处：避免直接进入换装界面，提升用户体验
                int option = JOptionPane.showConfirmDialog(
                    gui,
                    "是否要进行换装？",
                    "换装确认",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    // ⚠️脆鼠修改：进入换装界面 - 软工思想：功能分离
                    // 好处：将换装功能独立出来，便于维护
                    gui.showDressUpDialog(duck);
                }
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        });
    }
    
    /**
     * ⚠️脆鼠修改：启动点名系统 - 软工思想：模块化设计
     * 好处：将点名系统独立出来，便于维护和扩展
     */
    public void startRollCallSystem() {
        SwingUtilities.invokeLater(() -> {
            try {
                new RollCallGUI(gui).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, 
                    "启动点名系统时发生错误: " + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }
}