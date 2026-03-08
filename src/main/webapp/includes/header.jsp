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
      --ink: #e6edf7;
      --muted: #b2c2d8;
      --accent-1: #2ac7d8;
      --accent-2: #4f8ef7;
      --accent-3: #66e0ef;
      --bg: #071019;
      --bg-2: #0d1828;
      --surface: rgba(12, 21, 34, 0.82);
      --surface-strong: rgba(12, 21, 34, 0.92);
      --border: rgba(79, 142, 247, 0.28);
      --ring: rgba(42, 199, 216, 0.35);
      --card-radius: 1.6rem;
    }
    body,
    .app-shell {
      color: var(--ink);
      font-family: "Sora", system-ui, -apple-system, Segoe UI, sans-serif;
      background-color: var(--bg);
      background-image:
        radial-gradient(circle at 12% 12%, rgba(42, 199, 216, 0.14), transparent 48%),
        radial-gradient(circle at 80% 18%, rgba(79, 142, 247, 0.12), transparent 52%),
        radial-gradient(circle at 20% 85%, rgba(102, 224, 239, 0.1), transparent 45%),
        linear-gradient(180deg, #071019 0%, #0d1828 100%);
      background-attachment: fixed;
      min-height: 100vh;
    }
    .brand {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-weight: 700;
      letter-spacing: 0.5px;
      color: var(--ink);
    }
    .page-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-weight: 700;
      font-size: clamp(2rem, 3vw, 2.8rem);
      margin-bottom: 0.5rem;
    }
    .page-subtitle {
      color: var(--muted);
      max-width: 520px;
    }
    .btn-accent {
      background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
      color: #04121f;
      border: none;
      box-shadow: 0 14px 36px rgba(42, 199, 216, 0.35);
      border-radius: 999px;
      padding: 0.6rem 1.4rem;
      font-weight: 600;
    }
    .btn-accent:hover {
      filter: brightness(0.98);
      color: #04121f;
      box-shadow: 0 18px 44px rgba(79, 142, 247, 0.45);
    }
    .btn-outline-secondary {
      border-color: var(--border);
      color: var(--ink);
      background: rgba(12, 21, 34, 0.65);
      border-radius: 999px;
      padding: 0.55rem 1.2rem;
    }
    .btn-outline-secondary:hover {
      border-color: rgba(79, 142, 247, 0.6);
      background: rgba(12, 21, 34, 0.9);
      color: var(--ink);
      box-shadow: 0 10px 26px rgba(79, 142, 247, 0.3);
    }
    .btn-outline-danger {
      border-color: rgba(248, 113, 113, 0.6);
      color: #ffb9b9;
      background: rgba(52, 15, 22, 0.28);
      border-radius: 999px;
      padding: 0.55rem 1.15rem;
      font-weight: 600;
    }
    .btn-outline-danger:hover {
      border-color: rgba(248, 113, 113, 0.85);
      background: rgba(120, 25, 40, 0.34);
      color: #fff1f1;
      box-shadow: 0 12px 24px rgba(248, 113, 113, 0.28);
    }
    .btn-danger-soft {
      background: rgba(248, 113, 113, 0.15);
      border: 1px solid rgba(248, 113, 113, 0.55);
      color: #ffd6d6;
      border-radius: 999px;
      padding: 0.55rem 1.1rem;
      font-weight: 600;
    }
    .btn-danger-soft:hover {
      background: rgba(248, 113, 113, 0.28);
      border-color: rgba(248, 113, 113, 0.8);
      color: #fff3f3;
      box-shadow: 0 12px 24px rgba(248, 113, 113, 0.35);
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
      background: rgba(12, 21, 34, 0.7);
      color: var(--ink);
      text-decoration: none;
      border: 1px solid var(--border);
      font-weight: 500;
      transition: all 0.2s ease;
    }
    .nav-pill:hover {
      color: var(--ink);
      background: rgba(12, 21, 34, 0.95);
      border-color: rgba(79, 142, 247, 0.6);
      box-shadow: 0 10px 28px rgba(79, 142, 247, 0.25);
    }
    .badge-soft {
      background: rgba(12, 21, 34, 0.82);
      border: 1px solid var(--border);
      color: var(--ink);
      border-radius: 999px;
      font-weight: 500;
      padding: 0.35rem 0.8rem;
    }
    .card {
      border: 0;
      border-radius: var(--card-radius);
      background: var(--surface);
      border: 1px solid var(--border);
      color: var(--ink);
      box-shadow: 0 24px 58px rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(18px);
    }
    .card h1,
    .card h2,
    .card h3,
    .card h4,
    .card h5,
    .card h6,
    .card .h1,
    .card .h2,
    .card .h3,
    .card .h4,
    .card .h5,
    .card .h6 {
      color: var(--ink);
    }
    .card:hover {
      box-shadow:
        0 30px 78px rgba(0, 0, 0, 0.5),
        0 0 24px rgba(79, 142, 247, 0.28);
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
      box-shadow: 0 28px 70px rgba(0, 0, 0, 0.45);
      backdrop-filter: blur(18px);
    }
    .hero-panel.auth-hero {
      background-image:
        linear-gradient(135deg, rgba(7, 16, 25, 0.82), rgba(13, 24, 40, 0.94)),
        url("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=1400&q=80");
      background-size: cover;
      background-position: center;
      border-color: rgba(79, 142, 247, 0.35);
    }
    .hero-panel::after {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at 15% 20%, rgba(42, 199, 216, 0.2), transparent 45%),
        radial-gradient(circle at 85% 80%, rgba(79, 142, 247, 0.16), transparent 40%);
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
    .auth-mini-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0.85rem;
      margin-top: 1.75rem;
    }
    .auth-mini-card {
      padding: 0.95rem 1rem;
      border-radius: 1.1rem;
      background: rgba(8, 16, 27, 0.66);
      border: 1px solid rgba(79, 142, 247, 0.18);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .auth-mini-label {
      color: var(--muted);
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      margin-bottom: 0.3rem;
    }
    .auth-mini-value {
      color: #f4f9ff;
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.02rem;
      font-weight: 700;
    }
    .auth-form-panel {
      position: relative;
      overflow: hidden;
      background:
        linear-gradient(165deg, rgba(7, 19, 34, 0.98), rgba(9, 24, 43, 0.93) 58%, rgba(6, 14, 26, 1));
      border: 1px solid rgba(64, 146, 255, 0.22);
    }
    .auth-form-panel::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top right, rgba(42, 199, 216, 0.14), transparent 34%),
        radial-gradient(circle at bottom left, rgba(79, 142, 247, 0.1), transparent 38%);
      pointer-events: none;
    }
    .auth-form-panel > * {
      position: relative;
      z-index: 1;
    }
    .auth-form-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.8rem;
      margin-bottom: 0.25rem;
      color: #f5fbff;
    }
    .auth-form-subtitle {
      color: #b7c7db;
      margin-bottom: 0;
    }
    .auth-form-divider {
      height: 1px;
      background: linear-gradient(90deg, rgba(79, 142, 247, 0), rgba(79, 142, 247, 0.34), rgba(79, 142, 247, 0));
      margin: 1.5rem 0;
    }
    .auth-footer-note {
      color: #95a9c2;
      font-size: 0.9rem;
    }
    .auth-form-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.9rem;
      margin-top: 1.5rem;
    }
    .auth-support-card {
      padding: 1rem 1.05rem;
      border-radius: 1.15rem;
      background: rgba(8, 16, 27, 0.72);
      border: 1px solid rgba(79, 142, 247, 0.16);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .auth-support-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1rem;
      margin-bottom: 0.3rem;
      color: #f3f9ff;
    }
    .auth-support-text {
      color: #a8bbd4;
      font-size: 0.9rem;
      line-height: 1.55;
      margin: 0;
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
      color: var(--ink);
      box-shadow: 0 20px 54px rgba(0, 0, 0, 0.5);
      backdrop-filter: blur(18px);
    }
    .section-title {
      border-left: 3px solid var(--accent-1);
      padding-left: 1rem;
      margin-bottom: 1rem;
    }
    .stat-card {
      padding: 1.4rem 1.6rem;
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      min-height: 120px;
    }
    .stat-label {
      color: var(--muted);
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }
    .stat-value {
      font-size: 1.8rem;
      font-weight: 700;
    }
    .filter-panel {
      padding: 1.6rem;
      position: sticky;
      top: 1.5rem;
    }
    .filter-title {
      font-weight: 600;
      font-size: 1.05rem;
      margin-bottom: 1rem;
    }
    .filter-group {
      margin-bottom: 1.4rem;
    }
    .filter-panel .form-check-input {
      background-color: rgba(12, 21, 34, 0.8);
      border-color: var(--border);
    }
    .filter-panel .form-check-input:checked {
      background-color: var(--accent-1);
      border-color: var(--accent-1);
      box-shadow: 0 0 0 0.2rem rgba(42, 199, 216, 0.25);
    }
    .filter-panel .form-check-label {
      color: var(--ink);
    }
    .event-card .card-body {
      padding: 1.6rem;
      position: relative;
      z-index: 1;
    }
    .event-card .card-footer {
      background:
        linear-gradient(180deg, rgba(10, 18, 29, 0.2), rgba(17, 32, 52, 0.82));
      border-top: 1px solid rgba(102, 224, 239, 0.18);
      padding: 1.2rem 1.6rem;
      color: var(--ink);
    }
    .event-card {
      overflow: hidden;
      position: relative;
      min-height: 100%;
      background:
        linear-gradient(160deg, rgba(7, 19, 34, 0.96), rgba(9, 24, 43, 0.9) 62%, rgba(6, 14, 26, 0.98));
      border: 1px solid rgba(64, 146, 255, 0.24);
      box-shadow:
        0 18px 42px rgba(0, 0, 0, 0.38),
        inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .event-card::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top right, rgba(42, 199, 216, 0.16), transparent 34%),
        radial-gradient(circle at bottom left, rgba(79, 142, 247, 0.12), transparent 36%);
      opacity: 0.95;
      pointer-events: none;
    }
    .event-card::after {
      content: "";
      position: absolute;
      left: 1.25rem;
      right: 1.25rem;
      top: 0;
      height: 2px;
      background: linear-gradient(90deg, rgba(42, 199, 216, 0), rgba(42, 199, 216, 0.85), rgba(79, 142, 247, 0.75), rgba(79, 142, 247, 0));
      opacity: 0.85;
    }
    .event-card:hover {
      transform: translateY(-5px) scale(1.005);
      border-color: rgba(102, 224, 239, 0.34);
      box-shadow:
        0 28px 60px rgba(0, 0, 0, 0.5),
        0 0 30px rgba(42, 199, 216, 0.16);
    }
    .event-card .badge-soft {
      background: rgba(7, 20, 35, 0.76);
      border-color: rgba(102, 224, 239, 0.26);
      color: #ecf6ff;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
    }
    .event-card .h5,
    .event-card h2,
    .event-card .fw-semibold {
      color: var(--ink);
    }
    .event-card-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: clamp(1.4rem, 2vw, 1.9rem);
      line-height: 1.08;
      letter-spacing: -0.02em;
      margin-bottom: 0.7rem;
      text-wrap: balance;
    }
    .event-meta {
      color: #d6e6fb;
      font-size: 0.9rem;
      margin-bottom: 0.95rem;
      font-weight: 500;
      text-shadow: 0 0 14px rgba(42, 199, 216, 0.06);
    }
    .event-description {
      color: #bbcee4;
      font-size: 0.98rem;
      line-height: 1.65;
      min-height: 4.6rem;
      margin-bottom: 1rem;
    }
    .event-availability {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.48rem 0.7rem;
      border-radius: 999px;
      background: rgba(10, 25, 43, 0.7);
      border: 1px solid rgba(79, 142, 247, 0.18);
      color: #d8e8fb;
      font-size: 0.9rem;
    }
    .event-availability::before {
      content: "";
      width: 0.52rem;
      height: 0.52rem;
      border-radius: 999px;
      background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
      box-shadow: 0 0 12px rgba(42, 199, 216, 0.55);
    }
    .event-price {
      display: inline-flex;
      align-items: baseline;
      gap: 0.35rem;
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 2rem;
      font-weight: 700;
      color: #f7fbff;
      letter-spacing: -0.03em;
      text-shadow: 0 0 18px rgba(79, 142, 247, 0.15);
    }
    .event-price span {
      color: var(--accent-3);
      font-size: 0.95rem;
      font-weight: 600;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }
    .event-footer-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.8rem;
    }
    .ticket-card {
      overflow: hidden;
      position: relative;
      background:
        linear-gradient(165deg, rgba(7, 19, 34, 0.98), rgba(9, 24, 43, 0.93) 58%, rgba(6, 14, 26, 1));
      border: 1px solid rgba(64, 146, 255, 0.2);
    }
    .ticket-card::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top right, rgba(42, 199, 216, 0.15), transparent 34%),
        radial-gradient(circle at bottom left, rgba(79, 142, 247, 0.12), transparent 36%);
      pointer-events: none;
    }
    .ticket-card .card-body {
      position: relative;
      z-index: 1;
      padding: 1.7rem;
    }
    .ticket-card-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: clamp(1.35rem, 1.8vw, 1.8rem);
      line-height: 1.1;
      margin-bottom: 0.35rem;
      color: #f5fbff;
    }
    .ticket-card-meta {
      color: #d1e0f2;
      font-size: 0.92rem;
      font-weight: 500;
      margin-bottom: 1rem;
    }
    .ticket-info-stack {
      display: grid;
      gap: 0.55rem;
      margin-bottom: 1.2rem;
    }
    .ticket-detail-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.8rem;
      margin-bottom: 1.2rem;
    }
    .ticket-info-label {
      color: var(--muted);
      font-size: 0.78rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      margin-bottom: 0.15rem;
    }
    .ticket-info-value {
      color: #eef6ff;
      font-size: 1rem;
      font-weight: 600;
      word-break: break-word;
    }
    .ticket-code-box {
      padding: 0.85rem 1rem;
      border-radius: 1rem;
      background: rgba(9, 18, 29, 0.88);
      border: 1px solid rgba(79, 142, 247, 0.18);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
      min-height: 100%;
    }
    .ticket-price {
      display: inline-flex;
      align-items: baseline;
      gap: 0.35rem;
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.85rem;
      font-weight: 700;
      color: #f7fbff;
      letter-spacing: -0.03em;
    }
    .ticket-price span {
      color: var(--accent-3);
      font-size: 0.92rem;
      font-weight: 600;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }
    .ticket-summary-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1rem;
      padding: 0.95rem 1rem;
      border-radius: 1.15rem;
      background: rgba(8, 16, 27, 0.68);
      border: 1px solid rgba(79, 142, 247, 0.14);
    }
    .ticket-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 0.7rem;
      justify-content: flex-end;
    }
    .ticket-transfer-panel {
      margin-top: 0;
      padding: 1rem;
      border-radius: 1.15rem;
      background: rgba(8, 16, 27, 0.68);
      border: 1px solid rgba(79, 142, 247, 0.14);
    }
    .ticket-transfer-note {
      color: #8fa6c4;
      font-size: 0.84rem;
      margin-bottom: 0.65rem;
    }
    .ticket-transfer-row {
      display: flex;
      align-items: center;
      gap: 0.7rem;
    }
    .ticket-qr-panel {
      margin-top: 1.25rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.8rem;
      padding: 1rem;
      border-radius: 1.15rem;
      background: rgba(8, 16, 27, 0.78);
      border: 1px solid rgba(79, 142, 247, 0.16);
      text-align: center;
    }
    .ticket-qr-panel img {
      width: 150px;
      height: 150px;
      border-radius: 14px;
      border: 1px solid rgba(148,163,184,0.22);
      background: #ffffff;
      padding: 0.5rem;
      box-shadow: 0 14px 30px rgba(0, 0, 0, 0.28);
    }
    .ticket-qr-caption {
      color: #9fb4cd;
      font-size: 0.84rem;
      max-width: 240px;
      line-height: 1.45;
    }
    .metric-card {
      overflow: hidden;
      position: relative;
      min-height: 130px;
      background:
        linear-gradient(160deg, rgba(8, 20, 34, 0.98), rgba(9, 24, 43, 0.92) 62%, rgba(6, 14, 26, 1));
      border: 1px solid rgba(79, 142, 247, 0.2);
      box-shadow:
        0 20px 42px rgba(0, 0, 0, 0.34),
        inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .metric-card::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top right, rgba(42, 199, 216, 0.14), transparent 35%),
        radial-gradient(circle at bottom left, rgba(79, 142, 247, 0.1), transparent 38%);
      pointer-events: none;
    }
    .metric-card .stat-card,
    .metric-card.stat-card {
      position: relative;
      z-index: 1;
    }
    .section-surface {
      overflow: hidden;
      position: relative;
      background:
        linear-gradient(160deg, rgba(8, 20, 34, 0.98), rgba(9, 24, 43, 0.92) 62%, rgba(6, 14, 26, 1));
      border: 1px solid rgba(79, 142, 247, 0.2);
      box-shadow:
        0 24px 52px rgba(0, 0, 0, 0.38),
        inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .section-surface::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top right, rgba(42, 199, 216, 0.12), transparent 34%),
        radial-gradient(circle at bottom left, rgba(79, 142, 247, 0.1), transparent 36%);
      pointer-events: none;
    }
    .section-surface > .card-body,
    .section-surface > * {
      position: relative;
      z-index: 1;
    }
    .panel-toolbar {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.2rem;
    }
    .panel-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.35rem;
      margin-bottom: 0.2rem;
      color: #f4f9ff;
    }
    .panel-subtitle {
      color: #a7bad1;
      font-size: 0.94rem;
    }
    .count-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.35rem;
      padding: 0.45rem 0.9rem;
      border-radius: 999px;
      background: rgba(7, 20, 35, 0.76);
      border: 1px solid rgba(102, 224, 239, 0.2);
      color: #eef6ff;
      font-weight: 600;
      white-space: nowrap;
    }
    .status-pill {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.38rem 0.85rem;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 700;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      border: 1px solid transparent;
      white-space: nowrap;
    }
    .status-pill::before {
      content: "";
      width: 0.48rem;
      height: 0.48rem;
      border-radius: 999px;
      background: currentColor;
      box-shadow: 0 0 10px currentColor;
      opacity: 0.85;
    }
    .status-reserve {
      background: rgba(245, 158, 11, 0.14);
      border-color: rgba(245, 158, 11, 0.4);
      color: #ffd08a;
    }
    .status-paye {
      background: rgba(16, 185, 129, 0.14);
      border-color: rgba(16, 185, 129, 0.38);
      color: #bdf7db;
    }
    .status-annule {
      background: rgba(248, 113, 113, 0.12);
      border-color: rgba(248, 113, 113, 0.38);
      color: #ffc7c7;
    }
    .status-transfere {
      background: rgba(79, 142, 247, 0.14);
      border-color: rgba(79, 142, 247, 0.38);
      color: #cce0ff;
    }
    .table-actions {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      flex-wrap: wrap;
      gap: 0.55rem;
    }
    .value-chip {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.38rem 0.75rem;
      border-radius: 999px;
      background: rgba(10, 25, 43, 0.7);
      border: 1px solid rgba(79, 142, 247, 0.18);
      color: #d8e8fb;
      font-size: 0.88rem;
      font-weight: 600;
    }
    .value-chip::before {
      content: "";
      width: 0.5rem;
      height: 0.5rem;
      border-radius: 999px;
      background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
      box-shadow: 0 0 10px rgba(42, 199, 216, 0.45);
    }
    .empty-panel {
      padding: 2rem;
      text-align: center;
      color: var(--muted);
    }
    .payment-summary-card {
      overflow: hidden;
      position: relative;
      background:
        linear-gradient(165deg, rgba(7, 19, 34, 0.98), rgba(9, 24, 43, 0.93) 58%, rgba(6, 14, 26, 1));
      border: 1px solid rgba(64, 146, 255, 0.22);
    }
    .payment-summary-card::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top right, rgba(42, 199, 216, 0.18), transparent 34%),
        radial-gradient(circle at bottom left, rgba(79, 142, 247, 0.12), transparent 38%);
      pointer-events: none;
    }
    .payment-summary-card .card-body {
      position: relative;
      z-index: 1;
      padding: 1.7rem;
    }
    .payment-ticket-top {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .payment-ticket-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: clamp(1.45rem, 2vw, 1.95rem);
      line-height: 1.08;
      margin-bottom: 0.35rem;
      color: #f5fbff;
    }
    .payment-ticket-meta {
      color: #d1e0f2;
      font-size: 0.95rem;
      font-weight: 500;
    }
    .payment-ticket-grid {
      display: grid;
      gap: 0.9rem;
      grid-template-columns: minmax(0, 1.3fr) minmax(0, 0.9fr);
      align-items: end;
    }
    .payment-code-panel {
      padding: 0.95rem 1rem;
      border-radius: 1rem;
      background: rgba(9, 18, 29, 0.88);
      border: 1px solid rgba(79, 142, 247, 0.18);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .payment-code-value {
      color: #eef6ff;
      font-size: 1.1rem;
      font-weight: 700;
      word-break: break-word;
    }
    .payment-price-wrap {
      display: flex;
      align-items: end;
      justify-content: flex-end;
      height: 100%;
    }
    .payment-price {
      display: inline-flex;
      align-items: baseline;
      gap: 0.35rem;
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 2rem;
      font-weight: 700;
      color: #f7fbff;
      letter-spacing: -0.03em;
      text-shadow: 0 0 18px rgba(79, 142, 247, 0.15);
    }
    .payment-price span {
      color: var(--accent-3);
      font-size: 0.95rem;
      font-weight: 600;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }
    .payment-summary-meta-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0.8rem;
      margin-top: 1rem;
    }
    .payment-meta-card {
      padding: 0.9rem 1rem;
      border-radius: 1rem;
      background: rgba(8, 16, 27, 0.72);
      border: 1px solid rgba(79, 142, 247, 0.16);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .payment-form-shell {
      position: relative;
      overflow: hidden;
      background:
        linear-gradient(165deg, rgba(7, 19, 34, 0.98), rgba(9, 24, 43, 0.93) 58%, rgba(6, 14, 26, 1));
      border: 1px solid rgba(64, 146, 255, 0.22);
    }
    .payment-form-shell::before {
      content: "";
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at top left, rgba(42, 199, 216, 0.14), transparent 28%),
        radial-gradient(circle at bottom right, rgba(79, 142, 247, 0.12), transparent 34%);
      pointer-events: none;
    }
    .payment-form-shell .card-body {
      position: relative;
      z-index: 1;
      padding: 1.7rem;
    }
    .payment-section-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.55rem;
      margin-bottom: 1rem;
    }
    .payment-note-strip {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.9rem 1rem;
      border-radius: 1.1rem;
      background: rgba(8, 16, 27, 0.72);
      border: 1px solid rgba(79, 142, 247, 0.14);
      color: #c9d9ec;
      margin-bottom: 1.35rem;
    }
    .payment-note-strip strong {
      color: #f4f9ff;
      font-weight: 600;
    }
    .payment-method-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .payment-method-card {
      position: relative;
      display: block;
      min-height: 100%;
      padding: 1.2rem;
      border-radius: 1.5rem;
      background: rgba(7, 18, 31, 0.82);
      border: 1px solid rgba(79, 142, 247, 0.18);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
      transition: all 0.2s ease;
      cursor: pointer;
    }
    .payment-method-card.method-card-banque {
      background:
        linear-gradient(180deg, rgba(8, 23, 40, 0.9), rgba(6, 16, 28, 0.94));
    }
    .payment-method-card.method-card-paypal {
      background:
        linear-gradient(180deg, rgba(9, 20, 37, 0.9), rgba(8, 18, 31, 0.94));
    }
    .payment-method-card.method-card-stripe {
      background:
        linear-gradient(180deg, rgba(10, 22, 39, 0.9), rgba(8, 18, 31, 0.94));
    }
    .payment-method-card:hover {
      border-color: rgba(102, 224, 239, 0.34);
      box-shadow: 0 18px 34px rgba(0, 0, 0, 0.28), 0 0 20px rgba(42, 199, 216, 0.08);
      transform: translateY(-2px);
    }
    .payment-method-card.is-selected {
      border-color: rgba(42, 199, 216, 0.88);
      background:
        linear-gradient(165deg, rgba(9, 23, 37, 0.94), rgba(8, 22, 38, 0.96));
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.34), 0 0 28px rgba(42, 199, 216, 0.14);
    }
    .payment-method-card input[type="radio"] {
      position: absolute;
      inset: 0;
      opacity: 0;
      pointer-events: none;
    }
    .payment-method-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.9rem;
      gap: 0.75rem;
    }
    .payment-method-kicker {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 2rem;
      height: 2rem;
      border-radius: 999px;
      background: rgba(79, 142, 247, 0.14);
      border: 1px solid rgba(79, 142, 247, 0.18);
      color: var(--accent-3);
      font-size: 0.8rem;
      font-weight: 700;
      letter-spacing: 0.08em;
    }
    .payment-method-check {
      width: 0.95rem;
      height: 0.95rem;
      border-radius: 999px;
      border: 2px solid rgba(230, 237, 247, 0.75);
      box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.02);
    }
    .payment-method-card.is-selected .payment-method-check {
      border-color: var(--accent-1);
      background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
      box-shadow: 0 0 0 4px rgba(42, 199, 216, 0.14);
    }
    .payment-method-title {
      display: block;
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.2rem;
      color: #eef6ff;
      margin-bottom: 0.45rem;
    }
    .payment-method-text {
      display: block;
      color: #bbcee4;
      font-size: 0.98rem;
      line-height: 1.5;
    }
    .payment-detail-panel {
      background: rgba(7, 18, 31, 0.84);
      border: 1px solid rgba(79, 142, 247, 0.18);
      border-radius: 1.5rem;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
    }
    .payment-detail-panel .card-body {
      padding: 1.35rem 1.4rem;
    }
    .payment-detail-title {
      font-family: "Space Grotesk", "Sora", sans-serif;
      font-size: 1.2rem;
      margin-bottom: 1rem;
    }
    .payment-submit {
      padding-top: 0.9rem;
      padding-bottom: 0.9rem;
      font-size: 1.05rem;
      letter-spacing: 0.01em;
    }
    @media (max-width: 575.98px) {
      .ticket-detail-grid {
        grid-template-columns: 1fr;
      }
      .ticket-summary-row {
        flex-direction: column;
        align-items: flex-start;
      }
      .ticket-actions {
        justify-content: flex-start;
      }
      .ticket-transfer-row {
        flex-direction: column;
        align-items: stretch;
      }
    }
    @media (max-width: 991.98px) {
      .auth-mini-grid {
        grid-template-columns: 1fr;
      }
      .auth-form-grid {
        grid-template-columns: 1fr;
      }
      .payment-method-grid {
        grid-template-columns: 1fr;
      }
      .payment-ticket-grid {
        grid-template-columns: 1fr;
      }
      .payment-summary-meta-grid {
        grid-template-columns: 1fr;
      }
      .payment-price-wrap {
        justify-content: flex-start;
      }
    }
    @media (max-width: 767.98px) {
      .panel-toolbar {
        flex-direction: column;
        align-items: flex-start;
      }
      .table-actions {
        justify-content: flex-start;
      }
    }
    .divider {
      border-color: rgba(79, 142, 247, 0.18);
      opacity: 1;
    }
    .table {
      --bs-table-bg: transparent;
      --bs-table-color: var(--ink);
      --bs-table-border-color: rgba(79, 142, 247, 0.18);
      --bs-table-striped-bg: rgba(13, 24, 40, 0.72);
      --bs-table-striped-color: var(--ink);
      --bs-table-active-bg: rgba(23, 39, 61, 0.86);
      --bs-table-active-color: var(--ink);
      --bs-table-hover-bg: rgba(26, 44, 69, 0.82);
      --bs-table-hover-color: #f3f8ff;
      border-color: rgba(79, 142, 247, 0.18);
      color: var(--ink);
    }
    .table-responsive {
      border-radius: calc(var(--card-radius) - 0.15rem);
      overflow: hidden;
    }
    .table thead th {
      color: var(--muted);
      font-weight: 600;
      background: rgba(12, 21, 34, 0.9);
      border-color: rgba(79, 142, 247, 0.18);
    }
    .table tbody td,
    .table tbody th {
      background: rgba(9, 17, 28, 0.82);
      color: var(--ink);
      border-color: rgba(79, 142, 247, 0.12);
    }
    .table tbody tr:nth-child(even) td,
    .table tbody tr:nth-child(even) th {
      background: rgba(13, 24, 40, 0.88);
    }
    .table tbody tr {
      border-color: rgba(79, 142, 247, 0.12);
    }
    .table tbody tr:hover td,
    .table tbody tr:hover th {
      background: rgba(21, 35, 55, 0.95);
      color: #f7fbff;
    }
    .table-light,
    .table-light > tr > th,
    .table-light > tr > td {
      --bs-table-bg: rgba(12, 21, 34, 0.92);
      --bs-table-color: var(--muted);
      color: var(--muted);
      background: rgba(12, 21, 34, 0.92) !important;
      border-color: rgba(79, 142, 247, 0.18) !important;
    }
    .form-control,
    .form-select {
      border-radius: 0.85rem;
      border-color: rgba(79, 142, 247, 0.28);
      background: rgba(8, 16, 27, 0.88);
      color: var(--ink);
      box-shadow: none;
      color-scheme: dark;
    }
    .form-control::placeholder {
      color: rgba(140, 160, 185, 0.65);
    }
    .form-control:focus,
    .form-select:focus {
      border-color: var(--accent-1);
      box-shadow: 0 0 0 0.2rem var(--ring);
    }
    .form-label {
      color: #dbe7f7 !important;
      font-weight: 600;
    }
    .form-select option {
      background: #0f1c2e;
      color: var(--ink);
    }
    .form-select option:checked {
      background: #1b64c9;
      color: #ffffff;
    }
    .navbar-glass {
      background: rgba(8, 16, 27, 0.8);
      backdrop-filter: blur(18px);
      border-bottom: 1px solid var(--border);
    }
    .shadow-sm {
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.55) !important;
    }
    .text-secondary {
      color: var(--muted) !important;
    }
    .alert-warning {
      background: rgba(245, 158, 11, 0.2);
      border: 1px solid rgba(245, 158, 11, 0.4) !important;
      color: #ffe8bd;
    }
    .alert-success {
      background: rgba(16, 185, 129, 0.18);
      border: 1px solid rgba(16, 185, 129, 0.4) !important;
      color: #d7ffee;
    }
  </style>
