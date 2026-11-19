package com.mycompany.progpoe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A single QuickChat message, plus the running totals for the session.
 */
public class Message {

    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final String JSON_FILE = "messages.json";

    // Session-wide state.
    private static Message[] sentMessages = new Message[10];
    private static Message[] storedMessages = new Message[10];
    private static Message[] disregardedMessages = new Message[10];
    private static String[] messageHashes = new String[10];
    private static String[] messageIDs = new String[10];   // messageIDs[i] belongs to messageHashes[i]
    private static int totalMessagesSent = 0;   // also the number of items in sentMessages
    private static int totalMessagesStored = 0; // number of items in storedMessages
    private static int totalMessagesDisregarded = 0;
    private static int totalHashes = 0;         // number of items in messageHashes / messageIDs
    private static String jsonFile = JSON_FILE;

    private final String messageID;
    private final int numMessagesSent;   // starts at 0, goes up as messages are sent
    private final String recipient;
    private final String message;
    private final String messageHash;

    // Normal constructor: the ID is randomly generated.
    public Message(String recipient, String message) {
        this(generateMessageID(), recipient, message);
    }

    // Used when a specific ID is needed (e.g. unit tests).
    public Message(String messageID, String recipient, String message) {
        this.messageID = messageID;
        this.numMessagesSent = totalMessagesSent;
        this.recipient = recipient;
        this.message = message;
        this.messageHash = createMessageHash();
    }

    // Used when reading a stored message back from the JSON file: the hash
    // that was saved is kept as it is.
    private Message(String messageID, String messageHash,
                    String recipient, String message) {
        this.messageID = messageID;
        this.messageHash = messageHash;
        this.recipient = recipient;
        this.message = message;
        this.numMessagesSent = numberFromHash(messageHash);
    }

    private static int numberFromHash(String hash) {
        String[] parts = hash == null ? new String[0] : hash.split(":");
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return 0;
    }

