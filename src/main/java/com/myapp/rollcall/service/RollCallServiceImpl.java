package com.myapp.rollcall.service;

import com.myapp.rollcall.dao.*;
import com.myapp.rollcall.model.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RollCallServiceImpl implements RollCallService {

    private final StudentDao studentDao = new StudentDao();
    private final SessionDao sessionDao = new SessionDao();
    private final RecordDao recordDao = new RecordDao();
    private final StatDao statDao = new StatDao();

    // 内存里保存本次 session 的待点名队列（也可改成每次从DB算，这里先做最常用的）
    private final Map<Long, Deque<String>> sessionQueueMap = new HashMap<>();

    @Override
    public long startSession(CallType callType, Integer selectedCount, StrategyType strategy) throws Exception {
        if (callType == CallType.RANDOM && (selectedCount == null || selectedCount <= 0)) {
            throw new IllegalArgumentException("抽点必须指定 selectedCount > 0");
        }

        Session s = new Session();
        s.setDate(new Timestamp(System.currentTimeMillis()));
        s.setCallType(callType);
        s.setSelectedCount(selectedCount);
        s.setStrategy(strategy);

        long sessionId = sessionDao.insert(s);

        // 1) 拉所有学生
        List<Student> all = studentDao.findAll();
        if (all.isEmpty()) throw new IllegalStateException("student 表为空，无法点名");

        // 2) 确保 stat 都存在
        for (Student st : all) statDao.ensureExists(st.getStudentId());

        // 3) 选人 + 排序
        List<Student> selected = selectStudents(all, callType, selectedCount, strategy);

        // 4) 建队列（只存 studentId）
        Deque<String> q = new ArrayDeque<>();
        for (Student st : selected) q.addLast(st.getStudentId());
        sessionQueueMap.put(sessionId, q);

        return sessionId;
    }

    @Override
    public NextCall nextStudent(long sessionId) throws Exception {
        Deque<String> q = sessionQueueMap.get(sessionId);
        if (q == null) {
            // 允许“重进界面”后再 next：用 record 表已有的 + session 配置重建队列（简化版：直接抛）
            throw new IllegalStateException("未找到该 session 的队列，请先 startSession 或自行实现恢复逻辑");
        }
        if (q.isEmpty()) return null;

        String studentId = q.removeFirst();
        Timestamp callTime = new Timestamp(System.currentTimeMillis());

        // 插入一条 PENDING record
        long recordId = recordDao.insertPending(sessionId, studentId, callTime);

        // 被点到次数 +1
        statDao.incTotalCalls(studentId);

        Student st = studentDao.findById(studentId);
        return new NextCall(recordId, st);
    }

    @Override
    public void markStatus(long recordId, AttendanceStatus status, Timestamp responseTime) throws Exception {
        RollCallRecord r = recordDao.findById(recordId);
        if (r == null) throw new IllegalArgumentException("recordId 不存在: " + recordId);

        // 防止重复刷统计：如果从 PENDING 改为最终态才计数
        AttendanceStatus old = r.getAttendanceStatus();
        if (old != AttendanceStatus.PENDING) {
            // 允许从 ABSENT -> LATE 用专用方法；其它情况直接禁止避免统计错乱
            if (!(old == AttendanceStatus.ABSENT && status == AttendanceStatus.LATE)) {
                throw new IllegalStateException("该记录已不是 PENDING，禁止重复标记：" + old + " -> " + status);
            }
        }

        Integer lateTime = null;
        if (status == AttendanceStatus.LATE) {
            if (responseTime == null) responseTime = new Timestamp(System.currentTimeMillis());
            lateTime = computeLateMinutes(r.getCallTime(), responseTime);
        }

        recordDao.updateStatus(recordId, status, responseTime, lateTime);

        // 更新统计（仅 PENDING -> 最终态）
        if (old == AttendanceStatus.PENDING) {
            switch (status) {
                case ATTEND -> statDao.incAttend(r.getStudentId());
                case LEAVE -> statDao.incLeave(r.getStudentId());
                case ABSENT -> statDao.incAbsent(r.getStudentId());
                case LATE -> statDao.incLate(r.getStudentId());
                default -> { /* ignore */ }
            }
        } else {
            // 如果真需要支持其它转换，必须补“反向扣除统计”的逻辑，这里先保持严格一致性(其实没看懂）
        }
    }

    @Override
    public void convertAbsentToLateIfWithin10Min(long recordId, Timestamp responseTime) throws Exception {
        RollCallRecord r = recordDao.findById(recordId);
        if (r == null) throw new IllegalArgumentException("recordId 不存在: " + recordId);
        if (r.getAttendanceStatus() != AttendanceStatus.ABSENT) {
            throw new IllegalStateException("只有 ABSENT 才能转 LATE，当前=" + r.getAttendanceStatus());
        }
        if (responseTime == null) responseTime = new Timestamp(System.currentTimeMillis());

        int minutes = computeLateMinutes(r.getCallTime(), responseTime);
        if (minutes > 10) {
            throw new IllegalStateException("超过10分钟，不能从旷课转迟到，minutes=" + minutes);
        }

        // 更新 record
        recordDao.updateStatus(recordId, AttendanceStatus.LATE, responseTime, minutes);

        // 统计：ABSENT -> LATE（需要把 absence_count -1，late_count +1）
        // 你 stat 表里没设计“扣减”的方法，这里用直接 SQL 扣一次（保证正确）
        adjustStatAbsentToLate(r.getStudentId());
    }

    @Override
    public List<RollCallRecord> getSessionRecords(long sessionId) throws Exception {
        return recordDao.findBySession(sessionId);
    }

    // ---------------- internal helpers ----------------

    private List<Student> selectStudents(List<Student> all, CallType callType, Integer selectedCount, StrategyType strategy) throws Exception {
        List<Student> base = new ArrayList<>(all);

        if (callType == CallType.ALL) {
            // 全点：按学号排序点名（你图里说“按顺序点名”）
            base.sort(Comparator.comparing(Student::getStudentId));
            return base;
        }

        // 抽点：先按策略排序，再截取 selectedCount
        Map<String, int[]> statMap = statDao.getAllStatAsMap(); // [totalCalls, absenceCount]

        switch (strategy) {
            case RANDOM -> Collections.shuffle(base);
            case MOST_ABSENT -> base.sort((a, b) -> {
                int abA = statMap.getOrDefault(a.getStudentId(), new int[]{0,0})[1];
                int abB = statMap.getOrDefault(b.getStudentId(), new int[]{0,0})[1];
                int cmp = Integer.compare(abB, abA); // desc
                if (cmp != 0) return cmp;
                return a.getStudentId().compareTo(b.getStudentId());
            });
            case LEAST_CALLED -> base.sort((a, b) -> {
                int tcA = statMap.getOrDefault(a.getStudentId(), new int[]{0,0})[0];
                int tcB = statMap.getOrDefault(b.getStudentId(), new int[]{0,0})[0];
                int cmp = Integer.compare(tcA, tcB); // asc
                if (cmp != 0) return cmp;
                return a.getStudentId().compareTo(b.getStudentId());
            });
        }

        int n = Math.min(selectedCount, base.size());
        return new ArrayList<>(base.subList(0, n));
    }

    private int computeLateMinutes(Timestamp callTime, Timestamp responseTime) {
        Instant a = callTime.toInstant();
        Instant b = responseTime.toInstant();
        long minutes = Duration.between(a, b).toMinutes();
        if (minutes < 0) minutes = 0;
        return (int) minutes;
    }

    private void adjustStatAbsentToLate(String studentId) throws Exception {
        // absence_count -1, late_count +1
        // 保守写法：避免减成负数
        String sql = "UPDATE stat SET " +
                "absence_count = CASE WHEN absence_count > 0 THEN absence_count - 1 ELSE 0 END, " +
                "late_count = late_count + 1 " +
                "WHERE student_id = ?";
        try (var c = com.myapp.rollcall.db.Db.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }
}

