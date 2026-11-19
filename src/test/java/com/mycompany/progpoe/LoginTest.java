/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.progpoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Student
 */
public class LoginTest {
    
    public LoginTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    private final Login login = new Login();

    @Test
    public void testUsernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!"));
    }

    @Test
    public void testPasswordMeetsComplexityRequirements() {
        assertTrue(login.checkPasswordComplexity("Ch&seC@ke99!"));
    }

    @Test
    public void testPasswordDoesNotMeetComplexityRequirements() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    @Test
    public void testCellPhoneNumberCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27821234567"));
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("0821234567"));
    }

    @Test
    public void testRegistrationSucceedsWhenAllDetailsAreValid() {
        assertEquals("User registered successfully.",
                login.registerUser("kyl_1", "Ch&seC@ke99!", "+27821234567"));
    }

    @Test
    public void testRegistrationFailsForInvalidUsername() {
        assertEquals(
                "Username is not correctly formatted, please ensure that "
                + "your username contains an underscore and is no more "
                + "than five characters in length.",
                login.registerUser("kyle!!!!!!", "Ch&seC@ke99!", "+27821234567"));
    }

    @Test
    public void testRegistrationFailsForInvalidPassword() {
        assertEquals(
                "Password is not correctly formatted; please ensure that "
                + "the password contains at least eight characters, a "
                + "capital letter, a number, and a special character.",
                login.registerUser("kyl_1", "password", "+27821234567"));
    }

    @Test
    public void testRegistrationFailsForInvalidCellPhoneNumber() {
        assertEquals(
                "Cell phone number incorrectly formatted or does not "
                + "contain international code.",
                login.registerUser("kyl_1", "Ch&seC@ke99!", "0821234567"));
    }

    @Test
    public void testLoginSucceedsWithRegisteredDetails() {
        login.registerUser("kyl_1", "Ch&seC@ke99!", "+27821234567");
        assertTrue(login.loginUser("kyl_1", "Ch&seC@ke99!"));
        assertEquals("Welcome kyl_1, it is great to see you again.",
                login.returnLoginStatus("kyl_1", "Ch&seC@ke99!"));
    }

    @Test
    public void testLoginFailsWithWrongDetails() {
        login.registerUser("kyl_1", "Ch&seC@ke99!", "+27821234567");
        assertFalse(login.loginUser("kyl_1", "wrong"));
        assertEquals("Username or password incorrect, please try again.",
                login.returnLoginStatus("kyl_1", "wrong"));
    }
}