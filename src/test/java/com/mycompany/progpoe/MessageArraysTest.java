package com.mycompany.progpoe;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 3 unit tests, using the test data from the assignment brief.
 */
public class MessageArraysTest {

    // Test Data Message 1 - Sent
    private static final String ID_1 = "1000000001";
    private static final String RECIPIENT_1 = "+27834557896";
    private static final String TEXT_1 = "Did you get the cake?";

    // Test Data Message 2 - Stored
    private static final String ID_2 = "2000000002";
    private static final String RECIPIENT_2 = "+27838884567";
    private static final String TEXT_2 =
            "Where are you? You are late! I have asked you to be on time.";

    // Test Data Message 3 - Disregard
    private static final String ID_3 = "3000000003";
    private static final String RECIPIENT_3 = "+27834484567";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";

    // Test Data Message 4 - Sent (the brief labels this one "Developer")
    private static final String ID_4 = "4000000004";
    private static final String RECIPIENT_4 = "0838884567";
    private static final String TEXT_4 = "It is dinner time !";

    // Test Data Message 5 - Stored
    private static final String ID_5 = "5000000005";
    private static final String RECIPIENT_5 = "+27838884567";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private Message message2;

    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        // Keep the tests away from the real messages.json.
        Message.setJsonFile(new File(tempDir, "messages.json").getPath());
        Message.resetSession();

        // Populate the arrays using the flags from the brief:
        // 1 = Sent, 2 = Disregard, 3 = Stored.
        new Message(ID_1, RECIPIENT_1, TEXT_1).sentMessage(1);
        message2 = new Message(ID_2, RECIPIENT_2, TEXT_2);
        message2.sentMessage(3);
        new Message(ID_3, RECIPIENT_3, TEXT_3).sentMessage(2);
        new Message(ID_4, RECIPIENT_4, TEXT_4).sentMessage(1);
        new Message(ID_5, RECIPIENT_5, TEXT_5).sentMessage(3);
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList(TEXT_1, TEXT_4);
        assertEquals(expected, Arrays.asList(Message.getSentMessageTexts()));
    }

    @Test
    public void testDisregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList(TEXT_3),
                Arrays.asList(Message.getDisregardedMessageTexts()));
    }

    @Test
    public void testStoredMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList(TEXT_2, TEXT_5),
                Arrays.asList(Message.getStoredMessageTexts()));
    }

    @Test
    public void testMessageIDAndHashArraysPopulated() {
        assertEquals(Arrays.asList(ID_1, ID_2, ID_3, ID_4, ID_5),
                Arrays.asList(Message.getMessageIDs()));
        assertEquals(5, Message.getMessageHashes().length);
        assertTrue(Arrays.asList(Message.getMessageHashes())
                .contains(message2.getMessageHash()));
    }

    @Test
    public void testDisplayTheLongestMessage() {
        assertEquals(TEXT_2, Message.displayLongestStoredMessage());
    }

    @Test
    public void testSearchForMessageID() {
        assertEquals("Recipient: " + RECIPIENT_4 + "\nMessage: " + TEXT_4,
                Message.searchByMessageID(ID_4));
    }

    @Test
    public void testSearchForMessageIDNotFound() {
        assertEquals("No message found with ID 9999999999.",
                Message.searchByMessageID("9999999999"));
    }

    @Test
    public void testSearchAllMessagesForARecipient() {
        assertEquals(TEXT_2 + "\n" + TEXT_5,
                Message.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteMessageUsingMessageHash() {
        String hash = message2.getMessageHash();

        assertEquals("Message: \"" + TEXT_2 + "\" successfully deleted.",
                Message.deleteByMessageHash(hash));

        // It is gone from the stored, hash and ID arrays.
        assertEquals(Arrays.asList(TEXT_5),
                Arrays.asList(Message.getStoredMessageTexts()));
        assertFalse(Arrays.asList(Message.getMessageHashes()).contains(hash));
        assertFalse(Arrays.asList(Message.getMessageIDs()).contains(ID_2));
    }

    @Test
    public void testDeleteMessageHashNotFound() {
        assertEquals("No stored message found with hash NOPE.",
                Message.deleteByMessageHash("NOPE"));
        assertEquals(2, Message.returnTotalStored());
    }

    @Test
    public void testDisplayReport() {
        String report = Message.displayStoredMessagesReport();

        assertTrue(report.contains("Message Hash: " + message2.getMessageHash()));
        assertTrue(report.contains("Recipient: " + RECIPIENT_2));
        assertTrue(report.contains("Message: " + TEXT_2));
        assertTrue(report.contains("Message ID: " + ID_5));
        assertTrue(report.contains("Message: " + TEXT_5));
    }

    @Test
    public void testSenderAndRecipientOfStoredMessages() {
        assertEquals("Sender: Test | Recipient: " + RECIPIENT_2 + "\n"
                + "Sender: Test | Recipient: " + RECIPIENT_5,
                Message.displaySenderAndRecipient("Test"));
    }

    @Test
    public void testStoredMessagesReadBackFromJson() {
        // Wipe the session, then rebuild the stored array from the JSON file.
        Message.resetSession();
        assertEquals(2, Message.loadStoredMessages());

        assertEquals(Arrays.asList(TEXT_2, TEXT_5),
                Arrays.asList(Message.getStoredMessageTexts()));
        // The loaded messages keep their original hashes.
        assertTrue(Arrays.asList(Message.getMessageHashes())
                .contains(message2.getMessageHash()));
    }

    @Test
    public void testSendStoredMessageMovesItToSent() {
        assertEquals("Message: \"" + TEXT_2 + "\" successfully sent.",
                Message.sendStoredMessage(message2.getMessageHash()));

        // Now in the Sent array, no longer in the Stored array.
        assertEquals(Arrays.asList(TEXT_1, TEXT_4, TEXT_2),
                Arrays.asList(Message.getSentMessageTexts()));
        assertEquals(Arrays.asList(TEXT_5),
                Arrays.asList(Message.getStoredMessageTexts()));
        // Its hash and ID are still registered.
        assertEquals(5, Message.getMessageHashes().length);
    }

    @Test
    public void testSendStoredMessageByIDAndSavedToJson() {
        assertEquals("Message: \"" + TEXT_5 + "\" successfully sent.",
                Message.sendStoredMessage(ID_5));

        // The JSON file no longer holds it, so a reload only finds message 2.
        Message.resetSession();
        assertEquals(1, Message.loadStoredMessages());
        assertEquals(Arrays.asList(TEXT_2),
                Arrays.asList(Message.getStoredMessageTexts()));
    }

    @Test
    public void testSendStoredMessageNotFound() {
        assertEquals("No stored message found with hash or ID NOPE.",
                Message.sendStoredMessage("NOPE"));
        assertEquals(2, Message.returnTotalStored());
    }

    @Test
    public void testPrintStoredMessagesForHistory() {
        String stored = Message.printStoredMessages();
        assertTrue(stored.contains("Message: " + TEXT_2));
        assertTrue(stored.contains("Message: " + TEXT_5));
        assertFalse(stored.contains(TEXT_1));
    }
}
