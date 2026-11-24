package com.revature.banking.dao.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.revature.banking.dao.DataAccessException;
import com.revature.banking.model.AccountNote;

/**
 * Tests MongoAccountNoteDAO.createAccountNote using in-memory stand-ins for the
 * Mongo database and collection (no running MongoDB required).
 */
public class MongoAccountNoteDAOTest {

    private static final long ACCOUNT_ID = 1001L;
    private static final long EMPLOYEE_ID = 2002L;
    private static final String CATEGORY = "FRAUD_REVIEW";
    private static final String TEXT = "Customer reported a suspicious transfer.";
    private static final Instant CREATED_AT = Instant.parse("2026-11-24T19:00:00Z");
    private static final Long TRANSACTION_ID = 3003L;

    private final List<Document> inserted = new ArrayList<>();
    private String requestedCollectionName;
    private MongoException exceptionToThrow;
    private MongoAccountNoteDAO dao;

    @Before
    public void setUp() {
        inserted.clear();
        requestedCollectionName = null;
        exceptionToThrow = null;
        dao = new MongoAccountNoteDAO(fakeDatabase());
    }

    @Test
    public void usesAccountNotesCollection() {
        assertEquals("accountNotes", requestedCollectionName);
    }

    @Test
    public void createsNoteWithAllFieldsPopulated() {
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                TRANSACTION_ID, Arrays.asList("urgent", "fraud"), true);

        dao.createAccountNote(note);

        assertEquals(1, inserted.size());
        Document doc = inserted.get(0);
        assertEquals(ACCOUNT_ID, doc.get("accountId"));
        assertEquals(EMPLOYEE_ID, doc.get("employeeId"));
        assertEquals(CATEGORY, doc.get("category"));
        assertEquals(TEXT, doc.get("text"));
        assertEquals(CREATED_AT, doc.get("createdAt"));
        assertEquals(TRANSACTION_ID, doc.get("relatedTransactionId"));
        assertEquals(Arrays.asList("urgent", "fraud"), doc.get("tags"));
        assertEquals(Boolean.TRUE, doc.get("flag"));
    }

    @Test
    public void omitsRelatedTransactionIdWhenNull() {
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                null, Arrays.asList("general"), false);

        dao.createAccountNote(note);

        Document doc = inserted.get(0);
        assertFalse(doc.containsKey("relatedTransactionId"));
        assertNull(doc.get("relatedTransactionId"));
        assertEquals(Arrays.asList("general"), doc.get("tags"));
    }

    @Test
    public void storesEmptyTagsListWhenNoTagsGiven() {
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                TRANSACTION_ID, Collections.emptyList(), false);

        dao.createAccountNote(note);

        Document doc = inserted.get(0);
        assertTrue(doc.containsKey("tags"));
        assertEquals(Collections.emptyList(), doc.get("tags"));
    }

    @Test
    public void storesEmptyTagsListWhenTagsSetToNull() {
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                TRANSACTION_ID, Arrays.asList("temp"), false);
        note.setTags(null);

        dao.createAccountNote(note);

        assertEquals(Collections.emptyList(), inserted.get(0).get("tags"));
    }

    @Test
    public void storesFlagFalse() {
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                TRANSACTION_ID, Arrays.asList("routine"), false);

        dao.createAccountNote(note);

        assertEquals(Boolean.FALSE, inserted.get(0).get("flag"));
    }

    @Test
    public void omitsOptionalFieldsWhenBothAreAbsent() {
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                null, Collections.emptyList(), false);

        dao.createAccountNote(note);

        Document doc = inserted.get(0);
        assertFalse(doc.containsKey("relatedTransactionId"));
        assertEquals(ACCOUNT_ID, doc.get("accountId"));
        assertEquals(EMPLOYEE_ID, doc.get("employeeId"));
        assertEquals(TEXT, doc.get("text"));
    }

    @Test
    public void createsMultipleNotesIndependently() {
        dao.createAccountNote(new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                TRANSACTION_ID, Arrays.asList("a"), true));
        dao.createAccountNote(new AccountNote(4004L, 5005L, "GENERAL", "Second note", CREATED_AT,
                null, Arrays.asList("b"), false));

        assertEquals(2, inserted.size());
        assertEquals(ACCOUNT_ID, inserted.get(0).get("accountId"));
        assertEquals(4004L, inserted.get(1).get("accountId"));
        assertEquals("GENERAL", inserted.get(1).get("category"));
    }

    @Test
    public void wrapsMongoExceptionInDataAccessException() {
        MongoException cause = new MongoException("connection lost");
        exceptionToThrow = cause;
        AccountNote note = new AccountNote(ACCOUNT_ID, EMPLOYEE_ID, CATEGORY, TEXT, CREATED_AT,
                TRANSACTION_ID, Arrays.asList("urgent"), true);

        try {
            dao.createAccountNote(note);
            fail("Expected DataAccessException");
        } catch (DataAccessException e) {
            assertEquals("Failed to create account note", e.getMessage());
            assertSame(cause, e.getCause());
        }
        assertTrue(inserted.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void throwsWhenNoteIsNull() {
        dao.createAccountNote(null);
    }

    @SuppressWarnings("unchecked")
    private MongoDatabase fakeDatabase() {
        MongoCollection<Document> collection = (MongoCollection<Document>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { MongoCollection.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("insertOne")) {
                        if (exceptionToThrow != null) {
                            throw exceptionToThrow;
                        }
                        inserted.add((Document) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        return (MongoDatabase) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { MongoDatabase.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getCollection") && args.length == 1) {
                        requestedCollectionName = (String) args[0];
                        return collection;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
