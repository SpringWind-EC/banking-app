package com.revature.banking.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.revature.banking.dao.AccountNoteDAO;
import com.revature.banking.dao.DataAccessException;
import com.revature.banking.model.AccountNote;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.*;

public class MongoAccountNoteDAO implements AccountNoteDAO {

    private final MongoCollection<Document> accountNotes;

    public MongoAccountNoteDAO(MongoDatabase database) {
        this.accountNotes = database.getCollection("accountNotes");
    }

    @Override
    public String createAccountNote(AccountNote accountNote) {
        // Implementation for creating an account note in MongoDB
        Document document = new Document("accountId", accountNote.getAccountId())
                .append("employeeId", accountNote.getEmployeeId())
                .append("category", accountNote.getCategory())
                .append("text", accountNote.getText())
                .append("createdAt", accountNote.getCreatedAt())
                .append("flag", accountNote.isFlag());

        if (accountNote.getRelatedTransactionId() != null) {
            document.append("relatedTransactionId", accountNote.getRelatedTransactionId());
        }

        if (accountNote.getTags() != null) {
            document.append("tags", accountNote.getTags());
        }

        try {
            var result = accountNotes.insertOne(document);
            return result.getInsertedId().asObjectId().getValue().toHexString();
        } catch (MongoException e) {
            throw new DataAccessException("Failed to create account note", e);
        }

    }

}
