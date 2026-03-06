<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/includes/header.jsp">
  <jsp:param name="pageTitle" value="Connexion | Gestion Événements" />
</jsp:include>

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-md-7 col-lg-5">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4 p-lg-5">
          <div class="text-center mb-4">
            <div class="brand h4 mb-1">Gestion Événements</div>
            <div class="text-secondary">Accédez à votre espace</div>
          </div>

          <c:if test="${not empty error}">
            <div class="alert alert-warning border-0">${error}</div>
          </c:if>

          <form method="post" action="${pageContext.request.contextPath}/auth">
            <input type="hidden" name="action" value="login">
            <div class="mb-3">
              <label class="form-label">Email</label>
              <input type="email" name="email" class="form-control" placeholder="vous@exemple.com" required>
            </div>
            <div class="mb-4">
              <label class="form-label">Mot de passe</label>
              <input type="password" name="password" class="form-control" placeholder="••••••••" required>
            </div>
            <button type="submit" class="btn btn-accent w-100 py-2">Se connecter</button>
          </form>

          <div class="text-center mt-4">
            <span class="text-secondary">Pas de compte ?</span>
            <a class="link-accent" href="${pageContext.request.contextPath}/auth?action=register">Créer un compte</a>
          </div>
        </div>
      </div>
      <div class="text-center text-secondary small mt-3">© 2026 Gestion Événements</div>
    </div>
  </div>
</div>

<jsp:include page="/includes/footer.jsp" />
