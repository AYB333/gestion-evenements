<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Connexion | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="row g-4 align-items-stretch">
    <div class="col-12 col-lg-5">
      <div class="hero-panel auth-hero h-100">
        <div class="hero-kicker">Bienvenue</div>
        <div class="hero-title">Gestion Evenements</div>
        <p class="text-secondary mb-4">
          Accedez a votre espace pour reserver des places et suivre vos billets.
        </p>
        <ul class="hero-list">
          <li><span class="hero-dot"></span>Evenements publies et mis a jour en temps reel.</li>
          <li><span class="hero-dot"></span>Reservations securisees et historiques clairs.</li>
          <li><span class="hero-dot"></span>Acces rapide a vos billets.</li>
        </ul>
        <div class="auth-mini-grid">
          <div class="auth-mini-card">
            <div class="auth-mini-label">Acces</div>
            <div class="auth-mini-value">3 roles</div>
          </div>
          <div class="auth-mini-card">
            <div class="auth-mini-label">Paiement</div>
            <div class="auth-mini-value">Carte / PayPal / Stripe</div>
          </div>
          <div class="auth-mini-card">
            <div class="auth-mini-label">Billets</div>
            <div class="auth-mini-value">QR inclus</div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-12 col-lg-7">
      <div class="clean-section auth-form-panel p-4 p-lg-5 h-100">
        <div class="mb-4">
          <div class="auth-form-title">Connexion</div>
          <div class="auth-form-subtitle">Accedez a votre espace securise.</div>
        </div>
        <div class="auth-form-divider"></div>

        <c:if test="${not empty error}">
          <div class="alert alert-warning border-0">${error}</div>
        </c:if>
        <c:if test="${param.error == '1'}">
          <div class="alert alert-warning border-0">Email ou mot de passe incorrect.</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/auth">
          <input type="hidden" name="action" value="login">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" placeholder="vous@exemple.com" value="${fn:escapeXml(param.email)}" required>
          </div>
          <div class="mb-4">
            <label class="form-label">Mot de passe</label>
            <input type="password" name="password" class="form-control" placeholder="********" required>
          </div>
          <button type="submit" class="btn btn-accent w-100 py-2">Se connecter</button>
        </form>

        <div class="auth-form-grid">
          <div class="auth-support-card">
            <div class="auth-support-title">Acces direct</div>
            <p class="auth-support-text">Connectez-vous selon votre role pour acceder a votre espace, vos evenements ou votre tableau de validation.</p>
          </div>
          <div class="auth-support-card">
            <div class="auth-support-title">Suivi instantane</div>
            <p class="auth-support-text">Retrouvez vos billets, vos paiements et vos actions recentes sans repasser par plusieurs pages.</p>
          </div>
        </div>

        <div class="text-center mt-4">
          <span class="auth-footer-note">Pas de compte ?</span>
          <a class="link-accent" href="${pageContext.request.contextPath}/auth?action=register">Creer un compte</a>
        </div>
        <div class="text-center text-secondary small mt-4">(c) 2026 Gestion Evenements</div>
      </div>
    </div>
  </div>
</main>

<jsp:include page="/includes/footer.jsp" />
