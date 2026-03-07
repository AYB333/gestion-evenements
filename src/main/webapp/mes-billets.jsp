<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Mes billets | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="d-flex align-items-end justify-content-between mb-4 flex-wrap gap-3">
    <div>
      <h1 class="h3 mb-1">Mes billets</h1>
      <div class="text-secondary">Historique de vos reservations et achats.</div>
    </div>
    <form method="get" action="${pageContext.request.contextPath}/mes-billets" class="d-flex align-items-center gap-2">
      <label class="text-secondary small">Filtrer:</label>
      <select name="status" class="form-select form-select-sm">
        <option value="ALL" <c:if test="${statusFilter == 'ALL'}">selected</c:if>>Tous</option>
        <option value="RESERVE" <c:if test="${statusFilter == 'RESERVE'}">selected</c:if>>Reserve</option>
        <option value="PAYE" <c:if test="${statusFilter == 'PAYE'}">selected</c:if>>Paye</option>
        <option value="ANNULE" <c:if test="${statusFilter == 'ANNULE'}">selected</c:if>>Annule</option>
      </select>
      <button type="submit" class="btn btn-outline-secondary btn-sm">Appliquer</button>
    </form>
  </div>

  <c:choose>
    <c:when test="${empty tickets}">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4">
          <div class="text-secondary">Aucun billet trouve pour le moment.</div>
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
                    <div class="text-secondary small">${ticket.event.lieu} - ${ticket.event.dateDebut}</div>
                    <h2 class="h5 mb-1">${ticket.event.titre}</h2>
                  </div>
                  <span class="badge badge-soft">
                    <c:choose>
                      <c:when test="${ticket.statut == 'PAYE'}">Paye</c:when>
                      <c:when test="${ticket.statut == 'RESERVE'}">Reserve</c:when>
                      <c:when test="${ticket.statut == 'ANNULE'}">Annule</c:when>
                      <c:otherwise>${ticket.statut}</c:otherwise>
                    </c:choose>
                  </span>
                </div>
                <div class="text-secondary small mb-1">Reserve le: ${ticket.createdAt}</div>
                <div class="text-secondary mb-2">Code: ${ticket.code}</div>
                <div class="fw-semibold">${ticket.prix} MAD</div>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>

      <c:if test="${totalPages > 1}">
        <nav class="mt-4">
          <ul class="pagination justify-content-center">
            <li class="page-item <c:if test='${currentPage == 1}'>disabled</c:if>">
              <c:url var="prevUrl" value="/mes-billets">
                <c:param name="page" value="${currentPage - 1}" />
                <c:if test="${statusFilter != 'ALL'}">
                  <c:param name="status" value="${statusFilter}" />
                </c:if>
              </c:url>
              <a class="page-link" href="${prevUrl}">Precedent</a>
            </li>

            <c:forEach begin="1" end="${totalPages}" var="p">
              <li class="page-item <c:if test='${p == currentPage}'>active</c:if>">
                <c:url var="pageUrl" value="/mes-billets">
                  <c:param name="page" value="${p}" />
                  <c:if test="${statusFilter != 'ALL'}">
                    <c:param name="status" value="${statusFilter}" />
                  </c:if>
                </c:url>
                <a class="page-link" href="${pageUrl}">${p}</a>
              </li>
            </c:forEach>

            <li class="page-item <c:if test='${currentPage == totalPages}'>disabled</c:if>">
              <c:url var="nextUrl" value="/mes-billets">
                <c:param name="page" value="${currentPage + 1}" />
                <c:if test="${statusFilter != 'ALL'}">
                  <c:param name="status" value="${statusFilter}" />
                </c:if>
              </c:url>
              <a class="page-link" href="${nextUrl}">Suivant</a>
            </li>
          </ul>
        </nav>
      </c:if>
    </c:otherwise>
  </c:choose>
</main>

<jsp:include page="/includes/footer.jsp" />

