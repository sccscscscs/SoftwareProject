package com.myapp;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

import com.myapp.duckbehavior.DuckBehaviorService;
import com.myapp.duckbehavior.DuckSound;

//鸭子声音处理器
public class DuckSoundHandler {
private final DuckGUI gui;
    
    public DuckSoundHandler(DuckGUI gui) {
        this.gui = gui;
    }
    
    //播放鸭子声音
    public void playDuckSound(DuckBehaviorService.DuckBehavior behavior) {
        try {
            String soundPath = behavior.getSoundWavPath();
            java.net.URL audioUrl = getClass().getResource(soundPath);

            if (audioUrl != null) {
                                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioUrl);
                                Clip clip = AudioSystem.getClip();
                                clip.open(audioStream);
                
                //限制音频播放时长
                clip.start();
                
                //设置2秒后自动停止音频
                Timer timer = new Timer(2000, e -> {                 // 2秒后停止
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
    
    //从声音类型获取情绪
    public String getEmotionFromSound(DuckSound sound) {
        //根据声音枚举映射情绪
        if (sound == DuckSound.DUCKLING_HAPPY) {
            return "happy";
        } else if (sound == DuckSound.DUCKLING_SAD) {
            return "sad";
        } else if (sound == DuckSound.DUCKLING_CONFIDENT) {
            return "confident";
        }
        //默认返回开心状态
        return "happy";
    }
}