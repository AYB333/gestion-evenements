<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Validation evenements | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="mb-4">
    <h1 class="h3 mb-1">Validation des evenements</h1>
    <div class="text-secondary">Approuvez ou refusez les demandes.</div>
  </div>

  <c:choose>
    <c:when test="${empty events}">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4 text-secondary">Aucun evenement en attente.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card shadow-sm rounded-4 border-0">
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Titre</th>
                <th>Organisateur</th>
                <th>Date debut</th>
                <th>Lieu</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${events}" var="event">
                <tr>
                  <td class="fw-semibold">${event.titre}</td>
                  <td>${event.organisateur.user.fullName}</td>
                  <td>${event.dateDebut}</td>
                  <td>${event.lieu}</td>
                  <td class="text-end">
                    <form method="post" action="${pageContext.request.contextPath}/admin/events" class="d-inline">
                      <input type="hidden" name="action" value="approve">
                      <input type="hidden" name="id" value="${event.id}">
                      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                      <button type="submit" class="btn btn-accent btn-sm">Approuver</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/admin/events" class="d-inline">
                      <input type="hidden" name="action" value="reject">
                      <input type="hidden" name="id" value="${event.id}">
                      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                      <button type="submit" class="btn btn-outline-danger btn-sm">Refuser</button>
                    </form>
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

