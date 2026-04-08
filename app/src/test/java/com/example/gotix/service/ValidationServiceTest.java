package com.example.gotix.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidationServiceTest {
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void testRegisterFailsWhenEmailAndPhoneEmpty() {
        assertFalse(validationService.isValidRegistration("", ""));
        assertFalse(validationService.isValidRegistration(null, null));
    }

    @Test
    void testRegisterPassesWhenEmailProvided() {
        assertTrue(validationService.isValidRegistration("test@example.com", ""));
    }

    @Test
    void testRegisterPassesWhenPhoneProvided() {
        assertTrue(validationService.isValidRegistration("", "123456789"));
    }
}
