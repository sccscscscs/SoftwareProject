package com.myapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.myapp.duckbehavior.DuckBehaviorService;
import com.myapp.duckbehavior.DuckRole;
import com.myapp.rollcall.ui.RollCallGUI;

//鸭子事件处理器

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
    
    //处理鸭子点击事件
    public void handleDuckClick(DuckComponent duck, String duckName) {
        //更新选中状态
        gui.setSelectedDuck(duck);
        
        //为所有鸭子移除边框
        for (DuckComponent d : gui.getDucks()) {
            d.setBorder(null);
            d.setSelected(false);
        }
        
        //为当前鸭子添加选中效果
        duck.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 3));
        duck.setSelected(true);
        
        //获取鸭子行为
        DuckBehaviorService.DuckBehavior behavior = behaviorService.getBehavior(
            duckName.equals("唐老鸭") ? DuckRole.DONALD : DuckRole.DUCKLING
        );
        
        //播放鸭子声音
        soundHandler.playDuckSound(behavior);
        
        //设置鸭子情绪
        if (!duckName.equals("唐老鸭")) {
            duck.setEmotion(soundHandler.getEmotionFromSound(behavior.getSound()));
        }
        
        //执行鸭子动画
        SwingUtilities.invokeLater(() -> {
            animationHandler.executeDuckAnimation(duck, behavior.getAction());
            
            //动画结束后显示换装对话框
            Timer delayTimer = new Timer(1000, e -> { // 等待1秒动画完成
                //显示确认对话框
                int option = JOptionPane.showConfirmDialog(
                    gui,
                    "是否要进行换装？",
                    "换装确认",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    //进入换装界面
                    gui.showDressUpDialog(duck);
                }
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        });
    }
    
    //启动点名系统
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