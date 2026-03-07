<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Utilisateurs | Gestion Evenements" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="mb-4">
    <h1 class="h3 mb-1">Gestion des utilisateurs</h1>
    <div class="text-secondary">Activer ou desactiver les comptes.</div>
  </div>

  <c:choose>
    <c:when test="${empty users}">
      <div class="card shadow-sm rounded-4 border-0">
        <div class="card-body p-4 text-secondary">Aucun utilisateur trouve.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card shadow-sm rounded-4 border-0">
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead class="table-light">
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
                  <td class="fw-semibold">${user.fullName}</td>
                  <td>${user.email}</td>
                  <td>${user.role}</td>
                  <td>
                    <c:choose>
                      <c:when test="${user.enabled}">Actif</c:when>
                      <c:otherwise>Bloque</c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end">
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

