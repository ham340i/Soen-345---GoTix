package com.example.gotix.store;

import com.example.gotix.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserStore {
    private static List<User> users = new ArrayList<>();

    static {
        // Default users
        users.add(new User("1", "admin", "admin123", "admin", System.currentTimeMillis()));
        users.add(new User("2", "user", "user123", "end_user", System.currentTimeMillis()));
    }

    public static void addUser(User user) {
        users.add(user);
    }

    public static User findUser(String username, String password, String role) {
        for (User user : users) {
            if (user.getUsername().equals(username) && 
                user.getPassword().equals(password) && 
                user.getRole().equals(role)) {
                return user;
            }
        }
        return null;
    }

    public static boolean exists(String username, String role) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public static List<User> getAll() {
        return users;
    }
}
