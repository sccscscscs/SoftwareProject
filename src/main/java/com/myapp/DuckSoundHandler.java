package com.myapp;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import com.myapp.duckbehavior.DuckBehaviorService;
import com.myapp.duckbehavior.DuckSound;

/**
 * ⚠️脆鼠修改：鸭子声音处理器 - 软工思想：单一职责原则
 * 好处：将声音播放相关功能集中管理，提高代码可维护性
 */
public class DuckSoundHandler {
    private final DuckGUI gui;
    
    public DuckSoundHandler(DuckGUI gui) {
        this.gui = gui;
    }
    
    /**
     * ⚠️脆鼠修改：播放鸭子声音 - 软工思想：音频处理
     * 好处：封装音频播放逻辑，支持限时播放
     * 
     * @param behavior 鸭子行为
     */
    public void playDuckSound(DuckBehaviorService.DuckBehavior behavior) {
        try {
            String soundPath = behavior.getSoundWavPath();
            java.net.URL audioUrl = getClass().getResource(soundPath);
            
            if (audioUrl != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                
                // ⚠️脆鼠修改：限制音频播放时长 - 软工思想：用户体验优化
                // 好处：避免音频播放过长干扰用户操作
                clip.start();
                
                // ⚠️脆鼠修改：设置2秒后自动停止音频 - 软工思想：资源管理
                // 好处：自动释放音频资源，避免长时间占用
                Timer timer = new Timer(2000, e -> { // 2秒后停止
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.close();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                System.err.println("音频文件未找到: " + soundPath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("播放音频时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ⚠️脆鼠修改：从声音类型获取情绪 - 软工思想：状态映射
     * 好处：建立声音和情绪的映射关系，确保状态一致性
     * 
     * @param sound 鸭子声音
     * @return 对应的情绪类型：happy/sad/confident
     */
    public String getEmotionFromSound(DuckSound sound) {
        // ⚠️脆鼠修改：根据声音枚举映射情绪 - 软工思想：策略模式
        // 好处：统一的状态映射逻辑，便于维护
        if (sound == DuckSound.DUCKLING_HAPPY) {
            return "happy";
        } else if (sound == DuckSound.DUCKLING_SAD) {
            return "sad";
        } else if (sound == DuckSound.DUCKLING_CONFIDENT) {
            return "confident";
        }
        // ⚠️脆鼠修改：默认返回开心状态 - 软工思想：防御性编程
        // 好处：确保总有返回值，避免空指针异常
        return "happy";
    }
}