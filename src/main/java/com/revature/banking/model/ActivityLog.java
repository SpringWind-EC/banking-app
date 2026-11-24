package com.revature.banking.model;

import java.time.*;
import java.util.*;

public class ActivityLog {
    long employeeId;
    String actionType;
    long accountId;
    long customerId;
    Instant occurredAt;
    String details;

    public ActivityLog(long employeeId, String actionType, long accountId,
            long customerId, Instant occurredAt, String details) {
        this.employeeId = employeeId;
        this.actionType = actionType;
        this.accountId = accountId;
        this.customerId = customerId;
        this.occurredAt = occurredAt;
        this.details = details;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
