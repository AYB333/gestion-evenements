<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Evenement | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="mb-4">
    <h1 class="h3 mb-1">
      <c:choose>
        <c:when test="${editMode}">Modifier l'evenement</c:when>
        <c:otherwise>Nouvel evenement</c:otherwise>
      </c:choose>
    </h1>
    <div class="text-secondary">Completez les informations principales.</div>
  </div>

  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0">${error}</div>
  </c:if>

  <div class="card shadow-sm rounded-4 border-0">
    <div class="card-body p-4 p-lg-5">
      <form method="post" action="${pageContext.request.contextPath}/organisateur/events">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <c:if test="${editMode}">
          <input type="hidden" name="id" value="${event.id}">
        </c:if>

        <div class="mb-3">
          <label class="form-label">Titre</label>
          <input type="text" name="titre" class="form-control" value="${event.titre}" required>
        </div>
        <div class="mb-3">
          <label class="form-label">Description</label>
          <textarea name="description" class="form-control" rows="4">${event.description}</textarea>
        </div>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Categorie</label>
            <select name="categorie" class="form-select" required>
              <option value="">Choisir une categorie</option>
              <option value="Concert" <c:if test="${event.categorie == 'Concert'}">selected</c:if>>Concert</option>
              <option value="Conference" <c:if test="${event.categorie == 'Conference'}">selected</c:if>>Conference</option>
              <option value="Spectacle" <c:if test="${event.categorie == 'Spectacle'}">selected</c:if>>Spectacle</option>
              <option value="Festival" <c:if test="${event.categorie == 'Festival'}">selected</c:if>>Festival</option>
              <option value="Theatre" <c:if test="${event.categorie == 'Theatre'}">selected</c:if>>Theatre</option>
              <option value="Cinema" <c:if test="${event.categorie == 'Cinema'}">selected</c:if>>Cinema</option>
              <option value="Sport" <c:if test="${event.categorie == 'Sport'}">selected</c:if>>Sport</option>
              <option value="Workshop" <c:if test="${event.categorie == 'Workshop'}">selected</c:if>>Workshop</option>
              <option value="Exposition" <c:if test="${event.categorie == 'Exposition'}">selected</c:if>>Exposition</option>
              <option value="Autre" <c:if test="${event.categorie == 'Autre'}">selected</c:if>>Autre</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">Lieu</label>
            <select name="lieu" class="form-select" required>
              <option value="">Choisir une ville</option>
              <option value="Casablanca" <c:if test="${event.lieu == 'Casablanca'}">selected</c:if>>Casablanca</option>
              <option value="Rabat" <c:if test="${event.lieu == 'Rabat'}">selected</c:if>>Rabat</option>
              <option value="Marrakech" <c:if test="${event.lieu == 'Marrakech'}">selected</c:if>>Marrakech</option>
              <option value="Fes" <c:if test="${event.lieu == 'Fes'}">selected</c:if>>Fes</option>
              <option value="Tanger" <c:if test="${event.lieu == 'Tanger'}">selected</c:if>>Tanger</option>
              <option value="Agadir" <c:if test="${event.lieu == 'Agadir'}">selected</c:if>>Agadir</option>
              <option value="Oujda" <c:if test="${event.lieu == 'Oujda'}">selected</c:if>>Oujda</option>
              <option value="Kenitra" <c:if test="${event.lieu == 'Kenitra'}">selected</c:if>>Kenitra</option>
              <option value="Tetouan" <c:if test="${event.lieu == 'Tetouan'}">selected</c:if>>Tetouan</option>
              <option value="Meknes" <c:if test="${event.lieu == 'Meknes'}">selected</c:if>>Meknes</option>
            </select>
          </div>
        </div>
        <div class="row g-3 mt-1">
          <div class="col-md-6">
            <label class="form-label">Date debut</label>
            <input type="datetime-local" name="dateDebut" class="form-control" value="${dateDebutValue}" required>
          </div>
          <div class="col-md-6">
            <label class="form-label">Date fin</label>
            <input type="datetime-local" name="dateFin" class="form-control" value="${dateFinValue}">
          </div>
        </div>
        <div class="row g-3 mt-1">
          <div class="col-md-6">
            <label class="form-label">Prix (MAD)</label>
            <input type="number" step="0.01" name="prix" class="form-control" value="${event.prix}" required>
          </div>
          <div class="col-md-6">
            <label class="form-label">Capacite</label>
            <input type="number" name="capacite" class="form-control" value="${event.capacite}" required>
          </div>
        </div>

        <div class="d-flex gap-2 mt-4">
          <button type="submit" class="btn btn-accent">Enregistrer</button>
          <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/organisateur/events">Annuler</a>
        </div>
      </form>
    </div>
  </div>
</main>

<jsp:include page="/includes/footer.jsp" />

