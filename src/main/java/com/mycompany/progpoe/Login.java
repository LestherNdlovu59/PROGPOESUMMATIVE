package com.mycompany.progpoe;

/**
 * Handles registration and login.
 */
public class Login {

    private String registeredUsername;
    private String registeredPassword;
    private String registeredCellPhoneNumber;

    // Username must contain an underscore and be no more than 5 characters.
    public boolean checkUserName(String username) {
        return username != null
                && username.length() <= 5
                && username.contains("_");
    }

    // Password: at least 8 characters, uppercase letter, number, special character.
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char character : password.toCharArray()) {
            if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isDigit(character)) {
                hasNumber = true;
            } else if (!Character.isLetterOrDigit(character)) {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasNumber && hasSpecial;
    }

    // South African cell number: +27 followed by 9 digits.
    public boolean checkCellPhoneNumber(String cellPhoneNumber) {
        return cellPhoneNumber != null
                && cellPhoneNumber.matches("\\+27[0-9]{9}");
    }

    // Registers the account only when every requirement passes.
    public String registerUser(String username, String password,
                               String cellPhoneNumber) {

        if (!checkUserName(username)) {
            return "Username is not correctly formatted, please ensure that "
                    + "your username contains an underscore and is no more "
                    + "than five characters in length.";
        }

        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that "
                    + "the password contains at least eight characters, a "
                    + "capital letter, a number, and a special character.";
        }

        if (!checkCellPhoneNumber(cellPhoneNumber)) {
            return "Cell phone number incorrectly formatted or does not "
                    + "contain international code.";
        }

        registeredUsername = username;
        registeredPassword = password;
        registeredCellPhoneNumber = cellPhoneNumber;

        return "User registered successfully.";
    }

    // True only when the details match the registered account.
    public boolean loginUser(String username, String password) {
        return registeredUsername != null
                && registeredPassword != null
                && registeredUsername.equals(username)
                && registeredPassword.equals(password);
    }

    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + username
                    + ", it is great to see you again.";
        }

        return "Username or password incorrect, please try again.";
    }
}