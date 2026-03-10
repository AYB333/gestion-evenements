<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Validation evenements | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="mb-4">
    <div class="hero-kicker">Administration</div>
    <div class="page-title">Validation des evenements</div>
    <div class="page-subtitle">Approuvez ou refusez les demandes en attente.</div>
    <div class="d-flex flex-wrap gap-2 mt-3">
      <span class="badge-soft">Auto-refresh 30s</span>
      <span class="badge-soft">Derniere mise a jour: <c:out value="${lastRefreshAt}" /></span>
      <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/admin/events?export=csv">Exporter CSV</a>
    </div>
  </div>

  <c:if test="${not empty success}">
    <div class="alert alert-success border-0"><c:out value="${success}" /></div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
  </c:if>

  <div class="row g-3 mb-4">
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Utilisateurs</div>
        <div class="stat-value"><c:out value="${totalUsers}" /></div>
      </div>
    </div>
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Participants</div>
        <div class="stat-value"><c:out value="${participantCount}" /></div>
      </div>
    </div>
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Organisateurs</div>
        <div class="stat-value"><c:out value="${organisateurCount}" /></div>
      </div>
    </div>
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Publies</div>
        <div class="stat-value"><c:out value="${publishedCount}" /></div>
      </div>
    </div>
  </div>

  <div class="row g-3 mb-4">
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">En attente</div>
        <div class="stat-value"><c:out value="${pendingCount}" /></div>
      </div>
    </div>
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Total evenements</div>
        <div class="stat-value"><c:out value="${totalEvents}" /></div>
      </div>
    </div>
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Tickets vendus</div>
        <div class="stat-value"><c:out value="${paidTickets}" /></div>
      </div>
    </div>
    <div class="col-12 col-md-3">
      <div class="card metric-card stat-card">
        <div class="stat-label">Total tickets</div>
        <div class="stat-value"><c:out value="${totalTickets}" /></div>
      </div>
    </div>
  </div>

  <div class="card section-surface mb-4">
    <div class="card-body p-4">
      <div class="panel-toolbar">
        <div>
          <div class="panel-title">Evenements populaires</div>
          <div class="panel-subtitle">Classement par billets payes.</div>
        </div>
        <span class="count-badge"><c:out value="${fn:length(topEvents)}" /> lignes</span>
      </div>
      <c:choose>
        <c:when test="${empty topEvents}">
          <div class="empty-panel">Aucune vente payee pour le moment.</div>
        </c:when>
        <c:otherwise>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Evenement</th>
                  <th>Billets payes</th>
                  <th>Revenus (MAD)</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach items="${topEvents}" var="row">
                  <tr>
                    <td class="fw-semibold"><c:out value="${row.titre}" /></td>
                    <td><c:out value="${row.ticketsVendus}" /></td>
                    <td><c:out value="${row.revenus}" /></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty events}">
      <div class="card section-surface">
        <div class="empty-panel">Aucun evenement en attente.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card section-surface">
        <div class="card-body p-4 pb-0">
          <div class="panel-toolbar">
            <div>
              <div class="panel-title">File de validation</div>
              <div class="panel-subtitle">Evenements en attente de decision admin.</div>
            </div>
            <span class="count-badge"><c:out value="${pendingCount}" /> en attente</span>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead>
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
                  <td class="fw-semibold"><c:out value="${event.titre}" /></td>
                  <td><c:out value="${event.organisateur.user.fullName}" /></td>
                  <td><c:out value="${dateDisplayByEvent[event.id]}" /></td>
                  <td><c:out value="${event.lieu}" /></td>
                  <td class="text-end">
                    <div class="table-actions">
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

