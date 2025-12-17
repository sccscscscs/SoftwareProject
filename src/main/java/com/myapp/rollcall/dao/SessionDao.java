package com.myapp.rollcall.dao;

import com.myapp.rollcall.db.Db;
import com.myapp.rollcall.model.CallType;
import com.myapp.rollcall.model.Session;
import com.myapp.rollcall.model.StrategyType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDao implements AutoCloseable {
    private Connection c;

    public SessionDao() throws SQLException {
        c = Db.getConnection();
    }

    @Override
    public void close() throws SQLException {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    public long insert(Session session) throws SQLException {
        String sql = "INSERT INTO session(date, call_type, selected_count, strategy) VALUES(?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, session.getDate());
            ps.setString(2, session.getCallType().name());
            if (session.getSelectedCount() != null) {
                ps.setInt(3, session.getSelectedCount());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, session.getStrategy().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get generated session id");
            }
        }
    }
    
    public Session findById(long sessionId) throws SQLException {
        String sql = "SELECT session_id, date, call_type, selected_count, strategy FROM session WHERE session_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session session = new Session();
                    session.setSessionId(rs.getLong("session_id"));
                    session.setDate(rs.getTimestamp("date"));
                    session.setCallType(CallType.valueOf(rs.getString("call_type")));
                    int selectedCount = rs.getInt("selected_count");
                    if (!rs.wasNull()) {
                        session.setSelectedCount(selectedCount);
                    }
                    session.setStrategy(StrategyType.valueOf(rs.getString("strategy")));
                    return session;
                }
                return null;
            }
        }
    }
    
    // ⚠️脆鼠修改：添加获取所有会话的方法
    public List<Session> findAll() throws SQLException {
        String sql = "SELECT session_id, date, call_type, selected_count, strategy FROM session ORDER BY date DESC";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            List<Session> sessions = new ArrayList<>();
            while (rs.next()) {
                Session session = new Session();
                session.setSessionId(rs.getLong("session_id"));
                session.setDate(rs.getTimestamp("date"));
                session.setCallType(CallType.valueOf(rs.getString("call_type")));
                int selectedCount = rs.getInt("selected_count");
                if (!rs.wasNull()) {
                    session.setSelectedCount(selectedCount);
                }
                session.setStrategy(StrategyType.valueOf(rs.getString("strategy")));
                sessions.add(session);
            }
            return sessions;
        }
    }
}