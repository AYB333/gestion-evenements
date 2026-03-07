<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Evenements | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="d-flex align-items-end justify-content-between mb-4">
    <div>
      <h1 class="h3 mb-1">Evenements publies</h1>
      <div class="text-secondary">Decouvrez les prochaines experiences disponibles.</div>
    </div>
  </div>

  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0">${error}</div>
  </c:if>

  <c:choose>
    <c:when test="${empty events}">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4">
          <div class="text-secondary">Aucun evenement publie pour le moment.</div>
        </div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="row g-4">
        <c:forEach items="${events}" var="event">
          <div class="col-12 col-md-6 col-lg-4">
            <div class="card shadow-sm rounded-4 border-0 h-100">
              <div class="card-body p-4">
                <span class="badge badge-soft mb-3">${event.categorie}</span>
                <h2 class="h5 mb-2">${event.titre}</h2>
                <div class="text-secondary small mb-3">${event.lieu} - ${event.dateDebut}</div>
                <p class="text-secondary">${event.description}</p>
              </div>
              <div class="card-footer bg-white border-0 p-4 pt-0">
                <div class="d-flex align-items-center justify-content-between">
                  <div class="fw-semibold">${event.prix} MAD</div>
                  <c:choose>
                    <c:when test="${not empty sessionScope.user && sessionScope.role == 'PARTICIPANT'}">
                      <form method="post" action="${pageContext.request.contextPath}/reserver" class="m-0">
                        <input type="hidden" name="eventId" value="${event.id}">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <button type="submit" class="btn btn-accent btn-sm">Reserver</button>
                      </form>
                    </c:when>
                    <c:when test="${not empty sessionScope.user}">
                      <span class="text-secondary small">Reservation reservee aux participants.</span>
                    </c:when>
                    <c:otherwise>
                      <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/auth">Se connecter</a>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</main>

<jsp:include page="/includes/footer.jsp" />

