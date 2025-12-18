package com.myapp.duckbehavior;

/**
 * ⚠️脆鼠修改：声音定义 - 支持三种情绪状态
 * 声音定义：文本 + classpath里的wav路径 + 情绪类型
 * wav文件建议放：src/main/resources/sounds/
 * 
 * 修改说明：为支持小鸭子的三种情绪（开心、伤心、自信）
 * 添加了对应的声音文件和描述文本
 */
public enum DuckSound {

    // ⚠️脆鼠修改：唐老鸭声音（保持原有）
    DONALD_QUACK_1("唐老鸭：嘎嘎嘎——（有点嚣张）", "/sounds/donald.wav"),
    DONALD_QUACK_2("唐老鸭：嘎嘎！嘎嘎！", "/sounds/donald.wav"),

    // ⚠️脆鼠修改：小鸭子情绪声音 - 新增三种情绪状态
    // 开心状态
    DUCKLING_HAPPY("小鸭子：嘎嘎嘎！（开心快乐）", "/sounds/happy.wav"),
    
    // 伤心状态  
    DUCKLING_SAD("小鸭子：嘎...（伤心失落）", "/sounds/sad.wav"),
    
    // 自信状态
    DUCKLING_CONFIDENT("小鸭子：嘎！嘎嘎嘎！（自信满满）", "/sounds/confident.wav");

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
    
    /**
     * ⚠️脆鼠修改：获取对应情绪的声音
     * 好处：根据情绪类型快速定位对应声音
     * 
     * @param emotion 情绪类型：happy/sad/confident
     * @return 对应的声音枚举
     */
    public static DuckSound getEmotionSound(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy":
                return DUCKLING_HAPPY;
            case "sad":
                return DUCKLING_SAD;
            case "confident":
                return DUCKLING_CONFIDENT;
            default:
                return DUCKLING_HAPPY; // 默认返回开心声音
        }
    }
}
