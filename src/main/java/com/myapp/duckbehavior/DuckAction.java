package com.myapp.duckbehavior;

/** 行为类型（前端根据类型触发动画） */
public enum DuckAction {
    SHAKE("叉腰抖两下"),
    HOP("原地跳一下"),
    SPIN("转个圈"),
    WAVE("挥挥翅膀");

    private final String text;

    DuckAction(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
