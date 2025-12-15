package com.myapp.rollcall.model;

import java.sql.Timestamp;

public class RollCallRecord {
    private long recordId;
    private long sessionId;
    private String studentId;
    private AttendanceStatus attendanceStatus;
    private Timestamp callTime;
    private Timestamp responseTime;
    private Integer lateTime;

    public long getRecordId() { return recordId; }
    public void setRecordId(long recordId) { this.recordId = recordId; }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public Timestamp getCallTime() { return callTime; }
    public void setCallTime(Timestamp callTime) { this.callTime = callTime; }

    public Timestamp getResponseTime() { return responseTime; }
    public void setResponseTime(Timestamp responseTime) { this.responseTime = responseTime; }

    public Integer getLateTime() { return lateTime; }
    public void setLateTime(Integer lateTime) { this.lateTime = lateTime; }
}
