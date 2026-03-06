package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserDAO userDAO;

    public User register(User user) {
        if (user == null) {
            return null;
        }
        String email = normalizeEmail(user.getEmail());
        if (email == null) {
            return null;
        }
        user.setEmail(email);

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            return null;
        }

        User existing = userDAO.findByEmail(email);
        if (existing != null) {
            return null;
        }

        return userDAO.create(user);
    }

    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || password == null) {
            return null;
        }

        User user = userDAO.findByEmail(normalizedEmail);
        if (user == null || !user.isEnabled()) {
            return null;
        }

        if (!password.equals(user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
    }
}
