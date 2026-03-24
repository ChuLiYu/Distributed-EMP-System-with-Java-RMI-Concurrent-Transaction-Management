package com.example;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuditService {
    private static AuditService instance;
    private final ConcurrentLinkedQueue<AuditEvent> auditQueue;
    private final ExecutorService executorService;
    private boolean initialized = false;

    private AuditService() {
        auditQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AuditWriter");
            t.setDaemon(true);
            return t;
        });
        startAuditWriter();
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    private void startAuditWriter() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    AuditEvent event = auditQueue.poll();
                    if (event != null) {
                        writeAuditEvent(event);
                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error writing audit event: " + e.getMessage());
                }
            }
        });
    }

    public void logEvent(AuditEvent event) {
        if (!initialized) {
            initSchema();
        }
        auditQueue.offer(event);
        System.out.println(event.toString());
    }

    public void logEventSync(AuditEvent event) {
        if (!initialized) {
            initSchema();
        }
        writeAuditEvent(event);
        System.out.println(event.toString());
    }

    private void writeAuditEvent(AuditEvent event) {
        String sql = "INSERT INTO AUDIT_LOG (event_id, event_time, actor_id, actor_role, action, target_type, target_id, result, error_code, request_id, source_ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getEventId());
            pstmt.setTimestamp(2, Timestamp.from(event.getEventTime()));
            pstmt.setString(3, event.getActorId());
            pstmt.setString(4, event.getActorRole());
            pstmt.setString(5, event.getAction().name());
            pstmt.setString(6, event.getTargetType());
            pstmt.setString(7, event.getTargetId());
            pstmt.setString(8, event.getResult().name());
            pstmt.setString(9, event.getErrorCode());
            pstmt.setString(10, event.getRequestId());
            pstmt.setString(11, event.getSourceIp());

            pstmt.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Error writing audit to database: " + e.getMessage());
            logToFile(event);
        }
    }

    private void logToFile(AuditEvent event) {
        System.err.println("[AUDIT_FALLBACK] " + event.toString());
    }

    public List<AuditEvent> getAuditLogs(String targetType, String targetId, int limit) {
        List<AuditEvent> events = new ArrayList<>();
        
        if (!initialized) {
            initSchema();
        }

        String sql = "SELECT * FROM AUDIT_LOG WHERE target_type = ? AND target_id = ? ORDER BY event_time DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, targetType);
            pstmt.setString(2, targetId);
            pstmt.setInt(3, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(mapResultSetToAuditEvent(rs));
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error reading audit logs: " + e.getMessage());
        }

        return events;
    }

    public List<AuditEvent> getAuditLogsByActor(String actorId, int limit) {
        List<AuditEvent> events = new ArrayList<>();
        
        if (!initialized) {
            initSchema();
        }

        String sql = "SELECT * FROM AUDIT_LOG WHERE actor_id = ? ORDER BY event_time DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, actorId);
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(mapResultSetToAuditEvent(rs));
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error reading audit logs: " + e.getMessage());
        }

        return events;
    }

    private AuditEvent mapResultSetToAuditEvent(ResultSet rs) throws SQLException {
        AuditEvent event = new AuditEvent();
        event.setEventId(rs.getString("event_id"));
        
        Timestamp ts = rs.getTimestamp("event_time");
        if (ts != null) {
            event.setEventTime(ts.toInstant());
        }
        
        event.setActorId(rs.getString("actor_id"));
        event.setActorRole(rs.getString("actor_role"));
        event.setAction(AuditEvent.ActionType.valueOf(rs.getString("action")));
        event.setTargetType(rs.getString("target_type"));
        event.setTargetId(rs.getString("target_id"));
        event.setResult(AuditEvent.ResultType.valueOf(rs.getString("result")));
        event.setErrorCode(rs.getString("error_code"));
        event.setRequestId(rs.getString("request_id"));
        event.setSourceIp(rs.getString("source_ip"));
        
        return event;
    }

    public void initSchema() {
        if (initialized) {
            return;
        }

        String createTableSql = """
            CREATE TABLE IF NOT EXISTS AUDIT_LOG (
                event_id TEXT PRIMARY KEY,
                event_time TIMESTAMP NOT NULL,
                actor_id TEXT NOT NULL,
                actor_role TEXT,
                action TEXT NOT NULL,
                target_type TEXT NOT NULL,
                target_id TEXT,
                result TEXT NOT NULL,
                error_code TEXT,
                request_id TEXT,
                source_ip TEXT
            )
            """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSql);
            conn.commit();
            initialized = true;
            System.out.println("Audit log table initialized");

        } catch (SQLException e) {
            System.err.println("Error initializing audit log table: " + e.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
