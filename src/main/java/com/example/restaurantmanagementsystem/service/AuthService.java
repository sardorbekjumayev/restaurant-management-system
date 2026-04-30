package com.example.restaurantmanagementsystem.service;

import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.repository.AuthRepository;

public class AuthService {
    private final AuthRepository authRepository = new AuthRepository();

    public User login(String username, String password, String role) {
        return authRepository.authenticate(username, password, role)
                .orElseThrow(() -> new IllegalArgumentException("Login, parol yoki role noto'g'ri"));
    }
}
