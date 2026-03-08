<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Inscription | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="row g-4 align-items-stretch">
    <div class="col-12 col-lg-5">
      <div class="hero-panel auth-hero h-100">
        <div class="hero-kicker">Inscription</div>
        <div class="hero-title">Creer votre compte</div>
        <p class="text-secondary mb-4">
          Rejoignez la plateforme et commencez a gerer vos evenements ou a reserver vos billets.
        </p>
        <ul class="hero-list">
          <li><span class="hero-dot"></span>Choisissez votre role en quelques secondes.</li>
          <li><span class="hero-dot"></span>Suivi clair de vos reservations et evenements.</li>
          <li><span class="hero-dot"></span>Notifications et confirmations rapides.</li>
        </ul>
        <div class="auth-mini-grid">
          <div class="auth-mini-card">
            <div class="auth-mini-label">Participant</div>
            <div class="auth-mini-value">Reservation simple</div>
          </div>
          <div class="auth-mini-card">
            <div class="auth-mini-label">Organisateur</div>
            <div class="auth-mini-value">Gestion complete</div>
          </div>
          <div class="auth-mini-card">
            <div class="auth-mini-label">Admin</div>
            <div class="auth-mini-value">Validation & suivi</div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-12 col-lg-7">
      <div class="clean-section auth-form-panel p-4 p-lg-5 h-100">
        <div class="mb-4">
          <div class="auth-form-title">Creer un compte</div>
          <div class="auth-form-subtitle">Rejoignez la plateforme en quelques secondes.</div>
        </div>
        <div class="auth-form-divider"></div>

        <c:if test="${not empty error}">
          <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/auth">
          <input type="hidden" name="action" value="register">
          <div class="mb-3">
            <label class="form-label">Nom complet</label>
            <input type="text" name="fullName" class="form-control" placeholder="Ex: Amina El Idrissi" value="${fn:escapeXml(param.fullName)}" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" placeholder="vous@exemple.com" value="${fn:escapeXml(param.email)}" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Mot de passe</label>
            <input type="password" name="password" class="form-control" placeholder="********" required>
          </div>
          <div class="mb-4">
            <label class="form-label">Role</label>
            <select name="role" class="form-select">
              <option value="PARTICIPANT" <c:if test="${empty param.role || param.role == 'PARTICIPANT'}">selected</c:if>>Participant</option>
              <option value="ORGANISATEUR" <c:if test="${param.role == 'ORGANISATEUR'}">selected</c:if>>Organisateur</option>
            </select>
          </div>
          <div id="orgFields" class="mb-4 d-none">
            <div class="mb-3">
              <label class="form-label">Nom de l'organisation</label>
              <input type="text" name="organisationName" class="form-control" placeholder="Ex: Association Culture" value="${fn:escapeXml(param.organisationName)}">
            </div>
            <div>
              <label class="form-label">Telephone (optionnel)</label>
              <input type="text" name="telephone" class="form-control" placeholder="Ex: 06 12 34 56 78" value="${fn:escapeXml(param.telephone)}">
            </div>
          </div>
          <button type="submit" class="btn btn-accent w-100 py-2">Creer mon compte</button>
        </form>

        <div class="auth-form-grid">
          <div class="auth-support-card">
            <div class="auth-support-title">Choix du role</div>
            <p class="auth-support-text">Participant pour reserver et payer. Organisateur pour creer, suivre et gerer vos evenements.</p>
          </div>
          <div class="auth-support-card">
            <div class="auth-support-title">Parcours simple</div>
            <p class="auth-support-text">Inscription rapide, validation cote serveur et conservation des champs saisis en cas d'erreur.</p>
          </div>
        </div>

        <div class="text-center mt-4">
          <span class="auth-footer-note">Deja inscrit ?</span>
          <a class="link-accent" href="${pageContext.request.contextPath}/auth">Se connecter</a>
        </div>
        <div class="text-center text-secondary small mt-4">(c) 2026 Gestion Evenements</div>
      </div>
    </div>
  </div>
</main>

<jsp:include page="/includes/footer.jsp" />

<script>
  (function () {
    const roleSelect = document.querySelector('select[name="role"]');
    const orgFields = document.getElementById('orgFields');
    if (!roleSelect || !orgFields) return;
    const toggle = () => {
      if (roleSelect.value === 'ORGANISATEUR') {
        orgFields.classList.remove('d-none');
      } else {
        orgFields.classList.add('d-none');
      }
    };
    roleSelect.addEventListener('change', toggle);
    toggle();
  })();
</script>

