package com.myapp.rollcall.model;

public enum StrategyType {
    RANDOM,        // 随机
    MOST_ABSENT,   // 优先旷课次数最多
    LEAST_CALLED   // 优先被点到次数最少
}
