package com.myapp.rollcall.dao;

import com.myapp.rollcall.db.Db;
import com.myapp.rollcall.model.AttendanceStatus;
import com.myapp.rollcall.model.RollCallRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDao {

    public long insertPending(long sessionId, String studentId, Timestamp callTime) throws SQLException {
        String sql = "INSERT INTO record(session_id, student_id, attendance_status, call_time) VALUES(?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, sessionId);
            ps.setString(2, studentId);
            ps.setString(3, AttendanceStatus.PENDING.name());
            ps.setTimestamp(4, callTime);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("Insert record failed: no generated key.");
            }
        }
    }

    public RollCallRecord findById(long recordId) throws SQLException {
        String sql = "SELECT record_id, session_id, student_id, attendance_status, call_time, response_time, late_time " +
                "FROM record WHERE record_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public void updateStatus(long recordId, AttendanceStatus status, Timestamp responseTime, Integer lateTime) throws SQLException {
        String sql = "UPDATE record SET attendance_status=?, response_time=?, late_time=? WHERE record_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (responseTime == null) ps.setNull(2, Types.TIMESTAMP);
            else ps.setTimestamp(2, responseTime);
            if (lateTime == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, lateTime);
            ps.setLong(4, recordId);
            ps.executeUpdate();
        }
    }

    public List<RollCallRecord> findBySession(long sessionId) throws SQLException {
        String sql = "SELECT record_id, session_id, student_id, attendance_status, call_time, response_time, late_time " +
                "FROM record WHERE session_id=? ORDER BY record_id ASC";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                List<RollCallRecord> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    private RollCallRecord map(ResultSet rs) throws SQLException {
        RollCallRecord r = new RollCallRecord();
        r.setRecordId(rs.getLong("record_id"));
        r.setSessionId(rs.getLong("session_id"));
        r.setStudentId(rs.getString("student_id"));
        r.setAttendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")));
        r.setCallTime(rs.getTimestamp("call_time"));
        r.setResponseTime(rs.getTimestamp("response_time"));
        int lt = rs.getInt("late_time");
        r.setLateTime(rs.wasNull() ? null : lt);
        return r;
    }
}
