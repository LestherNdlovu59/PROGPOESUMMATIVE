package com.mycompany.progpoe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    // Test Data - Message 1
    private static final String ID_1 = "0012345678";
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";

    // Test Data - Message 2
    private static final String ID_2 = "1112345678";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        Message.setJsonFile(new File(tempDir, "messages.json").getPath());
        Message.resetSession();
    }

    // ---- Message length ----

    @Test
    public void testMessageLengthSuccess() {
        Message m = new Message(ID_1, RECIPIENT_1, TEXT_1);
        assertEquals("Message ready to send.", m.checkMessageLength());
    }

    @Test
    public void testMessageLengthFailure() {
        Message m = new Message(ID_1, RECIPIENT_1, "a".repeat(260));
        assertEquals("Message exceeds 250 characters by 10; "
                + "please reduce the size.", m.checkMessageLength());
    }

    // ---- Recipient number ----

    @Test
    public void testRecipientNumberSuccess() {
        Message m = new Message(ID_1, RECIPIENT_1, TEXT_1);
        assertEquals("Cell phone number successfully captured.",
                m.checkRecipientCell());
    }

    @Test
    public void testRecipientNumberFailure() {
        Message m = new Message(ID_2, RECIPIENT_2, TEXT_2);
        assertEquals("Cell phone number is incorrectly formatted or does not "
                + "contain an international code. Please correct the number "
                + "and try again.", m.checkRecipientCell());
    }

    // ---- Message hash ----

    @Test
    public void testMessageHashTestCase1() {
        Message m = new Message(ID_1, RECIPIENT_1, TEXT_1);
        assertEquals("00:0:HITONIGHT", m.createMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        String[] ids = {ID_1, ID_2};
        String[] recipients = {RECIPIENT_1, RECIPIENT_2};
        String[] texts = {TEXT_1, TEXT_2};
        String[] expected = {"00:0:HITONIGHT", "11:1:HIPAYMENT"};

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ids[i], recipients[i], texts[i]);
            assertEquals(expected[i], m.getMessageHash());
            m.sentMessage(1); // sending moves the message number on
        }
    }

    // ---- Message ID ----

    @Test
    public void testMessageIDCreated() {
        Message m = new Message(RECIPIENT_1, TEXT_1);
        assertTrue(m.checkMessageID());
        assertEquals("Message ID generated: " + m.getMessageID(),
                m.getMessageIDStatement());
        assertEquals(10, m.getMessageID().length());
    }

    // ---- Send / Disregard / Store ----

    @Test
    public void testMessageSentOption1Send() {
        Message m = new Message(ID_1, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully sent.", m.sentMessage(1));
    }

    @Test
    public void testMessageSentOption2Disregard() {
        Message m = new Message(ID_2, RECIPIENT_2, TEXT_2);
        assertEquals("Press 0 to delete the message.", m.sentMessage(2));
    }

    @Test
    public void testMessageSentOption3Store() {
        Message m = new Message(ID_1, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully stored.", m.sentMessage(3));
    }

    // ---- Totals / printing (Test Data 1 sent, Test Data 2 discarded) ----

    @Test
    public void testReturnTotalMessages() {
        new Message(ID_1, RECIPIENT_1, TEXT_1).sentMessage(1);
        new Message(ID_2, RECIPIENT_2, TEXT_2).sentMessage(2);
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages() {
        new Message(ID_1, RECIPIENT_1, TEXT_1).sentMessage(1);
        String printed = Message.printMessages();
        assertTrue(printed.contains("Message ID: " + ID_1));
        assertTrue(printed.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(printed.contains("Recipient: " + RECIPIENT_1));
        assertTrue(printed.contains("Message: " + TEXT_1));
    }
}