package com.revature.banking.model;

import java.util.*;
import java.time.*;

public class AccountNote {

    long accountId;
    long employeeId;
    String category;
    String text;
    Instant createdAt;
    long relatedTransactionId;
    String[] tags;

    public AccountNote(long accountId, long employeeId, String category,
            String text, Instant createdAt, Long relatedTransactionId,
            String[] tags) {

        this.accountId = accountId;
        this.employeeId = employeeId;
        this.category = category;
        this.text = text;
        this.createdAt = createdAt;
        this.relatedTransactionId = relatedTransactionId;
        this.tags = tags;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getRelatedTransactionId() {
        return relatedTransactionId;
    }

    public void setRelatedTransactionId(long relatedTransactionId) {
        this.relatedTransactionId = relatedTransactionId;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
