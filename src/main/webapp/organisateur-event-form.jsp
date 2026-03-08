<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Evenement | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />
<c:set var="formTitre" value="${empty param.titre ? event.titre : param.titre}" />
<c:set var="formDescription" value="${empty param.description ? event.description : param.description}" />
<c:set var="formCategorie" value="${empty param.categorie ? event.categorie : param.categorie}" />
<c:set var="formLieu" value="${empty param.lieu ? event.lieu : param.lieu}" />
<c:set var="formDateDebut" value="${empty param.dateDebut ? dateDebutValue : param.dateDebut}" />
<c:set var="formDateFin" value="${empty param.dateFin ? dateFinValue : param.dateFin}" />
<c:set var="formPrix" value="${empty param.prix ? event.prix : param.prix}" />
<c:set var="formCapacite" value="${empty param.capacite ? event.capacite : param.capacite}" />

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
    <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
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
          <input type="text" name="titre" class="form-control" value="${fn:escapeXml(formTitre)}" required>
        </div>
        <div class="mb-3">
          <label class="form-label">Description</label>
          <textarea name="description" class="form-control" rows="4"><c:out value="${formDescription}" /></textarea>
        </div>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Categorie</label>
            <select name="categorie" class="form-select" required>
              <option value="">Choisir une categorie</option>
              <c:forEach items="${categories}" var="category">
                <option value="${fn:escapeXml(category)}" <c:if test="${formCategorie == category}">selected</c:if>><c:out value="${category}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">Lieu</label>
            <select name="lieu" class="form-select" required>
              <option value="">Choisir une ville</option>
              <c:forEach items="${cities}" var="city">
                <option value="${fn:escapeXml(city)}" <c:if test="${formLieu == city}">selected</c:if>><c:out value="${city}" /></option>
              </c:forEach>
            </select>
          </div>
        </div>
        <div class="row g-3 mt-1">
          <div class="col-md-6">
            <label class="form-label">Date debut</label>
            <input type="datetime-local" name="dateDebut" class="form-control" value="${fn:escapeXml(formDateDebut)}" min="${fn:escapeXml(minDateTimeNow)}" required>
          </div>
          <div class="col-md-6">
            <label class="form-label">Date fin</label>
            <input type="datetime-local" name="dateFin" class="form-control" value="${fn:escapeXml(formDateFin)}" min="${fn:escapeXml(not empty formDateDebut ? formDateDebut : minDateTimeNow)}">
          </div>
        </div>
        <div class="row g-3 mt-1">
          <div class="col-md-6">
            <label class="form-label">Prix (MAD)</label>
            <input type="number" step="0.01" min="0" name="prix" class="form-control" value="${formPrix}" required>
          </div>
          <div class="col-md-6">
            <label class="form-label">Capacite</label>
            <input type="number" min="1" name="capacite" class="form-control" value="${formCapacite}" required>
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

