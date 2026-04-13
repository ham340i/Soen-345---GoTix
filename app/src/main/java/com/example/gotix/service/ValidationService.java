package com.example.gotix.service;

public class ValidationService {
    public boolean isValidRegistration(String email, String phone) {
        return (email != null && !email.trim().isEmpty()) || (phone != null && !phone.trim().isEmpty());
    }
}
