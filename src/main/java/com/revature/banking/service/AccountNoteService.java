package com.revature.banking.service;

import com.revature.banking.dao.AccountNoteDAO;
import com.revature.banking.model.AccountNote;
import java.time.Instant;
import java.util.*;

import com.revature.banking.dao.AccountNoteDAO;

public class AccountNoteService {

    private static final Set<String> CATEGORIES = Set.of(
            "GENERAL", "CUSTOMER_CONTACT", "WIRE_TRANSFER", "SUSPICIOUS_ACTIVITY");

    private final AccountNoteDAO accountNoteDAO;

    public AccountNoteService(AccountNoteDAO accountNoteDAO) {
        this.accountNoteDAO = accountNoteDAO;
    }

    public String createAccountNote(long accountId, long employeeId, String category,
            String text, Instant createdAt, Long relatedTransactionId,
            List<String> tags) {

        if (accountId <= 0) {
            throw new IllegalArgumentException("Invalid account ID: " + accountId);
        }
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Invalid employee ID: " + employeeId);
        }
        if (category == null || !CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        if ("WIRE_TRANSFER".equals(category) && relatedTransactionId == null) {
            throw new IllegalArgumentException("Related transaction ID cannot be null for wire transfer notes");
        }

        boolean flag = "SUSPICIOUS_ACTIVITY".equals(category);

        List<String> tagList = tags != null ? new ArrayList<>(tags) : new ArrayList<>();

        AccountNote note = new AccountNote(accountId, employeeId, category, text, createdAt, relatedTransactionId,
                tagList, flag);

        return accountNoteDAO.createAccountNote(note);

    }
}
