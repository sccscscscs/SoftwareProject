package com.myapp.rollcall.dao;

import com.myapp.rollcall.db.Db;
import com.myapp.rollcall.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    public List<Student> findAll() throws SQLException {
        String sql = "SELECT student_id, name, gender, class, photo_path FROM student";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public Student findById(String studentId) throws SQLException {
        String sql = "SELECT student_id, name, gender, class, photo_path FROM student WHERE student_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("gender"),
                rs.getString("class"),
                rs.getString("photo_path")
        );
    }
}

