<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
      </div>
    </div>
    <div class="col-12 col-lg-7">
      <div class="clean-section p-4 p-lg-5 h-100">
        <div class="mb-4">
          <div class="brand h4 mb-1">Creer un compte</div>
          <div class="text-secondary">Rejoignez la plateforme</div>
        </div>

        <c:if test="${not empty error}">
          <div class="alert alert-warning border-0">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/auth">
          <input type="hidden" name="action" value="register">
          <div class="mb-3">
            <label class="form-label">Nom complet</label>
            <input type="text" name="fullName" class="form-control" placeholder="Ex: Amina El Idrissi" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" placeholder="vous@exemple.com" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Mot de passe</label>
            <input type="password" name="password" class="form-control" placeholder="********" required>
          </div>
          <div class="mb-4">
            <label class="form-label">Role</label>
            <select name="role" class="form-select">
              <option value="PARTICIPANT" selected>Participant</option>
              <option value="ORGANISATEUR">Organisateur</option>
            </select>
          </div>
          <div id="orgFields" class="mb-4 d-none">
            <div class="mb-3">
              <label class="form-label">Nom de l'organisation</label>
              <input type="text" name="organisationName" class="form-control" placeholder="Ex: Association Culture">
            </div>
            <div>
              <label class="form-label">Telephone (optionnel)</label>
              <input type="text" name="telephone" class="form-control" placeholder="Ex: 06 12 34 56 78">
            </div>
          </div>
          <button type="submit" class="btn btn-accent w-100 py-2">Creer mon compte</button>
        </form>

        <div class="text-center mt-4">
          <span class="text-secondary">Deja inscrit ?</span>
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

