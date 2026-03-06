<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${empty param.pageTitle ? "Gestion Événements" : param.pageTitle}</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    :root {
      --ink: #1f2937;
      --indigo: #3b4a9a;
      --teal: #2f8f83;
      --card-radius: 1.5rem;
    }
    body {
      color: var(--ink);
    }
    .brand {
      font-weight: 700;
      letter-spacing: 0.3px;
    }
    .btn-accent {
      background: linear-gradient(135deg, var(--indigo), var(--teal));
      color: #fff;
      border: none;
    }
    .btn-accent:hover {
      filter: brightness(0.95);
      color: #fff;
    }
    .link-accent {
      color: var(--indigo);
      text-decoration: none;
    }
    .link-accent:hover {
      color: var(--teal);
    }
    .nav-pill {
      border-radius: 999px;
      padding: 0.4rem 0.9rem;
      background: #eef2f7;
      color: var(--ink);
      text-decoration: none;
      border: none;
    }
    .nav-pill:hover {
      color: var(--ink);
      background: #e3e8ef;
    }
    .badge-soft {
      background: #eef2f7;
      color: var(--ink);
      border-radius: 999px;
      font-weight: 500;
    }
  </style>
</head>
<body class="bg-light">
  <nav class="navbar navbar-expand-lg bg-white shadow-sm">
    <div class="container">
      <a class="navbar-brand brand" href="${pageContext.request.contextPath}/events">Gestion Événements</a>
      <div class="d-flex align-items-center gap-2">
        <a class="nav-pill" href="${pageContext.request.contextPath}/events">Événements</a>
        <c:choose>
          <c:when test="${not empty sessionScope.user}">
            <a class="nav-pill" href="${pageContext.request.contextPath}/mes-billets">Mes billets</a>
            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/logout">
              <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
              <button type="submit" class="nav-pill">Déconnexion</button>
            </form>
          </c:when>
          <c:otherwise>
            <a class="nav-pill" href="${pageContext.request.contextPath}/auth">Connexion</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/auth?action=register">Inscription</a>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </nav>
