package com.myapp.rollcall.dao;

import com.myapp.rollcall.db.Db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StatDao {

    /** 保证 stat 存在（student 表插入后可调用一次初始化，也可在点名时懒创建） */
    public void ensureExists(String studentId) throws SQLException {
        String sql =
                "INSERT INTO stat(student_id,total_calls,attendance_count,leave_count,absence_count,late_count) " +
                        "VALUES(?,0,0,0,0,0) " +
                        "ON DUPLICATE KEY UPDATE student_id=student_id";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    public Map<String, int[]> getAllStatAsMap() throws SQLException {
        // map: studentId -> [totalCalls, absenceCount]
        String sql = "SELECT student_id, total_calls, absence_count FROM stat";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, int[]> m = new HashMap<>();
            while (rs.next()) {
                m.put(rs.getString("student_id"),
                        new int[]{rs.getInt("total_calls"), rs.getInt("absence_count")});
            }
            return m;
        }
    }

    public void incTotalCalls(String studentId) throws SQLException {
        ensureExists(studentId);
        String sql = "UPDATE stat SET total_calls = total_calls + 1 WHERE student_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    public void incAttend(String studentId) throws SQLException {
        ensureExists(studentId);
        String sql = "UPDATE stat SET attendance_count = attendance_count + 1 WHERE student_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    public void incLeave(String studentId) throws SQLException {
        ensureExists(studentId);
        String sql = "UPDATE stat SET leave_count = leave_count + 1 WHERE student_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    public void incAbsent(String studentId) throws SQLException {
        ensureExists(studentId);
        String sql = "UPDATE stat SET absence_count = absence_count + 1 WHERE student_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    public void incLate(String studentId) throws SQLException {
        ensureExists(studentId);
        String sql = "UPDATE stat SET late_count = late_count + 1 WHERE student_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }
}
