<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Mes evenements | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="d-flex align-items-center justify-content-between mb-4">
    <div>
      <h1 class="h3 mb-1">Mes evenements</h1>
      <div class="text-secondary">Gerez vos evenements et suivez leur statut.</div>
    </div>
    <a class="btn btn-accent" href="${pageContext.request.contextPath}/organisateur/events?action=new">Nouvel evenement</a>
  </div>

  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0">${error}</div>
  </c:if>

  <c:choose>
    <c:when test="${empty events}">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4 text-secondary">Aucun evenement pour le moment.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card shadow-sm rounded-4 border-0">
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Titre</th>
                <th>Date debut</th>
                <th>Lieu</th>
                <th>Statut</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${events}" var="event">
                <tr>
                  <td class="fw-semibold">${event.titre}</td>
                  <td>${event.dateDebut}</td>
                  <td>${event.lieu}</td>
                  <td><span class="badge badge-soft">${event.statut}</span></td>
                  <td class="text-end">
                    <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/organisateur/events?action=edit&id=${event.id}">Modifier</a>
                    <c:if test="${event.statut ne 'PUBLIE'}">
                      <form method="post" action="${pageContext.request.contextPath}/organisateur/events" class="d-inline">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${event.id}">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <button type="submit" class="btn btn-outline-danger btn-sm">Supprimer</button>
                      </form>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </c:otherwise>
  </c:choose>
</main>

<jsp:include page="/includes/footer.jsp" />

