package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserDAO userDAO;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

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

        String hashed = passwordHash.generate(user.getPasswordHash().toCharArray());
        user.setPasswordHash(hashed);

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

        if (!verifyOrMigratePassword(user, password)) {
            return null;
        }

        return user;
    }

    private boolean verifyOrMigratePassword(User user, String rawPassword) {
        try {
            return passwordHash.verify(rawPassword.toCharArray(), user.getPasswordHash());
        } catch (IllegalArgumentException ex) {
            // Legacy plaintext password stored before hashing was enabled.
            if (!rawPassword.equals(user.getPasswordHash())) {
                return false;
            }
            String hashed = passwordHash.generate(rawPassword.toCharArray());
            user.setPasswordHash(hashed);
            userDAO.update(user);
            return true;
        }
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
