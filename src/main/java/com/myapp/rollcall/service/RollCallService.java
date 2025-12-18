package com.myapp.rollcall.service;

import com.myapp.rollcall.model.*;
import java.sql.Timestamp;
import java.util.List;

public interface RollCallService {

    long startSession(CallType callType, Integer selectedCount, StrategyType strategy) throws Exception;

    //获取下一个被点到的学生（并在 DB 里插入一条 PENDING record，保存 call_time）如果返回 null 说明点名结束

    NextCall nextStudent(long sessionId) throws Exception;

    /**
     * 给某条 record 标记状态
     * - ATTEND/LEAVE/LATE：写 response_time
     * - ABSENT：可不写 response_time（但你也可以写）
     */
    void markStatus(long recordId, AttendanceStatus status, Timestamp responseTime) throws Exception;

    /**
     * “10分钟内由旷课改迟到”：前端如果检测到学生回来了，直接调用这个即可
     * 后端会：
     * - 读 call_time
     * - 计算相差分钟数
     * - <=10 则改成 LATE 并写 late_time
     * - >10 则不允许（抛异常）
     */
    void convertAbsentToLateIfWithin10Min(long recordId, Timestamp responseTime) throws Exception;

    List<RollCallRecord> getSessionRecords(long sessionId) throws Exception;
    
    Session getSessionById(long sessionId) throws Exception;
    
    // ⚠️脆鼠修改：添加获取学生信息的方法
    Student getStudentById(String studentId) throws Exception;
    
    // ⚠️脆鼠修改：添加获取所有会话的方法
    List<Session> getAllSessions() throws Exception;
    
    // 统计汇总：每个人被点次数/出勤/请假/旷课/迟到（前端表格用）可以汇总学生目前的情况
    List<StudentStatView> getAllStudentStats() throws Exception;
}