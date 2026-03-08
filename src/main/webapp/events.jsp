<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Evenements | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="d-flex flex-column flex-lg-row align-items-start align-items-lg-end justify-content-between gap-3 mb-4">
    <div>
      <div class="hero-kicker">Explorer</div>
      <div class="page-title">Evenements publies</div>
      <div class="page-subtitle">Decouvrez les evenements publies et reservez vos places en quelques clics.</div>
    </div>
    <span class="badge-soft"><c:out value="${fn:length(events)}" /> evenements</span>
  </div>

  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
  </c:if>

  <div class="row g-4">
    <div class="col-12 col-lg-3">
      <form class="card filter-panel" method="get" action="${pageContext.request.contextPath}/events">
        <div class="filter-title">Filtres</div>
        <div class="filter-group">
          <label class="form-label text-secondary">Recherche</label>
          <input type="text" class="form-control" name="q" placeholder="Titre ou description" value="${fn:escapeXml(filterQ)}">
        </div>
        <div class="filter-group">
          <label class="form-label text-secondary">Date</label>
          <input type="date" class="form-control" name="date" value="${filterDate}">
        </div>
        <div class="filter-group">
          <div class="form-label text-secondary">Categorie</div>
          <div class="form-check mb-2">
            <input class="form-check-input" type="radio" name="category" id="cat-all" value="ALL"
              <c:if test="${filterCategory eq 'ALL'}">checked</c:if>>
            <label class="form-check-label" for="cat-all">Toutes les categories</label>
          </div>
          <c:forEach items="${categories}" var="cat" varStatus="catStatus">
            <div class="form-check mb-2">
              <input class="form-check-input" type="radio" name="category" id="cat-${catStatus.index}" value="${fn:escapeXml(cat)}"
                <c:if test="${filterCategory eq cat}">checked</c:if>>
              <label class="form-check-label" for="cat-${catStatus.index}"><c:out value="${cat}" /></label>
            </div>
          </c:forEach>
        </div>
        <div class="filter-group">
          <label class="form-label text-secondary">Ville</label>
          <select class="form-select" name="city">
            <option value="ALL" <c:if test="${filterCity eq 'ALL'}">selected</c:if>>Toutes les villes</option>
            <c:forEach items="${cities}" var="city">
              <option value="${fn:escapeXml(city)}" <c:if test="${filterCity eq city}">selected</c:if>><c:out value="${city}" /></option>
            </c:forEach>
          </select>
        </div>
        <div class="d-grid gap-2">
          <button type="submit" class="btn btn-accent">Appliquer</button>
          <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/events">Effacer</a>
        </div>
      </form>
    </div>
    <div class="col-12 col-lg-9">
      <div class="panel-toolbar">
        <div>
          <div class="panel-title">Liste des evenements</div>
          <div class="panel-subtitle"><c:out value="${fn:length(events)}" /> resultats disponibles</div>
        </div>
        <span class="count-badge"><c:out value="${fn:length(events)}" /> resultats</span>
      </div>

      <c:choose>
        <c:when test="${empty events}">
          <div class="card p-4">
            <div class="text-secondary mb-3">Aucun evenement ne correspond a votre recherche.</div>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/events">Effacer les filtres</a>
          </div>
        </c:when>
        <c:otherwise>
          <div class="row g-4">
            <c:forEach items="${events}" var="event">
              <div class="col-12 col-md-6 col-xl-4">
                <div class="card event-card h-100">
                  <div class="card-body">
                    <c:set var="resState" value="${reservationStateByEvent[event.id]}" />
                    <span class="badge badge-soft mb-3"><c:out value="${categoryDisplayByEvent[event.id]}" /></span>
                    <h2 class="event-card-title"><c:out value="${event.titre}" /></h2>
                    <div class="event-meta"><c:out value="${event.lieu}" /> • <c:out value="${dateDisplayByEvent[event.id]}" /></div>
                    <p class="event-description">
                      <c:choose>
                        <c:when test="${not empty event.description}">
                          <c:out value="${event.description}" />
                        </c:when>
                        <c:otherwise>Description non renseignee.</c:otherwise>
                      </c:choose>
                    </p>
                    <div class="event-availability">
                      Places restantes: <c:out value="${event.capacite}" />
                    </div>
                  </div>
                  <div class="card-footer">
                    <div class="event-footer-meta">
                      <div class="event-price"><c:out value="${event.prix}" /><span>MAD</span></div>
                      <c:choose>
                        <c:when test="${not empty sessionScope.user && sessionScope.role == 'PARTICIPANT'}">
                          <c:choose>
                            <c:when test="${resState eq 'OK'}">
                              <form method="post" action="${pageContext.request.contextPath}/reserver" class="m-0">
                                <input type="hidden" name="eventId" value="${event.id}">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <button type="submit" class="btn btn-accent btn-sm">Reserver</button>
                              </form>
                            </c:when>
                            <c:when test="${resState eq 'COMPLET'}">
                              <span class="badge badge-soft">Complet</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge badge-soft">Date passee</span>
                            </c:otherwise>
                          </c:choose>
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
    </div>
  </div>
</main>

<jsp:include page="/includes/footer.jsp" />

