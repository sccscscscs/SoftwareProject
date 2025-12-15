package com.myapp.rollcall.model;

public class Stat {
    private long statId;
    private String studentId;
    private int totalCalls;
    private int attendanceCount;
    private int leaveCount;
    private int absenceCount;
    private int lateCount;

    public long getStatId() { return statId; }
    public void setStatId(long statId) { this.statId = statId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getTotalCalls() { return totalCalls; }
    public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }

    public int getAttendanceCount() { return attendanceCount; }
    public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }

    public int getLeaveCount() { return leaveCount; }
    public void setLeaveCount(int leaveCount) { this.leaveCount = leaveCount; }

    public int getAbsenceCount() { return absenceCount; }
    public void setAbsenceCount(int absenceCount) { this.absenceCount = absenceCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }
}