</head>
<body class="app-shell">
  <c:set var="homePath" value="${pageContext.request.contextPath}/auth" />
  <c:if test="${not empty sessionScope.user}">
    <c:choose>
      <c:when test="${sessionScope.role == 'ADMIN'}">
        <c:set var="homePath" value="${pageContext.request.contextPath}/admin/events" />
      </c:when>
      <c:when test="${sessionScope.role == 'ORGANISATEUR'}">
        <c:set var="homePath" value="${pageContext.request.contextPath}/organisateur/events" />
      </c:when>
      <c:otherwise>
        <c:set var="homePath" value="${pageContext.request.contextPath}/events" />
      </c:otherwise>
    </c:choose>
  </c:if>
  <nav class="navbar navbar-expand-lg navbar-glass shadow-sm">
    <div class="container">
      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <a class="navbar-brand brand" href="${homePath}">Gestion Evenements</a>
        </c:when>
        <c:otherwise>
          <a class="navbar-brand brand" href="${pageContext.request.contextPath}/auth">Gestion Evenements</a>
        </c:otherwise>
      </c:choose>
      <div class="d-flex align-items-center gap-2">
        <c:if test="${not empty sessionScope.user}">
          <c:if test="${sessionScope.role == 'PARTICIPANT'}">
            <a class="nav-pill" href="${pageContext.request.contextPath}/events">Evenements</a>
          </c:if>
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

