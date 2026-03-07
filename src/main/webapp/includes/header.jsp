<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@500;600;700&family=Sora:wght@400;500;600&display=swap" rel="stylesheet">
  <title>${not empty pageTitle ? pageTitle : (empty param.pageTitle ? "Gestion Evenements" : param.pageTitle)}</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    :root {
      --ink: #eef2ff;
      --muted: #9aa4b2;
      --accent-1: #7c3aed;
      --accent-2: #38bdf8;
      --accent-3: #10b981;
      --bg: #0b0f1a;
      --bg-2: #111827;
      --surface: rgba(15, 23, 42, 0.7);
      --surface-strong: rgba(15, 23, 42, 0.88);
      --border: rgba(148, 163, 184, 0.2);
      --ring: rgba(124, 58, 237, 0.28);
      --card-radius: 1.35rem;
    }
    body,
    .app-shell {
      color: var(--ink);
      font-family: "Sora", system-ui, -apple-system, Segoe UI, sans-serif;
      background-color: var(--bg);
      background-image:
        radial-gradient(circle at top left, rgba(124, 58, 237, 0.2), transparent 45%),
        radial-gradient(circle at 75% 20%, rgba(56, 189, 248, 0.18), transparent 40%),
        radial-gradient(circle at 25% 80%, rgba(16, 185, 129, 0.12), transparent 40%),
        linear-gradient(180deg, #0b0f1a 0%, #0f172a 100%);
      background-attachment: fixed;
      min-height: 100vh;
      animation: pageFade 0.4s ease-out;
    }
    .brand {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-weight: 700;
      letter-spacing: 0.4px;
      color: var(--ink);
    }
    .btn-accent {
      background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
      color: #fff;
      border: none;
      box-shadow: 0 12px 30px rgba(124, 58, 237, 0.3);
    }
    .btn-accent:hover {
      filter: brightness(0.95);
      color: #fff;
      box-shadow: 0 16px 40px rgba(124, 58, 237, 0.45);
    }
    .btn-outline-secondary {
      border-color: var(--border);
      color: var(--ink);
      background: rgba(15, 23, 42, 0.4);
    }
    .btn-outline-secondary:hover {
      border-color: rgba(124, 58, 237, 0.5);
      background: rgba(15, 23, 42, 0.6);
      color: var(--ink);
      box-shadow: 0 12px 30px rgba(56, 189, 248, 0.25);
    }
    .link-accent {
      color: var(--accent-2);
      text-decoration: none;
    }
    .link-accent:hover {
      color: var(--accent-1);
    }
    .nav-pill {
      border-radius: 999px;
      padding: 0.45rem 0.95rem;
      background: rgba(15, 23, 42, 0.55);
      color: var(--ink);
      text-decoration: none;
      border: 1px solid var(--border);
      font-weight: 500;
      transition: all 0.2s ease;
    }
    .nav-pill:hover {
      color: var(--ink);
      background: rgba(15, 23, 42, 0.75);
      border-color: rgba(124, 58, 237, 0.5);
      box-shadow: 0 10px 28px rgba(56, 189, 248, 0.2);
    }
    .badge-soft {
      background: rgba(15, 23, 42, 0.65);
      border: 1px solid var(--border);
      color: var(--ink);
      border-radius: 999px;
      font-weight: 500;
    }
    .card {
      border: 0;
      border-radius: var(--card-radius);
      background: var(--surface);
      border: 1px solid var(--border);
      box-shadow: 0 20px 60px rgba(5, 8, 20, 0.45);
      backdrop-filter: blur(18px);
      animation: floatIn 0.45s ease-out;
    }
    .card:hover {
      box-shadow:
        0 24px 80px rgba(5, 8, 20, 0.6),
        0 0 30px rgba(124, 58, 237, 0.25);
      transform: translateY(-2px);
      transition: all 0.2s ease;
    }
    .hero-panel {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: calc(var(--card-radius) + 0.25rem);
      padding: 2.5rem;
      position: relative;
      overflow: hidden;
      box-shadow: 0 24px 60px rgba(5, 8, 20, 0.5);
      backdrop-filter: blur(18px);
    }
    .hero-panel.auth-hero {
      background-image:
        linear-gradient(135deg, rgba(7, 10, 22, 0.78), rgba(7, 10, 22, 0.92)),
        url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 800'><defs><linearGradient id='g1' x1='0' y1='0' x2='1' y2='1'><stop offset='0' stop-color='%237c3aed' stop-opacity='0.85'/><stop offset='1' stop-color='%2338bdf8' stop-opacity='0.6'/></linearGradient></defs><rect width='1200' height='800' fill='%230b0f1a'/><g fill='none' stroke='url(%23g1)' stroke-width='2' opacity='0.75'><path d='M-50 200 C 150 100, 350 300, 550 200 S 950 250, 1250 140' /><path d='M-50 320 C 200 220, 400 420, 650 300 S 1000 360, 1300 250' /><path d='M-50 440 C 200 340, 420 520, 700 420 S 1020 470, 1350 380' /></g><g fill='%2310b981' opacity='0.25'><circle cx='900' cy='180' r='90'/></g><g fill='%237c3aed' opacity='0.2'><circle cx='280' cy='560' r='120'/></g></svg>");
      background-size: cover;
      background-position: center;
      border-color: rgba(124, 58, 237, 0.3);
    }
    .hero-panel::after {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at 15% 20%, rgba(124, 58, 237, 0.2), transparent 45%),
        radial-gradient(circle at 85% 80%, rgba(56, 189, 248, 0.18), transparent 40%);
      opacity: 0.7;
      pointer-events: none;
    }
    .hero-panel > * {
      position: relative;
      z-index: 1;
    }
    .hero-kicker {
      text-transform: uppercase;
      letter-spacing: 0.16em;
      font-size: 0.75rem;
      color: var(--muted);
      font-weight: 600;
      margin-bottom: 0.75rem;
    }
    .hero-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-weight: 700;
      font-size: clamp(1.8rem, 2.6vw, 2.6rem);
      margin-bottom: 1rem;
    }
    .hero-list {
      list-style: none;
      padding: 0;
      margin: 1.5rem 0 0;
      display: grid;
      gap: 0.9rem;
    }
    .hero-list li {
      display: flex;
      gap: 0.65rem;
      align-items: flex-start;
      color: var(--muted);
      font-size: 0.95rem;
    }
    .hero-dot {
      width: 0.6rem;
      height: 0.6rem;
      border-radius: 999px;
      background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
      margin-top: 0.35rem;
      flex: 0 0 auto;
    }
    .clean-section {
      background: var(--surface-strong);
      border-radius: var(--card-radius);
      border: 1px solid var(--border);
      box-shadow: 0 18px 50px rgba(5, 8, 20, 0.5);
      backdrop-filter: blur(18px);
    }
    .table {
      border-color: rgba(148, 163, 184, 0.18);
      color: var(--ink);
    }
    .table thead th {
      color: var(--muted);
      font-weight: 600;
      background: rgba(15, 23, 42, 0.75);
    }
    .form-control,
    .form-select {
      border-radius: 0.85rem;
      border-color: rgba(148, 163, 184, 0.25);
      background: rgba(7, 10, 22, 0.75);
      color: var(--ink);
      box-shadow: none;
    }
    .form-control::placeholder {
      color: rgba(154, 164, 178, 0.7);
    }
    .form-control:focus,
    .form-select:focus {
      border-color: var(--accent-1);
      box-shadow: 0 0 0 0.2rem var(--ring);
    }
    .navbar-glass {
      background: rgba(7, 10, 22, 0.7);
      backdrop-filter: blur(18px);
      border-bottom: 1px solid var(--border);
    }
    .shadow-sm {
      box-shadow: 0 12px 30px rgba(5, 8, 20, 0.5) !important;
    }
    .text-secondary {
      color: var(--muted) !important;
    }
    @keyframes pageFade {
      from { opacity: 0; transform: translateY(6px); }
      to { opacity: 1; transform: translateY(0); }
    }
    @keyframes floatIn {
      from { opacity: 0; transform: translateY(12px); }
      to { opacity: 1; transform: translateY(0); }
    }
  </style>
