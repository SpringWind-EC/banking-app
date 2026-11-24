package com.revature.banking;

import com.mongodb.client.MongoClients;
import com.revature.banking.dao.mongo.MongoAccountNoteDAO;
import com.revature.banking.service.AccountNoteService;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountNoteMongoIT {
    @Test
    public void serviceInsertsNoteAndReturnsItsMongoId() {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("Set MONGODB_URI before running this test.");
        }

        try (var client = MongoClients.create(uri)) {
            var database = client.getDatabase("banking_app_test");
            var collection = database.getCollection("accountNotes");
            var service = new AccountNoteService(new MongoAccountNoteDAO(database));
            String text = "Integration test " + UUID.randomUUID();

            String id = service.createAccountNote(
                    42L, 7L, "GENERAL", text, Instant.now(), null, List.of("integration-test"));

            try {
                assertNotNull("The service should return the inserted ID", id);
                var saved = collection.find(eq("_id", new ObjectId(id))).first();

                assertNotNull("The returned ID should identify a saved note", saved);
                assertEquals(text, saved.getString("text"));
                assertEquals(Long.valueOf(42L), saved.getLong("accountId"));
                assertEquals(Long.valueOf(7L), saved.getLong("employeeId"));
                assertEquals(List.of("integration-test"), saved.getList("tags", String.class));
            } finally {
                collection.deleteOne(eq("text", text));
            }
        }
    }
}