    // Random ten-digit number.
    private static String generateMessageID() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(ThreadLocalRandom.current().nextInt(10));
        }
        return id.toString();
    }

    // The ID must not be more than ten characters.
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Must start with an international code and be no longer than the
    // international code (+ and up to 3 digits) plus ten digits.
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("\\+[0-9]{1,3}[0-9]{1,10}")
                && recipient.length() <= 13) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not "
                + "contain an international code. Please correct the number "
                + "and try again.";
    }

    // Message must not exceed 250 characters.
    public String checkMessageLength() {
        int length = message == null ? 0 : message.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by "
                + (length - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    // Confirmation shown once the ID has been created.
    public String getMessageIDStatement() {
        return "Message ID generated: " + messageID;
    }

    // First two digits of the ID : message number : FIRSTLAST word, all caps.
    // Example: 00:0:HITHANKS
    public String createMessageHash() {
        String[] words = message.trim().split("\\s+");
        String first = words[0].replaceAll("[^A-Za-z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        String prefix = messageID.length() >= 2
                ? messageID.substring(0, 2) : messageID;

        return (prefix + ":" + numMessagesSent + ":" + first + last)
                .toUpperCase();
    }

    // option 1 = send, 2 = disregard, 3 = store for later.
    public String sentMessage(int option) {
        switch (option) {
            case 1:
                if (totalMessagesSent == sentMessages.length) {
                    sentMessages = Arrays.copyOf(sentMessages, sentMessages.length * 2);
                }
                sentMessages[totalMessagesSent] = this;
                totalMessagesSent++;
                registerHashAndID();
                return "Message successfully sent.";
            case 2:
                if (totalMessagesDisregarded == disregardedMessages.length) {
                    disregardedMessages = Arrays.copyOf(disregardedMessages,
                            disregardedMessages.length * 2);
                }
                disregardedMessages[totalMessagesDisregarded] = this;
                totalMessagesDisregarded++;
                registerHashAndID();
                return "Press 0 to delete the message.";
            case 3:
                if (totalMessagesStored == storedMessages.length) {
                    storedMessages = Arrays.copyOf(storedMessages, storedMessages.length * 2);
                }
                storedMessages[totalMessagesStored] = this;
                totalMessagesStored++;
                registerHashAndID();
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option, please choose 1, 2 or 3.";
        }
    }

    // Full details in the required order: ID, Hash, Recipient, Message.
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + message;
    }

    // All messages sent while the program is running.
    public static String printMessages() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < totalMessagesSent; i++) {
            output.append(sentMessages[i].getDetails()).append("\n\n");
        }
        return output.toString().trim();
    }

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    // Writes every stored message to messages.json.
    public void storeMessage() {
        writeStoredMessagesToJson();
    }

    private static void writeStoredMessagesToJson() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < totalMessagesStored; i++) {
            Message m = storedMessages[i];
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escape(m.messageID)).append("\",\n")
                .append("    \"messageHash\": \"").append(escape(m.messageHash)).append("\",\n")
                .append("    \"recipient\": \"").append(escape(m.recipient)).append("\",\n")
                .append("    \"message\": \"").append(escape(m.message)).append("\"\n")
                .append("  }")
                .append(i < totalMessagesStored - 1 ? ",\n" : "\n");
        }
        json.append("]\n");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not write " + jsonFile + ": " + e.getMessage());
        }
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    // Clears session state (used by unit tests).
    public static void resetSession() {
        sentMessages = new Message[10];
        storedMessages = new Message[10];
        disregardedMessages = new Message[10];
        messageHashes = new String[10];
        messageIDs = new String[10];
        totalMessagesSent = 0;
        totalMessagesStored = 0;
        totalMessagesDisregarded = 0;
        totalHashes = 0;
    }

    // Lets the unit tests write to a temporary file instead of messages.json.
    public static void setJsonFile(String path) {
        jsonFile = path;
    }

    // =====================================================================
    //  Part 3: arrays, reading the JSON file and the Stored Messages menu
    // =====================================================================

    // Adds this message's hash and ID to the two parallel arrays.
    private void registerHashAndID() {
        if (totalHashes == messageHashes.length) {
            messageHashes = Arrays.copyOf(messageHashes, messageHashes.length * 2);
            messageIDs = Arrays.copyOf(messageIDs, messageIDs.length * 2);
        }
        messageHashes[totalHashes] = messageHash;
        messageIDs[totalHashes] = messageID;
        totalHashes++;
    }

    private static boolean idIsRegistered(String id) {
        for (int i = 0; i < totalHashes; i++) {
            if (messageIDs[i].equals(id)) {
                return true;
            }
        }
        return false;
    }

    // Fills the Stored Messages array from the JSON file. Returns how many
    // messages were read.
    public static int loadStoredMessages() {
        storedMessages = new Message[10];
        totalMessagesStored = 0;

        File file = new File(jsonFile);
        if (!file.exists()) {
            return 0;
        }

        String content;
        try {
            content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
        } catch (IOException e) {
            System.out.println("Could not read " + jsonFile + ": " + e.getMessage());
            return 0;
        }

        for (Map<String, String> fields : parseJsonObjects(content)) {
            String id = fields.get("messageID");
            String hash = fields.get("messageHash");
            String recipient = fields.get("recipient");
            String text = fields.get("message");
            if (id == null || hash == null || recipient == null || text == null) {
                continue; // skip anything that is not a full message
            }

            Message m = new Message(id, hash, recipient, text);
            if (totalMessagesStored == storedMessages.length) {
                storedMessages = Arrays.copyOf(storedMessages, storedMessages.length * 2);
            }
            storedMessages[totalMessagesStored] = m;
            totalMessagesStored++;

            if (!idIsRegistered(id)) {
                m.registerHashAndID();
            }
        }
        return totalMessagesStored;
    }

    // Reads the flat format written by storeMessage(): a list of objects whose
    // values are all strings. Strings appear as key, value, key, value, ...
    private static List<Map<String, String>> parseJsonObjects(String json) {
        List<Map<String, String>> objects = new ArrayList<>();
        Map<String, String> current = null;
        String key = null;
        int i = 0;

        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '{') {
                current = new HashMap<>();
                key = null;
                i++;
            } else if (c == '}') {
                if (current != null) {
                    objects.add(current);
                }
                current = null;
                i++;
            } else if (c == '"') {
                StringBuilder text = new StringBuilder();
                i++;
                while (i < json.length() && json.charAt(i) != '"') {
                    char ch = json.charAt(i);
                    if (ch == '\\' && i + 1 < json.length()) {
                        i++;
                        char esc = json.charAt(i);
                        switch (esc) {
                            case 'n': text.append('\n'); break;
                            case 'r': text.append('\r'); break;
                            case 't': text.append('\t'); break;
                            case 'u':
                                if (i + 4 < json.length()) {
                                    text.append((char) Integer.parseInt(
                                            json.substring(i + 1, i + 5), 16));
                                    i += 4;
                                }
                                break;
                            default: text.append(esc);
                        }
                    } else {
                        text.append(ch);
                    }
                    i++;
                }
                i++; // closing quote
                if (current != null) {
                    if (key == null) {
                        key = text.toString();
                    } else {
                        current.put(key, text.toString());
                        key = null;
                    }
                }
            } else {
                i++;
            }
        }
        return objects;
    }

    // ---- Array getters (copies trimmed to the number of items in use) ----

    private static String[] textsOf(Message[] source, int count) {
        String[] texts = new String[count];
        for (int i = 0; i < count; i++) {
            texts[i] = source[i].message;
        }
        return texts;
    }

    public static String[] getSentMessageTexts() {
        return textsOf(sentMessages, totalMessagesSent);
    }

    public static String[] getDisregardedMessageTexts() {
        return textsOf(disregardedMessages, totalMessagesDisregarded);
    }

    public static String[] getStoredMessageTexts() {
        return textsOf(storedMessages, totalMessagesStored);
    }

    public static String[] getMessageHashes() {
        return Arrays.copyOf(messageHashes, totalHashes);
    }

    public static String[] getMessageIDs() {
        return Arrays.copyOf(messageIDs, totalHashes);
    }

    public static int returnTotalStored() {
        return totalMessagesStored;
    }

    // ---- 2a. Sender and recipient of every stored message ----

    public static String displaySenderAndRecipient(String sender) {
        if (totalMessagesStored == 0) {
            return "There are no stored messages.";
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < totalMessagesStored; i++) {
            output.append("Sender: ").append(sender)
                  .append(" | Recipient: ").append(storedMessages[i].recipient)
                  .append("\n");
        }
        return output.toString().trim();
    }

    // ---- 2b. Longest stored message ----

    public static String displayLongestStoredMessage() {
        if (totalMessagesStored == 0) {
            return "There are no stored messages.";
        }
        Message longest = storedMessages[0];
        for (int i = 1; i < totalMessagesStored; i++) {
            if (storedMessages[i].message.length() > longest.message.length()) {
                longest = storedMessages[i];
            }
        }
        return longest.message;
    }

    // ---- 2c. Search by message ID ----

    public static String searchByMessageID(String id) {
        if (id != null) {
            id = id.trim();
        }
        for (int i = 0; i < totalHashes; i++) {
            if (messageIDs[i].equals(id)) {
                Message m = findByID(id);
                if (m != null) {
                    return "Recipient: " + m.recipient + "\nMessage: " + m.message;
                }
            }
        }
        return "No message found with ID " + id + ".";
    }

    private static Message findByID(String id) {
        for (int i = 0; i < totalMessagesSent; i++) {
            if (sentMessages[i].messageID.equals(id)) return sentMessages[i];
        }
        for (int i = 0; i < totalMessagesStored; i++) {
            if (storedMessages[i].messageID.equals(id)) return storedMessages[i];
        }
        for (int i = 0; i < totalMessagesDisregarded; i++) {
            if (disregardedMessages[i].messageID.equals(id)) return disregardedMessages[i];
        }
        return null;
    }

    // ---- 2d. Every message sent or stored for one recipient ----

    public static String searchByRecipient(String recipient) {
        if (recipient != null) {
            recipient = recipient.trim();
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < totalMessagesSent; i++) {
            if (sentMessages[i].recipient.equals(recipient)) {
                output.append(sentMessages[i].message).append("\n");
            }
        }
        for (int i = 0; i < totalMessagesStored; i++) {
            if (storedMessages[i].recipient.equals(recipient)) {
                output.append(storedMessages[i].message).append("\n");
            }
        }
        if (output.length() == 0) {
            return "No messages found for " + recipient + ".";
        }
        return output.toString().trim();
    }

    // ---- 2e. Delete a stored message using its hash ----

    public static String deleteByMessageHash(String hash) {
        if (hash != null) {
            hash = hash.trim();
        }
        for (int i = 0; i < totalMessagesStored; i++) {
            if (storedMessages[i].messageHash.equals(hash)) {
                Message removed = storedMessages[i];

                for (int j = i; j < totalMessagesStored - 1; j++) {
                    storedMessages[j] = storedMessages[j + 1];
                }
                storedMessages[totalMessagesStored - 1] = null;
                totalMessagesStored--;

                removeFromHashAndIDArrays(removed);
                writeStoredMessagesToJson();
                return "Message: \"" + removed.message + "\" successfully deleted.";
            }
        }
        return "No stored message found with hash " + hash + ".";
    }

    private static void removeFromHashAndIDArrays(Message m) {
        for (int i = 0; i < totalHashes; i++) {
            if (messageHashes[i].equals(m.messageHash) && messageIDs[i].equals(m.messageID)) {
                for (int j = i; j < totalHashes - 1; j++) {
                    messageHashes[j] = messageHashes[j + 1];
                    messageIDs[j] = messageIDs[j + 1];
                }
                messageHashes[totalHashes - 1] = null;
                messageIDs[totalHashes - 1] = null;
                totalHashes--;
                return;
            }
        }
    }

    // ---- Stored messages waiting to be sent (used by the history view) ----

    public static String printStoredMessages() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < totalMessagesStored; i++) {
            output.append(storedMessages[i].getDetails()).append("\n\n");
        }
        return output.toString().trim();
    }

    // ---- Send a stored message (found by its hash or its ID) ----
    // Moves it out of the Stored array and into the Sent array.

    public static String sendStoredMessage(String hashOrID) {
        if (hashOrID != null) {
            hashOrID = hashOrID.trim();
        }
        for (int i = 0; i < totalMessagesStored; i++) {
            Message m = storedMessages[i];
            if (m.messageHash.equals(hashOrID) || m.messageID.equals(hashOrID)) {
                for (int j = i; j < totalMessagesStored - 1; j++) {
                    storedMessages[j] = storedMessages[j + 1];
                }
                storedMessages[totalMessagesStored - 1] = null;
                totalMessagesStored--;

                if (totalMessagesSent == sentMessages.length) {
                    sentMessages = Arrays.copyOf(sentMessages, sentMessages.length * 2);
                }
                sentMessages[totalMessagesSent] = m;
                totalMessagesSent++;

                // Its hash and ID are already in the two arrays, so they stay.
                writeStoredMessagesToJson();
                return "Message: \"" + m.message + "\" successfully sent.";
            }
        }
        return "No stored message found with hash or ID " + hashOrID + ".";
    }

    // ---- 2f. Report with the full details of every stored message ----

    public static String displayStoredMessagesReport() {
        if (totalMessagesStored == 0) {
            return "There are no stored messages.";
        }
        StringBuilder report = new StringBuilder("=== Stored Messages Report ===\n\n");
        for (int i = 0; i < totalMessagesStored; i++) {
            report.append(storedMessages[i].getDetails()).append("\n\n");
        }
        return report.toString().trim();
    }

    public String getMessageID() { return messageID; }
    public int getNumMessagesSent() { return numMessagesSent; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public String getMessageHash() { return messageHash; }
}