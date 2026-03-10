package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.model.Ticket;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.StripeClient;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.Locale;

@ApplicationScoped
public class StripeCheckoutService {

    public record CheckoutSessionResult(String sessionId, String checkoutUrl) {
    }

    public record StripeVerificationResult(boolean success, String paymentReference, String message) {
    }

    public boolean isEnabled() {
        return loadConfig().enabled();
    }

    public CheckoutSessionResult createCheckoutSession(Ticket ticket,
                                                       Long userId,
                                                       String customerEmail,
                                                       String customerName,
                                                       String successUrl,
                                                       String cancelUrl) throws StripeException {
        if (ticket == null || ticket.getId() == null || userId == null) {
            return null;
        }

        StripeConfig config = loadConfig();
        if (!config.enabled()) {
            return null;
        }

        StripeClient client = new StripeClient(config.secretKey());

        String eventTitle = ticket.getEvent() == null ? "Evenement" : safeValue(ticket.getEvent().getTitre(), "Evenement");
        String eventLocation = ticket.getEvent() == null ? "" : safeValue(ticket.getEvent().getLieu(), "");
        String ticketCode = safeValue(ticket.getCode(), String.valueOf(ticket.getId()));

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(eventTitle)
                        .setDescription(buildDescription(eventLocation, ticketCode))
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(config.currency())
                        .setUnitAmount(toMinorAmount(ticket.getPrix()))
                        .setProductData(productData)
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(ticket.getId().toString())
                .setCustomerEmail(customerEmail)
                .putMetadata("ticketId", ticket.getId().toString())
                .putMetadata("userId", userId.toString())
                .putMetadata("ticketCode", ticketCode)
                .putMetadata("customerName", safeValue(customerName, "Participant"))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(priceData)
                                .build())
                .build();

        Session session = client.v1().checkout().sessions().create(params);
        if (session == null || session.getId() == null || session.getUrl() == null) {
            return null;
        }
        return new CheckoutSessionResult(session.getId(), session.getUrl());
    }

    public StripeVerificationResult verifyPaidSession(String sessionId, Long ticketId, Long userId) throws StripeException {
        if (sessionId == null || sessionId.isBlank() || ticketId == null || userId == null) {
            return new StripeVerificationResult(false, null, "Session Stripe invalide.");
        }

        StripeConfig config = loadConfig();
        if (!config.enabled()) {
            return new StripeVerificationResult(false, null, "Stripe n'est pas configure.");
        }

        StripeClient client = new StripeClient(config.secretKey());
        Session session = client.v1().checkout().sessions().retrieve(sessionId);
        if (session == null) {
            return new StripeVerificationResult(false, null, "Session Stripe introuvable.");
        }

        if (!String.valueOf(ticketId).equals(session.getClientReferenceId())) {
            return new StripeVerificationResult(false, null, "Session Stripe non associee a ce ticket.");
        }

        String metadataTicketId = session.getMetadata() == null ? null : session.getMetadata().get("ticketId");
        String metadataUserId = session.getMetadata() == null ? null : session.getMetadata().get("userId");
        if (!String.valueOf(ticketId).equals(metadataTicketId) || !String.valueOf(userId).equals(metadataUserId)) {
            return new StripeVerificationResult(false, null, "Metadonnees Stripe invalides.");
        }

        String paymentStatus = session.getPaymentStatus();
        if (paymentStatus == null || !"paid".equalsIgnoreCase(paymentStatus)) {
            return new StripeVerificationResult(false, null, "Le paiement Stripe n'est pas encore confirme.");
        }

        String status = session.getStatus();
        if (status != null && !"complete".equalsIgnoreCase(status)) {
            return new StripeVerificationResult(false, null, "La session Stripe n'est pas complete.");
        }

        String paymentReference = safeValue(session.getPaymentIntent(), session.getId());
        if (paymentReference.length() > 100) {
            paymentReference = paymentReference.substring(0, 100);
        }
        return new StripeVerificationResult(true, paymentReference, null);
    }

    private StripeConfig loadConfig() {
        String secretKey = getenvOrDefault("STRIPE_SECRET_KEY", "").trim();
        String currency = getenvOrDefault("STRIPE_CURRENCY", "mad").trim().toLowerCase(Locale.ROOT);
        if (currency.isBlank()) {
            currency = "mad";
        }
        return new StripeConfig(secretKey, currency);
    }

    private String getenvOrDefault(String key, String def) {
        String value = System.getenv(key);
        return value == null ? def : value;
    }

    private long toMinorAmount(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.movePointRight(2).longValueExact();
    }

    private String buildDescription(String eventLocation, String ticketCode) {
        if (eventLocation == null || eventLocation.isBlank()) {
            return "Billet " + ticketCode;
        }
        return eventLocation + " | Billet " + ticketCode;
    }

    private String safeValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private record StripeConfig(String secretKey, String currency) {
        boolean enabled() {
            return secretKey != null && !secretKey.isBlank();
        }
    }
}
