<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Mes billets | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="d-flex flex-column flex-lg-row align-items-start align-items-lg-end justify-content-between mb-4 gap-3">
    <div>
      <div class="hero-kicker">Participant</div>
      <div class="page-title">Mes billets</div>
      <div class="page-subtitle">Historique de vos reservations, paiements et transferts.</div>
    </div>
    <div class="d-flex flex-column flex-sm-row align-items-start align-items-sm-center gap-3">
      <span class="count-badge"><c:out value="${totalCount}" /> billets</span>
      <form method="get" action="${pageContext.request.contextPath}/mes-billets" class="d-flex align-items-center gap-2">
        <label class="text-secondary small">Filtrer:</label>
        <select name="status" class="form-select form-select-sm">
          <option value="ALL" <c:if test="${statusFilter == 'ALL'}">selected</c:if>>Tous</option>
          <option value="RESERVE" <c:if test="${statusFilter == 'RESERVE'}">selected</c:if>>Reserve</option>
          <option value="PAYE" <c:if test="${statusFilter == 'PAYE'}">selected</c:if>>Paye</option>
          <option value="ANNULE" <c:if test="${statusFilter == 'ANNULE'}">selected</c:if>>Annule</option>
          <option value="TRANSFERE" <c:if test="${statusFilter == 'TRANSFERE'}">selected</c:if>>Transfere</option>
        </select>
        <button type="submit" class="btn btn-outline-secondary btn-sm">Appliquer</button>
      </form>
    </div>
  </div>

  <c:if test="${not empty success}">
    <div class="alert alert-success border-0"><c:out value="${success}" /></div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
  </c:if>

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
            <div class="card ticket-card shadow-sm rounded-4 border-0 h-100">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                  <div>
                    <div class="ticket-card-meta"><c:out value="${ticket.event.lieu}" /> - <c:out value="${eventDateDisplayByTicket[ticket.id]}" /></div>
                    <h2 class="ticket-card-title"><c:out value="${ticket.event.titre}" /></h2>
                  </div>
                  <span class="status-pill
                    <c:choose>
                      <c:when test="${ticket.statut == 'PAYE'}">status-paye</c:when>
                      <c:when test="${ticket.statut == 'RESERVE'}">status-reserve</c:when>
                      <c:when test="${ticket.statut == 'ANNULE'}">status-annule</c:when>
                      <c:otherwise>status-transfere</c:otherwise>
                    </c:choose>">
                    <c:choose>
                      <c:when test="${ticket.statut == 'PAYE'}">Paye</c:when>
                      <c:when test="${ticket.statut == 'RESERVE'}">Reserve</c:when>
                      <c:when test="${ticket.statut == 'ANNULE'}">Annule</c:when>
                      <c:otherwise>Transfere</c:otherwise>
                    </c:choose>
                  </span>
                </div>

                <div class="ticket-detail-grid">
                  <div class="ticket-code-box">
                    <div class="ticket-info-label">Date de reservation</div>
                    <div class="ticket-info-value"><c:out value="${createdAtDisplayByTicket[ticket.id]}" /></div>
                  </div>
                  <div class="ticket-code-box">
                    <div class="ticket-info-label">Code du billet</div>
                    <div class="ticket-info-value"><c:out value="${ticket.code}" /></div>
                  </div>
                </div>

                <div class="ticket-summary-row">
                  <div class="ticket-price"><c:out value="${ticket.prix}" /><span>MAD</span></div>
                  <div class="ticket-actions">
                    <c:if test="${ticket.statut == 'RESERVE'}">
                      <a class="btn btn-accent btn-sm" href="${pageContext.request.contextPath}/paiement?ticketId=${ticket.id}">Payer</a>
                    </c:if>
                    <c:if test="${ticket.statut == 'RESERVE' || ticket.statut == 'PAYE'}">
                      <form method="post" action="${pageContext.request.contextPath}/mes-billets" class="d-inline">
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="ticketId" value="${ticket.id}">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <button type="submit" class="btn btn-danger-soft btn-sm">Annuler</button>
                      </form>
                    </c:if>
                  </div>
                </div>

                <c:if test="${ticket.statut == 'RESERVE' || ticket.statut == 'PAYE'}">
                  <form method="post" action="${pageContext.request.contextPath}/mes-billets" class="ticket-transfer-panel">
                    <input type="hidden" name="action" value="transfer">
                    <input type="hidden" name="ticketId" value="${ticket.id}">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <label class="form-label text-secondary small mb-2">Transferer vers email participant</label>
                    <div class="ticket-transfer-note">Le billet garde le meme evenement et change uniquement de proprietaire.</div>
                    <div class="ticket-transfer-row">
                      <input type="email" name="targetEmail" class="form-control form-control-sm" placeholder="participant@email.com" value="${fn:escapeXml(param.targetEmail)}" required>
                      <button type="submit" class="btn btn-outline-secondary btn-sm">Transferer</button>
                    </div>
                  </form>
                </c:if>
                <c:if test="${ticket.statut == 'PAYE' && not empty qrCodes[ticket.id]}">
                  <div class="ticket-qr-panel">
                    <div class="ticket-info-label">QR Code</div>
                    <img alt="QR Code" src="data:image/png;base64,${qrCodes[ticket.id]}">
                    <div class="ticket-qr-caption">Presentez ce QR code lors du controle du billet.</div>
                  </div>
                </c:if>
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

