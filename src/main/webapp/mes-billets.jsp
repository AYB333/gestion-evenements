<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/includes/header.jsp">
  <jsp:param name="pageTitle" value="Mes billets | Gestion Événements" />
</jsp:include>

<main class="container py-5">
  <div class="d-flex align-items-end justify-content-between mb-4">
    <div>
      <h1 class="h3 mb-1">Mes billets</h1>
      <div class="text-secondary">Historique de vos réservations et achats.</div>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty tickets}">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4">
          <div class="text-secondary">Aucun billet trouvé pour le moment.</div>
        </div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="row g-4">
        <c:forEach items="${tickets}" var="ticket">
          <div class="col-12 col-md-6">
            <div class="card shadow-sm rounded-4 border-0 h-100">
              <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-start mb-3">
                  <div>
                    <div class="text-secondary small">${ticket.event.lieu} · ${ticket.event.dateDebut}</div>
                    <h2 class="h5 mb-1">${ticket.event.titre}</h2>
                  </div>
                  <span class="badge badge-soft">${ticket.statut}</span>
                </div>
                <div class="text-secondary mb-2">Code: ${ticket.code}</div>
                <div class="fw-semibold">${ticket.prix} MAD</div>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</main>

<jsp:include page="/includes/footer.jsp" />
