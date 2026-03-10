<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Mes evenements | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="d-flex flex-column flex-lg-row align-items-start align-items-lg-center justify-content-between gap-3 mb-4">
    <div>
      <div class="hero-kicker">Organisateur</div>
      <div class="page-title">Tableau de bord</div>
      <div class="page-subtitle">Suivez vos evenements, ventes et capacites.</div>
      <div class="d-flex flex-wrap gap-2 mt-3">
        <span class="badge-soft">Auto-refresh 30s</span>
        <span class="badge-soft">Derniere mise a jour: <c:out value="${lastRefreshAt}" /></span>
      </div>
    </div>
    <div class="d-flex flex-wrap gap-2">
      <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/organisateur/events?export=csv">Exporter CSV</a>
      <a class="btn btn-accent" href="${pageContext.request.contextPath}/organisateur/events?action=new">Nouvel evenement</a>
    </div>
  </div>

  <div class="row g-4 mb-4">
    <div class="col-6 col-lg-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Total evenements</div>
        <div class="stat-value"><c:out value="${totalEvents}" /></div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Tickets vendus</div>
        <div class="stat-value"><c:out value="${paidTickets}" /></div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Publies</div>
        <div class="stat-value"><c:out value="${publishedCount}" /></div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Revenus</div>
        <div class="stat-value"><c:out value="${revenue}" /> MAD</div>
      </div>
    </div>
  </div>

  <c:if test="${not empty success}">
    <div class="alert alert-success border-0"><c:out value="${success}" /></div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
  </c:if>

  <div class="d-flex align-items-center justify-content-between mb-3" id="events-table">
    <div class="section-title">
      <div class="h5 mb-0">Mes evenements</div>
    <div class="text-secondary small">Liste complete de vos evenements.</div>
    </div>
    <span class="badge-soft"><c:out value="${totalEvents}" /> total</span>
  </div>

  <c:choose>
    <c:when test="${empty events}">
      <div class="card section-surface">
        <div class="empty-panel">Aucun evenement pour le moment.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card section-surface">
        <div class="card-body p-4 pb-0">
          <div class="panel-toolbar">
            <div>
              <div class="panel-title">Gestion de vos evenements</div>
              <div class="panel-subtitle">Consultez statut, ventes et capacite restante.</div>
            </div>
            <span class="count-badge"><c:out value="${totalEvents}" /> evenements</span>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead>
              <tr>
                <th>Titre</th>
                <th>Categorie</th>
                <th>Date</th>
                <th>Statut</th>
                <th>Reserves</th>
                <th>Vendus</th>
                <th>Revenus</th>
                <th>Capacite</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${events}" var="event">
                <tr>
                  <td class="fw-semibold"><c:out value="${event.titre}" /></td>
                  <td><span class="value-chip"><c:out value="${event.categorie}" /></span></td>
                  <td><c:out value="${dateDisplayByEvent[event.id]}" /></td>
                  <td><span class="badge badge-soft"><c:out value="${event.statut}" /></span></td>
                  <td><c:out value="${empty reservedByEvent[event.id] ? 0 : reservedByEvent[event.id]}" /></td>
                  <td><c:out value="${empty soldByEvent[event.id] ? 0 : soldByEvent[event.id]}" /></td>
                  <td><c:out value="${empty revenueByEvent[event.id] ? 0 : revenueByEvent[event.id]}" /> MAD</td>
                  <td><span class="value-chip"><c:out value="${event.capacite}" /> places</span></td>
                  <td class="text-end">
                    <div class="table-actions">
                      <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/organisateur/events?action=edit&id=${event.id}">Modifier</a>
                      <c:if test="${event.statut ne 'PUBLIE'}">
                        <form method="post" action="${pageContext.request.contextPath}/organisateur/events" class="d-inline">
                          <input type="hidden" name="action" value="delete">
                          <input type="hidden" name="id" value="${event.id}">
                          <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                          <button type="submit" class="btn btn-outline-danger btn-sm">Supprimer</button>
                        </form>
                      </c:if>
                    </div>
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

