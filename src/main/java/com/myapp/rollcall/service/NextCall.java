package com.myapp.rollcall.service;

import com.myapp.rollcall.model.Student;

public class NextCall {
    private final long recordId;
    private final Student student;

    public NextCall(long recordId, Student student) {
        this.recordId = recordId;
        this.student = student;
    }

    public long getRecordId() { return recordId; }
    public Student getStudent() { return student; }
}

