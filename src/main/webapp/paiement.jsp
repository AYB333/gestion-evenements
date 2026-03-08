<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Paiement | Gestion Evenements" />
<c:set var="paymentMethod" value="${empty selectedMethod ? 'CARTE' : selectedMethod}" />
<jsp:include page="/includes/header.jsp" />

<main class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-xl-10">
      <div class="clean-section p-4 p-lg-5">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
          <div>
            <div class="hero-kicker">Paiement</div>
            <div class="page-title mb-1" style="font-size: clamp(1.8rem, 2.3vw, 2.3rem);">Paiement du billet</div>
            <div class="page-subtitle">Finalisez votre reservation en quelques secondes.</div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <c:if test="${not empty ticket}">
              <span class="status-pill
                <c:choose>
                  <c:when test="${ticket.statut == 'PAYE'}">status-paye</c:when>
                  <c:when test="${ticket.statut == 'RESERVE'}">status-reserve</c:when>
                  <c:when test="${ticket.statut == 'ANNULE'}">status-annule</c:when>
                  <c:otherwise>status-transfere</c:otherwise>
                </c:choose>">
                <c:out value="${ticket.statut}" />
              </span>
            </c:if>
            <a class="nav-pill" href="${pageContext.request.contextPath}/mes-billets">Retour</a>
          </div>
        </div>

        <c:if test="${not empty error}">
          <div class="alert alert-warning border-0"><c:out value="${error}" /></div>
        </c:if>

        <c:choose>
          <c:when test="${empty ticket}">
            <div class="text-secondary">Ticket introuvable.</div>
          </c:when>
          <c:otherwise>
            <div class="payment-note-strip">
              <span><strong>Etape rapide:</strong> choisissez une methode puis completez uniquement les champs affiches.</span>
              <span class="count-badge">Ticket #<c:out value="${ticket.id}" /></span>
            </div>

            <div class="card payment-summary-card mb-4">
              <div class="card-body">
                <div class="payment-ticket-top">
                  <div>
                    <div class="payment-ticket-meta"><c:out value="${ticket.event.lieu}" /> - <c:out value="${eventDateDisplay}" /></div>
                    <h2 class="payment-ticket-title"><c:out value="${ticket.event.titre}" /></h2>
                  </div>
                  <span class="status-pill
                    <c:choose>
                      <c:when test="${ticket.statut == 'PAYE'}">status-paye</c:when>
                      <c:when test="${ticket.statut == 'RESERVE'}">status-reserve</c:when>
                      <c:when test="${ticket.statut == 'ANNULE'}">status-annule</c:when>
                      <c:otherwise>status-transfere</c:otherwise>
                    </c:choose>">
                    <c:out value="${ticket.statut}" />
                  </span>
                </div>
                <div class="payment-ticket-grid">
                  <div class="payment-code-panel">
                    <div class="ticket-info-label">Code du billet</div>
                    <div class="payment-code-value"><c:out value="${ticket.code}" /></div>
                  </div>
                  <div class="payment-price-wrap">
                    <div class="payment-price"><c:out value="${ticket.prix}" /><span>MAD</span></div>
                  </div>
                </div>
                <div class="payment-summary-meta-grid">
                  <div class="payment-meta-card">
                    <div class="ticket-info-label">Lieu</div>
                    <div class="ticket-info-value"><c:out value="${ticket.event.lieu}" /></div>
                  </div>
                  <div class="payment-meta-card">
                    <div class="ticket-info-label">Date de l'evenement</div>
                    <div class="ticket-info-value"><c:out value="${eventDateDisplay}" /></div>
                  </div>
                  <div class="payment-meta-card">
                    <div class="ticket-info-label">Etat actuel</div>
                    <div class="ticket-info-value"><c:out value="${ticket.statut}" /></div>
                  </div>
                </div>
              </div>
            </div>

            <c:if test="${not empty paiement}">
              <div class="card payment-summary-card mb-4">
                <div class="card-body">
                  <div class="ticket-info-label">Paiement deja effectue</div>
                  <div class="payment-ticket-grid mt-2">
                    <div class="payment-code-panel">
                      <div class="ticket-info-label">Reference</div>
                      <div class="payment-code-value"><c:out value="${paiement.reference}" /></div>
                    </div>
                    <div class="payment-code-panel">
                      <div class="ticket-info-label">Methode</div>
                      <div class="payment-code-value"><c:out value="${paiement.methode}" /></div>
                    </div>
                  </div>
                </div>
              </div>
            </c:if>

            <c:if test="${empty paiement && ticket.statut == 'RESERVE'}">
              <form method="post" action="${pageContext.request.contextPath}/paiement" class="card payment-form-shell">
                <div class="card-body">
                  <input type="hidden" name="ticketId" value="${ticket.id}">
                  <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

                  <div class="payment-section-title">Methode de paiement</div>
                  <div class="payment-method-grid">
                    <div>
                      <label class="payment-method-card method-card-banque">
                        <input class="form-check-input" type="radio" name="methode" value="CARTE" <c:if test="${paymentMethod == 'CARTE'}">checked</c:if>>
                        <span class="payment-method-head">
                          <span class="payment-method-kicker">01</span>
                          <span class="payment-method-check"></span>
                        </span>
                        <span class="payment-method-title">Carte bancaire</span>
                        <span class="payment-method-text">Paiement direct avec verification du titulaire.</span>
                      </label>
                    </div>
                    <div>
                      <label class="payment-method-card method-card-paypal">
                        <input class="form-check-input" type="radio" name="methode" value="PAYPAL" <c:if test="${paymentMethod == 'PAYPAL'}">checked</c:if>>
                        <span class="payment-method-head">
                          <span class="payment-method-kicker">02</span>
                          <span class="payment-method-check"></span>
                        </span>
                        <span class="payment-method-title">PayPal</span>
                        <span class="payment-method-text">Validation via email de portefeuille PayPal.</span>
                      </label>
                    </div>
                    <div>
                      <label class="payment-method-card method-card-stripe">
                        <input class="form-check-input" type="radio" name="methode" value="STRIPE" <c:if test="${paymentMethod == 'STRIPE'}">checked</c:if>>
                        <span class="payment-method-head">
                          <span class="payment-method-kicker">03</span>
                          <span class="payment-method-check"></span>
                        </span>
                        <span class="payment-method-title">Stripe</span>
                        <span class="payment-method-text">Checkout rapide avec profil de facturation.</span>
                      </label>
                    </div>
                  </div>

                  <div class="row g-3 mb-4">
                    <div class="col-12 payment-method-details <c:if test='${paymentMethod != \"CARTE\"}'>d-none</c:if>" data-payment-method="CARTE">
                      <div class="card payment-detail-panel">
                        <div class="card-body">
                          <div class="payment-detail-title">Champs carte bancaire</div>
                          <div class="row g-3">
                            <div class="col-12">
                              <label class="form-label">Titulaire</label>
                              <input type="text" class="form-control" name="cardHolder" placeholder="Ayoub El Idrissi" value="${fn:escapeXml(param.cardHolder)}">
                            </div>
                            <div class="col-12 col-md-6">
                              <label class="form-label">Numero de carte</label>
                              <input type="text" class="form-control" name="cardNumber" inputmode="numeric" placeholder="4242424242424242" value="${fn:escapeXml(param.cardNumber)}">
                            </div>
                            <div class="col-6 col-md-3">
                              <label class="form-label">Expiration</label>
                              <input type="text" class="form-control" name="cardExpiry" placeholder="MM/AA" value="${fn:escapeXml(param.cardExpiry)}">
                            </div>
                            <div class="col-6 col-md-3">
                              <label class="form-label">CVV</label>
                              <input type="password" class="form-control" name="cardCvv" inputmode="numeric" placeholder="123">
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="col-12 col-lg-6 payment-method-details <c:if test='${paymentMethod != \"PAYPAL\"}'>d-none</c:if>" data-payment-method="PAYPAL">
                      <div class="card payment-detail-panel">
                        <div class="card-body">
                          <div class="payment-detail-title">Compte PayPal</div>
                          <label class="form-label">Email PayPal</label>
                          <input type="email" class="form-control" name="paypalEmail" placeholder="paypal@email.com" value="${fn:escapeXml(param.paypalEmail)}">
                        </div>
                      </div>
                    </div>
                    <div class="col-12 col-lg-6 payment-method-details <c:if test='${paymentMethod != \"STRIPE\"}'>d-none</c:if>" data-payment-method="STRIPE">
                      <div class="card payment-detail-panel">
                        <div class="card-body">
                          <div class="payment-detail-title">Profil Stripe</div>
                          <label class="form-label">Nom de facturation</label>
                          <input type="text" class="form-control mb-3" name="stripeBillingName" placeholder="Ayoub El Idrissi" value="${fn:escapeXml(param.stripeBillingName)}">
                          <label class="form-label">Email Stripe</label>
                          <input type="email" class="form-control" name="stripeEmail" placeholder="stripe@email.com" value="${fn:escapeXml(param.stripeEmail)}">
                        </div>
                      </div>
                    </div>
                  </div>

                  <button type="submit" class="btn btn-accent payment-submit w-100">Payer maintenant</button>
                </div>
              </form>
            </c:if>

            <c:if test="${empty paiement && ticket.statut != 'RESERVE'}">
              <div class="text-secondary">Ce ticket n'est plus payable.</div>
            </c:if>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</main>

<jsp:include page="/includes/footer.jsp" />

<script>
  (function () {
    const radios = document.querySelectorAll('input[name="methode"]');
    const sections = document.querySelectorAll('.payment-method-details');
    const cards = document.querySelectorAll('.payment-method-card');

    const toggleMethod = () => {
      const selected = document.querySelector('input[name="methode"]:checked')?.value;
      sections.forEach((section) => {
        section.classList.toggle('d-none', section.dataset.paymentMethod !== selected);
      });
      cards.forEach((card) => {
        const input = card.querySelector('input[name="methode"]');
        card.classList.toggle('is-selected', input?.checked);
      });
    };

    radios.forEach((radio) => radio.addEventListener('change', toggleMethod));
    toggleMethod();
  })();
</script>
