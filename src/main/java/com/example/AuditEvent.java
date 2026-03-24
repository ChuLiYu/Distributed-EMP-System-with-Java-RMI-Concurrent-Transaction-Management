package com.example;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class AuditEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private Instant eventTime;
    private String actorId;
    private String actorRole;
    private ActionType action;
    private String targetType;
    private String targetId;
    private ResultType result;
    private String errorCode;
    private String requestId;
    private String sourceIp;

    public enum ActionType {
        CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, LOGIN_FAILED, ACCESS_DENIED
    }

    public enum ResultType {
        SUCCESS, FAIL
    }

    public AuditEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.eventTime = Instant.now();
    }

    public static AuditEvent createLoginEvent(String userId, String role, String requestId) {
        AuditEvent event = new AuditEvent();
        event.setActorId(userId);
        event.setActorRole(role);
        event.setAction(ActionType.LOGIN);
        event.setTargetType("USER");
        event.setTargetId(userId);
        event.setResult(ResultType.SUCCESS);
        event.setRequestId(requestId);
        return event;
    }

    public static AuditEvent createLoginFailedEvent(String username, String requestId, String errorCode) {
        AuditEvent event = new AuditEvent();
        event.setActorId(username);
        event.setActorRole("UNKNOWN");
        event.setAction(ActionType.LOGIN_FAILED);
        event.setTargetType("USER");
        event.setTargetId(username);
        event.setResult(ResultType.FAIL);
        event.setErrorCode(errorCode);
        event.setRequestId(requestId);
        return event;
    }

    public static AuditEvent createAccessDeniedEvent(String userId, String role, String action, String target, String requestId) {
        AuditEvent event = new AuditEvent();
        event.setActorId(userId);
        event.setActorRole(role);
        event.setAction(ActionType.ACCESS_DENIED);
        event.setTargetType(target);
        event.setTargetId(action);
        event.setResult(ResultType.FAIL);
        event.setErrorCode("ACCESS_DENIED");
        event.setRequestId(requestId);
        return event;
    }

    public static AuditEvent createDataEvent(String userId, String role, ActionType action, String targetType, String targetId, ResultType result, String requestId) {
        AuditEvent event = new AuditEvent();
        event.setActorId(userId);
        event.setActorRole(role);
        event.setAction(action);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setResult(result);
        event.setRequestId(requestId);
        return event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public ResultType getResult() {
        return result;
    }

    public void setResult(ResultType result) {
        this.result = result;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Override
    public String toString() {
        return String.format("[AUDIT] %s | %s | %s | %s | %s | %s | %s | %s",
                eventTime, actorId, actorRole, action, targetType, targetId, result, requestId);
    }
}
