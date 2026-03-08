<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Utilisateurs | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="mb-4">
    <div class="hero-kicker">Administration</div>
    <div class="page-title">Gestion des utilisateurs</div>
    <div class="page-subtitle">Activez, bloquez et surveillez les comptes de la plateforme.</div>
  </div>

  <c:if test="${not empty success}">
    <div class="alert alert-success border-0"><c:out value="${success}" /></div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
  </c:if>

  <c:choose>
    <c:when test="${empty users}">
      <div class="card section-surface">
        <div class="empty-panel">Aucun utilisateur trouve.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card section-surface">
        <div class="card-body p-4 pb-0">
          <div class="panel-toolbar">
            <div>
              <div class="panel-title">Liste des comptes</div>
              <div class="panel-subtitle">Vue complete des profils inscrits.</div>
            </div>
            <span class="count-badge"><c:out value="${fn:length(users)}" /> comptes</span>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Email</th>
                <th>Role</th>
                <th>Statut</th>
                <th class="text-end">Action</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${users}" var="user">
                <tr>
                  <td class="fw-semibold"><c:out value="${user.fullName}" /></td>
                  <td><c:out value="${user.email}" /></td>
                  <td><span class="value-chip"><c:out value="${user.role}" /></span></td>
                  <td>
                    <c:choose>
                      <c:when test="${user.enabled}"><span class="value-chip">Actif</span></c:when>
                      <c:otherwise><span class="badge badge-soft">Bloque</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end">
                    <c:choose>
                      <c:when test="${user.role == 'ADMIN' || user.id == currentUserId}">
                        <span class="badge badge-soft">Protege</span>
                      </c:when>
                      <c:otherwise>
                        <div class="table-actions">
                          <form method="post" action="${pageContext.request.contextPath}/admin/users" class="d-inline">
                            <input type="hidden" name="id" value="${user.id}">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <button type="submit" class="btn btn-outline-secondary btn-sm">
                              <c:choose>
                                <c:when test="${user.enabled}">Desactiver</c:when>
                                <c:otherwise>Activer</c:otherwise>
                              </c:choose>
                            </button>
                          </form>
                        </div>
                      </c:otherwise>
                    </c:choose>
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