</head>
<body class="app-shell">
  <nav class="navbar navbar-expand-lg navbar-glass shadow-sm">
    <div class="container">
      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <a class="navbar-brand brand" href="${pageContext.request.contextPath}/events">Gestion Evenements</a>
        </c:when>
        <c:otherwise>
          <a class="navbar-brand brand" href="${pageContext.request.contextPath}/auth">Gestion Evenements</a>
        </c:otherwise>
      </c:choose>
      <div class="d-flex align-items-center gap-2">
        <c:if test="${not empty sessionScope.user}">
          <a class="nav-pill" href="${pageContext.request.contextPath}/events">Evenements</a>
          <c:if test="${sessionScope.role == 'ORGANISATEUR'}">
            <a class="nav-pill" href="${pageContext.request.contextPath}/organisateur/events">Mes evenements</a>
          </c:if>
          <c:if test="${sessionScope.role == 'ADMIN'}">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/events">Validation</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/users">Utilisateurs</a>
          </c:if>
        </c:if>
        <c:choose>
          <c:when test="${not empty sessionScope.user}">
            <c:if test="${sessionScope.role == 'PARTICIPANT'}">
              <a class="nav-pill" href="${pageContext.request.contextPath}/mes-billets">Mes billets</a>
            </c:if>
            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/logout">
              <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
              <button type="submit" class="nav-pill">Deconnexion</button>
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

