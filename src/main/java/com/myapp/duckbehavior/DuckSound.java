package com.myapp.duckbehavior;

/**
 * 声音定义：文本 + classpath里的wav路径
 * wav文件建议放：src/main/resources/sounds/
 */
public enum DuckSound {

    // 唐老鸭
    DONALD_QUACK_1("唐老鸭：嘎嘎嘎——（有点嚣张）", "/sounds/donald.wav"),
    DONALD_QUACK_2("唐老鸭：嘎嘎！嘎嘎！", "/sounds/donald.wav"),

    // 小鸭子
    DUCKLING_QUACK_1("小鸭子：嘎~（奶声奶气）", "/sounds/duckling.wav"),
    DUCKLING_QUACK_2("小鸭子：嘎嘎嘎！（超可爱）", "/sounds/duckling.wav");

    private final String text;
    private final String wavPath;

    DuckSound(String text, String wavPath) {
        this.text = text;
        this.wavPath = wavPath;
    }

    public String getText() {
        return text;
    }

    public String getWavPath() {
        return wavPath;
    }
}

