package com.ayoub.gestionevenements.security;

import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.util.Set;

@ApplicationScoped
public class UserIdentityStore implements IdentityStore {

    @Inject
    private UserDAO userDAO;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        // Called by Jakarta Security during login. We load the user, verify the
        // password, then return the role that will be used by @RolesAllowed.
        if (!(credential instanceof UsernamePasswordCredential upc)) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        String email = normalizeEmail(upc.getCaller());
        if (email == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        User user = userDAO.findByEmail(email);
        if (user == null || !user.isEnabled()) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        if (!verifyOrMigratePassword(user, upc.getPassword().getValue())) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        return new CredentialValidationResult(user.getEmail(), Set.of(user.getRole().name()));
    }

    private boolean verifyOrMigratePassword(User user, char[] rawPassword) {
        try {
            return passwordHash.verify(rawPassword, user.getPasswordHash());
        } catch (IllegalArgumentException ex) {
            // Seeded demo accounts may start with a plaintext password. The first
            // successful login upgrades them transparently to a hashed value.
            String rawString = new String(rawPassword);
            if (!rawString.equals(user.getPasswordHash())) {
                return false;
            }
            String hashed = passwordHash.generate(rawPassword);
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
