package com.example.gotix.service;

import com.example.gotix.model.User;
import com.example.gotix.store.UserStore;
import java.util.UUID;

public class UserService {
    public User registerUser(String email, String phone) {
        String id = UUID.randomUUID().toString();
        User user = new User(id, email, phone, System.currentTimeMillis());
        UserStore.addUser(user);
        return user;
    }
}
