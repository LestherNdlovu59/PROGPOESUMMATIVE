package com.mycompany.progpoe;

import java.util.Scanner;

public class PROGPOE {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Login login = new Login();

        // ---- Registration ----
        System.out.println("=== Create an account ===");
        String registrationMessage;
        do {
            System.out.print("Enter username: ");
            String username = input.nextLine();

            System.out.print("Enter password: ");
            String password = input.nextLine();

            System.out.print("Enter South African cell number (+27XXXXXXXXX): ");
            String cellPhoneNumber = input.nextLine();

            registrationMessage =
                    login.registerUser(username, password, cellPhoneNumber);
            System.out.println(registrationMessage);
        } while (!registrationMessage.equals("User registered successfully."));

        // ---- Login ----
        System.out.println("\n=== Log in ===");
        System.out.print("Enter username: ");
        String loginUsername = input.nextLine();
        System.out.print("Enter password: ");
        String loginPassword = input.nextLine();

        System.out.println(login.returnLoginStatus(loginUsername, loginPassword));

        // Only logged-in users may send messages.
        if (!login.loginUser(loginUsername, loginPassword)) {
            input.close();
            return;
        }

        // ---- QuickChat ----
        System.out.println("\nWelcome to QuickChat.");

        // Fill the Stored Messages array from messages.json.
        int loaded = Message.loadStoredMessages();
        System.out.println(loaded + " stored message(s) loaded from "
                + Message.JSON_FILE + ".");

        int maxMessages = readPositiveInt(input,
                "How many messages do you wish to enter? ");

        boolean running = true;
        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    sendMessages(input, maxMessages);
                    break;
                case "2":
                    System.out.println("\n--- Recently Sent Messages ---");
                    if (Message.returnTotalMessages() == 0) {
                        System.out.println("No messages have been sent yet.");
                    } else {
                        System.out.println(Message.printMessages());
                    }
                    System.out.println("\n--- Stored Messages (waiting to be sent) ---");
                    if (Message.returnTotalStored() == 0) {
                        System.out.println("There are no stored messages.");
                    } else {
                        System.out.println(Message.printStoredMessages());
                        System.out.println("\nUse option 4 to send or delete a stored message.");
                    }
                    break;
                case "3":
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                case "4":
                    storedMessagesMenu(input, loginUsername);
                    break;
                default:
                    System.out.println("Invalid option, please enter 1, 2, 3 or 4.");
            }
        }

        input.close();
    }

    // Main menu option 4: works with the Stored Messages array.
    private static void storedMessagesMenu(Scanner input, String sender) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display report of all stored messages");
            System.out.println("7) Send a stored message");
            System.out.println("0) Back to main menu");
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println(Message.displaySenderAndRecipient(sender));
                    break;
                case "2":
                    System.out.println(Message.displayLongestStoredMessage());
                    break;
                case "3":
                    System.out.print("Enter the message ID: ");
                    System.out.println(Message.searchByMessageID(input.nextLine()));
                    break;
                case "4":
                    System.out.print("Enter the recipient cell number: ");
                    System.out.println(Message.searchByRecipient(input.nextLine()));
                    break;
                case "5":
                    System.out.print("Enter the message hash: ");
                    System.out.println(Message.deleteByMessageHash(input.nextLine()));
                    break;
                case "6":
                    System.out.println(Message.displayStoredMessagesReport());
                    break;
                case "7":
                    System.out.println(Message.displayStoredMessagesReport());
                    if (Message.returnTotalStored() > 0) {
                        System.out.print("Enter the hash or ID of the message to send: ");
                        System.out.println(Message.sendStoredMessage(input.nextLine()));
                    }
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 0 to 7.");
            }
        }
    }

    private static void sendMessages(Scanner input, int maxMessages) {
        int remaining = maxMessages - Message.returnTotalMessages();

        if (remaining <= 0) {
            System.out.println("You have already entered all "
                    + maxMessages + " messages.");
            return;
        }

        while (remaining > 0) {
            // Recipient
            String recipient;
            while (true) {
                System.out.print("Enter recipient cell number (with international code): ");
                recipient = input.nextLine().trim();
                Message check = new Message(recipient, "x");
                String result = check.checkRecipientCell();
                System.out.println(result);
                if (result.equals("Cell phone number successfully captured.")) {
                    break;
                }
            }

            // Message text
            String text;
            while (true) {
                System.out.print("Enter your message: ");
                text = input.nextLine();
                if (text.trim().isEmpty()) {
                    System.out.println("Message cannot be empty.");
                    continue;
                }
                Message check = new Message(recipient, text);
                String result = check.checkMessageLength();
                System.out.println(result);
                if (result.equals("Message ready to send.")) {
                    break;
                }
            }

            Message message = new Message(recipient, text);
            System.out.println(message.getMessageIDStatement());

            // Send / disregard / store
            System.out.println("\n1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int option = readPositiveInt(input, "Choose an option: ");
            String outcome = message.sentMessage(option);
            System.out.println(outcome);

            if (option == 1) {
                System.out.println("\n" + message.getDetails());
                remaining--;
            } else if (option == 2) {
                System.out.println("Message deleted.");
            }
            // Stored messages do not count as sent; the slot is reused.
        }

        System.out.println("\nTotal messages sent: "
                + Message.returnTotalMessages());
    }

    private static int readPositiveInt(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }
}