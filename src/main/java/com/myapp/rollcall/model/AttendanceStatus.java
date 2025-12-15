package com.myapp.rollcall.model;

public enum AttendanceStatus {
    PENDING,   // 被点到但尚未确认
    ATTEND,    // 出勤
    LEAVE,     // 请假
    ABSENT,    // 旷课
    LATE       // 迟到
}

