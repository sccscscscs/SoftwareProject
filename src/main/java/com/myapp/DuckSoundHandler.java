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
    
    //根据情绪播放对应声音 - 从DuckComponent移动过来的逻辑
    public void playCorrespondingSound(String emotion) {
        try {
            String soundFile;
            switch (emotion) {
                case "happy":
                    soundFile = "/sounds/happy.wav";
                    break;
                case "sad":
                    soundFile = "/sounds/sad.wav";
                    break;
                case "confident":
                    soundFile = "/sounds/confident.wav";
                    break;
                default:
                    return; // 不播放声音
            }
            
            java.net.URL soundUrl = getClass().getResource(soundFile);
            if (soundUrl != null) {
                javax.sound.sampled.AudioInputStream audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(soundUrl);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                
                // 根据[多媒体播放时长控制规范](memory://project_specification/0cc37fa0-34b2-4aff-af16-ccacc70f862c)限制播放时长不超过2秒
                Timer timer = new Timer(2000, e -> { // 2秒后自动停止
                    if (clip.isRunning()) {
                        clip.stop();
                        clip.close();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        } catch (Exception e) {
            System.err.println("播放声音时发生错误: " + e.getMessage());
        }
    }
}
