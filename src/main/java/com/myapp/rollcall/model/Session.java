package com.myapp.rollcall.model;

import java.sql.Timestamp;

public class Session {
    private long sessionId;
    private Timestamp date;
    private CallType callType;
    private Integer selectedCount;
    private StrategyType strategy;

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    public CallType getCallType() { return callType; }
    public void setCallType(CallType callType) { this.callType = callType; }

    public Integer getSelectedCount() { return selectedCount; }
    public void setSelectedCount(Integer selectedCount) { this.selectedCount = selectedCount; }

    public StrategyType getStrategy() { return strategy; }
    public void setStrategy(StrategyType strategy) { this.strategy = strategy; }
}

