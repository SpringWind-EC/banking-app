package com.revature.banking.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.revature.banking.dao.AccountNoteDAO;
import com.revature.banking.dao.DataAccessException;
import com.revature.banking.model.AccountNote;

/**
 * Tests AccountNoteService.createAccountNote against an in-memory DAO, so no
 * database is needed.
 */
public class AccountNoteServiceTest {

    private static final long ACCOUNT_ID = 1001L;
    private static final long EMPLOYEE_ID = 2002L;
    private static final String TEXT = "Customer called about a pending wire.";
    private static final Instant CREATED_AT = Instant.parse("2026-11-24T19:00:00Z");
    private static final Long TRANSACTION_ID = 3003L;

    private final List<AccountNote> saved = new ArrayList<>();
    private RuntimeException daoFailure;
    private AccountNoteService service;

    @Before
    public void setUp() {
        saved.clear();
        daoFailure = null;
        AccountNoteDAO fakeDao = note -> {
            if (daoFailure != null) {
                throw daoFailure;
            }
            saved.add(note);
            return "note-" + saved.size();
        };
        service = new AccountNoteService(fakeDao);
    }

    private String create(String category, Long transactionId, List<String> tags) {
        return service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, category, TEXT, CREATED_AT,
                transactionId, tags);
    }

    private void assertRejected(Runnable call, String expectedMessage) {
        try {
            call.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
        assertTrue("DAO must not be called for invalid input", saved.isEmpty());
    }

    // ---- successful creation ----

    @Test
    public void savesNoteWithAllFieldsAndReturnsDaoId() {
        String id = create("WIRE_TRANSFER", TRANSACTION_ID, Arrays.asList("wire", "urgent"));

        assertEquals("note-1", id);
        assertEquals(1, saved.size());
        AccountNote note = saved.get(0);
        assertEquals(ACCOUNT_ID, note.getAccountId());
        assertEquals(EMPLOYEE_ID, note.getEmployeeId());
        assertEquals("WIRE_TRANSFER", note.getCategory());
        assertEquals(TEXT, note.getText());
        assertEquals(CREATED_AT, note.getCreatedAt());
        assertEquals(TRANSACTION_ID, note.getRelatedTransactionId());
        assertEquals(Arrays.asList("wire", "urgent"), note.getTags());
    }

    @Test
    public void acceptsEveryAllowedCategory() {
        for (String category : Arrays.asList("GENERAL", "CUSTOMER_CONTACT", "WIRE_TRANSFER",
                "SUSPICIOUS_ACTIVITY")) {
            create(category, TRANSACTION_ID, null);
        }

        assertEquals(4, saved.size());
    }

    @Test
    public void createsMultipleNotesIndependently() {
        String first = create("GENERAL", null, null);
        String second = create("CUSTOMER_CONTACT", null, null);

        assertEquals("note-1", first);
        assertEquals("note-2", second);
        assertEquals("GENERAL", saved.get(0).getCategory());
        assertEquals("CUSTOMER_CONTACT", saved.get(1).getCategory());
    }

    // ---- flag ----

    @Test
    public void flagsSuspiciousActivityNotes() {
        create("SUSPICIOUS_ACTIVITY", null, null);

        assertTrue(saved.get(0).isFlag());
    }

    @Test
    public void doesNotFlagOtherCategories() {
        create("GENERAL", null, null);
        create("CUSTOMER_CONTACT", null, null);
        create("WIRE_TRANSFER", TRANSACTION_ID, null);

        for (AccountNote note : saved) {
            assertFalse(note.getCategory(), note.isFlag());
        }
    }

    // ---- account and employee ids ----

    @Test
    public void rejectsZeroAccountId() {
        assertRejected(() -> service.createAccountNote(0, EMPLOYEE_ID, "GENERAL", TEXT, CREATED_AT, null, null),
                "Invalid account ID: 0");
    }

    @Test
    public void rejectsNegativeAccountId() {
        assertRejected(() -> service.createAccountNote(-5, EMPLOYEE_ID, "GENERAL", TEXT, CREATED_AT, null, null),
                "Invalid account ID: -5");
    }

    @Test
    public void rejectsZeroEmployeeId() {
        assertRejected(() -> service.createAccountNote(ACCOUNT_ID, 0, "GENERAL", TEXT, CREATED_AT, null, null),
                "Invalid employee ID: 0");
    }

    @Test
    public void rejectsNegativeEmployeeId() {
        assertRejected(() -> service.createAccountNote(ACCOUNT_ID, -1, "GENERAL", TEXT, CREATED_AT, null, null),
                "Invalid employee ID: -1");
    }

    @Test
    public void acceptsSmallestAndLargestValidIds() {
        service.createAccountNote(1, 1, "GENERAL", TEXT, CREATED_AT, null, null);
        service.createAccountNote(Long.MAX_VALUE, Long.MAX_VALUE, "GENERAL", TEXT, CREATED_AT, null, null);

        assertEquals(2, saved.size());
        assertEquals(Long.MAX_VALUE, saved.get(1).getAccountId());
    }

    // ---- category ----

    @Test
    public void rejectsNullCategory() {
        assertRejected(() -> create(null, null, null), "Invalid category: null");
    }

    @Test
    public void rejectsBlankCategory() {
        assertRejected(() -> create("  ", null, null), "Invalid category:   ");
    }

    @Test
    public void rejectsUnknownCategory() {
        assertRejected(() -> create("FRAUD_REVIEW", null, null), "Invalid category: FRAUD_REVIEW");
    }

    @Test
    public void rejectsLowercaseCategory() {
        assertRejected(() -> create("general", null, null), "Invalid category: general");
    }

    @Test
    public void rejectsCategoryWithSurroundingWhitespace() {
        assertRejected(() -> create(" GENERAL ", null, null), "Invalid category:  GENERAL ");
    }

    // ---- text ----

    @Test
    public void rejectsNullText() {
        assertRejected(() -> service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", null, CREATED_AT,
                null, null), "Text cannot be null or empty");
    }

    @Test
    public void rejectsEmptyText() {
        assertRejected(() -> service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", "", CREATED_AT,
                null, null), "Text cannot be null or empty");
    }

    @Test
    public void rejectsWhitespaceOnlyText() {
        assertRejected(() -> service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", " \t\n ", CREATED_AT,
                null, null), "Text cannot be null or empty");
    }

    @Test
    public void storesTextUnchangedIncludingWhitespaceAndSpecialCharacters() {
        String text = "  Caf\u00e9 \u2615 \"quoted\" $set {'a': 1}\nline two  ";

        service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", text, CREATED_AT, null, null);

        assertEquals(text, saved.get(0).getText());
    }

    @Test
    public void acceptsVeryLongText() {
        String text = "x".repeat(100_000);

        service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", text, CREATED_AT, null, null);

        assertEquals(100_000, saved.get(0).getText().length());
    }

    // ---- createdAt ----

    @Test
    public void rejectsNullCreatedAt() {
        assertRejected(() -> service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", TEXT, null,
                null, null), "CreatedAt cannot be null");
    }

    @Test
    public void keepsCallerSuppliedCreatedAt() {
        Instant past = Instant.parse("2000-01-01T00:00:00Z");

        service.createAccountNote(ACCOUNT_ID, EMPLOYEE_ID, "GENERAL", TEXT, past, null, null);

        assertEquals(past, saved.get(0).getCreatedAt());
    }

    // ---- related transaction id ----

    @Test
    public void rejectsWireTransferNoteWithoutTransactionId() {
        assertRejected(() -> create("WIRE_TRANSFER", null, null),
                "Related transaction ID cannot be null for wire transfer notes");
    }

    @Test
    public void acceptsWireTransferNoteWithTransactionId() {
        create("WIRE_TRANSFER", TRANSACTION_ID, null);

        assertEquals(TRANSACTION_ID, saved.get(0).getRelatedTransactionId());
    }

    @Test
    public void transactionIdIsOptionalForNonWireNotes() {
        create("GENERAL", null, null);

        assertNull(saved.get(0).getRelatedTransactionId());
    }

    @Test
    public void suspiciousNoteCanBeLinkedToTransaction() {
        create("SUSPICIOUS_ACTIVITY", TRANSACTION_ID, null);

        assertEquals(TRANSACTION_ID, saved.get(0).getRelatedTransactionId());
        assertTrue(saved.get(0).isFlag());
    }

    // ---- tags ----

    @Test
    public void nullTagsBecomeEmptyList() {
        create("GENERAL", null, null);

        assertEquals(Collections.emptyList(), saved.get(0).getTags());
    }

    @Test
    public void emptyTagsStayEmpty() {
        create("GENERAL", null, Collections.emptyList());

        assertTrue(saved.get(0).getTags().isEmpty());
    }

    @Test
    public void copiesTagsSoLaterCallerChangesDoNotAffectSavedNote() {
        List<String> tags = new ArrayList<>(Arrays.asList("a", "b"));

        create("GENERAL", null, tags);
        tags.add("c");
        tags.remove("a");

        assertEquals(Arrays.asList("a", "b"), saved.get(0).getTags());
    }

    @Test
    public void acceptsImmutableTagList() {
        create("GENERAL", null, List.of("x", "y"));

        assertEquals(Arrays.asList("x", "y"), saved.get(0).getTags());
    }

    @Test
    public void savedNoteHasItsOwnTagListInstance() {
        List<String> tags = new ArrayList<>(Arrays.asList("a"));

        create("GENERAL", null, tags);

        assertNotSame(tags, saved.get(0).getTags());
    }

    // ---- failures from the data layer ----

    @Test
    public void propagatesDataAccessException() {
        DataAccessException failure = new DataAccessException("Failed to create account note",
                new RuntimeException("connection lost"));
        daoFailure = failure;

        try {
            create("GENERAL", null, null);
            fail("Expected DataAccessException");
        } catch (DataAccessException e) {
            assertSame(failure, e);
        }
        assertTrue(saved.isEmpty());
    }

    @Test
    public void validationRunsBeforeDaoSoInvalidInputNeverReachesFailingDao() {
        daoFailure = new DataAccessException("should not be reached", null);

        try {
            create("NOT_A_CATEGORY", null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid category: NOT_A_CATEGORY", e.getMessage());
        }
    }
}
