package com.revature.banking.model;

import java.util.*;
import java.time.*;

public class AccountNote {

    long accountId;
    long employeeId;
    String category;
    String text;
    Instant createdAt;
    Long relatedTransactionId;
    List<String> tags;
    boolean flag;

    public AccountNote(long accountId, long employeeId, String category,
            String text, Instant createdAt, Long relatedTransactionId,
            List<String> tags, boolean flag) {

        this.accountId = accountId;
        this.employeeId = employeeId;
        this.category = category;
        this.text = text;
        this.createdAt = createdAt;
        this.relatedTransactionId = relatedTransactionId;
        this.tags = new ArrayList<>(tags);
        this.flag = flag;
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

    public Long getRelatedTransactionId() {
        return relatedTransactionId;
    }

    public void setRelatedTransactionId(Long relatedTransactionId) {
        this.relatedTransactionId = relatedTransactionId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = (tags == null ? new ArrayList<>() : new ArrayList<>(tags));
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
