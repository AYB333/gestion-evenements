package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.dao.OrganisateurDAO;
import com.ayoub.gestionevenements.model.Organisateur;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.AuthService;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\-\\s]{8,20}$");

    @Inject
    private AuthService authService;

    @Inject
    private OrganisateurDAO organisateurDAO;

    @Inject
    private SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("register".equalsIgnoreCase(action)) {
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // The same /auth endpoint handles both forms: registration and login.
        String action = req.getParameter("action");
        if ("register".equalsIgnoreCase(action)) {
            handleRegister(req, resp);
        } else {
            handleLogin(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String roleParam = req.getParameter("role");
        String organisationName = req.getParameter("organisationName");
        String telephone = req.getParameter("telephone");

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            req.setAttribute("error", "Veuillez remplir tous les champs obligatoires.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            req.setAttribute("error", "Adresse email invalide.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (!isStrongPassword(password)) {
            req.setAttribute("error", "Mot de passe trop faible: au moins 8 caracteres avec lettre et chiffre.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (fullName.trim().length() < 3 || fullName.trim().length() > 120) {
            req.setAttribute("error", "Nom complet invalide.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(password);
        user.setRole(resolveRole(roleParam));

        // Organisateur users need extra profile data stored in a dedicated table.
        if (user.getRole() == User.Role.ORGANISATEUR) {
            if (organisationName == null || organisationName.isBlank()) {
                req.setAttribute("error", "Veuillez saisir le nom de l'organisation.");
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
                return;
            }
        }

        User created = authService.register(user);
        if (created == null) {
            req.setAttribute("error", "Email deja utilise ou donnees invalides.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (created.getRole() == User.Role.ORGANISATEUR) {
            Organisateur organisateur = new Organisateur();
            organisateur.setUser(created);
            organisateur.setOrganisationName(organisationName.trim());
            if (telephone != null && !telephone.isBlank()) {
                if (!PHONE_PATTERN.matcher(telephone.trim()).matches()) {
                    req.setAttribute("error", "Numero de telephone invalide.");
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }
                organisateur.setTelephone(telephone.trim());
            }
            organisateurDAO.create(organisateur);
        }

        AuthenticationStatus status = securityContext.authenticate(
                req,
                resp,
                AuthenticationParameters.withParams()
                        .credential(new UsernamePasswordCredential(normalizedEmail, new Password(password)))
        );

        // JSP pages read sessionScope.user/role directly, so we mirror the
        // authenticated principal into the HTTP session after login/register.
        if (status == AuthenticationStatus.SEND_CONTINUE) {
            return;
        }
        if (status != AuthenticationStatus.SUCCESS) {
            req.setAttribute("error", "Impossible de connecter le compte. Merci de reessayer.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        setSessionUser(session, created);
        resp.sendRedirect(req.getContextPath() + resolveHomePath(created));
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Identifiants invalides.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            req.setAttribute("error", "Adresse email invalide.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        AuthenticationStatus status = securityContext.authenticate(
                req,
                resp,
                AuthenticationParameters.withParams()
                        .credential(new UsernamePasswordCredential(normalizedEmail, new Password(password)))
        );

        if (status == AuthenticationStatus.SEND_CONTINUE) {
            return;
        }
        if (status != AuthenticationStatus.SUCCESS) {
            req.setAttribute("error", "Email ou mot de passe incorrect.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = authService.login(normalizedEmail, password);
        if (user == null) {
            req.setAttribute("error", "Email ou mot de passe incorrect.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        setSessionUser(session, user);

        resp.sendRedirect(req.getContextPath() + resolveHomePath(user));
    }

    private void setSessionUser(HttpSession session, User user) {
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        if (session.getAttribute("csrfToken") == null) {
            // A single session token is reused by all state-changing forms.
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
    }

    private User.Role resolveRole(String roleParam) {
        if (roleParam == null) {
            return User.Role.PARTICIPANT;
        }
        if ("ORGANISATEUR".equalsIgnoreCase(roleParam)) {
            return User.Role.ORGANISATEUR;
        }
        return User.Role.PARTICIPANT;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return EMAIL_PATTERN.matcher(normalized).matches() ? normalized : null;
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    private String resolveHomePath(User user) {
        if (user == null || user.getRole() == null) {
            return "/events";
        }
        return switch (user.getRole()) {
            case ADMIN -> "/admin/events";
            case ORGANISATEUR -> "/organisateur/events";
            case PARTICIPANT -> "/events";
        };
    }
}
