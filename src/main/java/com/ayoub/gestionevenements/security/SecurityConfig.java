package com.ayoub.gestionevenements.security;

import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.annotation.security.DeclareRoles;

@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/auth",
                errorPage = "/auth?error=1"
        )
)
@DeclareRoles({"PARTICIPANT", "ORGANISATEUR", "ADMIN"})
public class SecurityConfig {
    // Central security entry point: protected pages redirect to /auth, then
    // Jakarta Security sends the user back to the original target after login.
}
