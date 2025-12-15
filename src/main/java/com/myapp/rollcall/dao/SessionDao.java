package com.myapp.rollcall.dao;

import com.myapp.rollcall.db.Db;
import com.myapp.rollcall.model.CallType;
import com.myapp.rollcall.model.Session;
import com.myapp.rollcall.model.StrategyType;

import java.sql.*;

public class SessionDao {

    public long insert(Session s) throws SQLException {
        String sql = "INSERT INTO session(date, call_type, selected_count, strategy) VALUES(?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, s.getDate());
            ps.setString(2, s.getCallType().name());
            if (s.getSelectedCount() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, s.getSelectedCount());
            ps.setString(4, s.getStrategy().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("Insert session failed: no generated key.");
            }
        }
    }

    public Session findById(long sessionId) throws SQLException {
        String sql = "SELECT session_id, date, call_type, selected_count, strategy FROM session WHERE session_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Session s = new Session();
                s.setSessionId(rs.getLong("session_id"));
                s.setDate(rs.getTimestamp("date"));
                s.setCallType(CallType.valueOf(rs.getString("call_type")));
                int sc = rs.getInt("selected_count");
                s.setSelectedCount(rs.wasNull() ? null : sc);
                s.setStrategy(StrategyType.valueOf(rs.getString("strategy")));
                return s;
            }
        }
    }
}
