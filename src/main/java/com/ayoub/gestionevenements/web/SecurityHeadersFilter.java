package com.ayoub.gestionevenements.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    private static final String CSP = String.join(" ",
            "default-src 'self';",
            "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net;",
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com;",
            "font-src 'self' https://fonts.gstatic.com;",
            "img-src 'self' data: https://images.unsplash.com;",
            "connect-src 'self';",
            "frame-ancestors 'none';",
            "base-uri 'self';",
            "form-action 'self'");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            SecurityRuntimeConfig config = SecurityRuntimeConfig.load();
            if (shouldRedirectToHttps(httpRequest, config)) {
                httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpResponse.setHeader("Location", buildHttpsUrl(httpRequest, config.httpsPort()));
                return;
            }

            httpResponse.setHeader("Content-Security-Policy", CSP);
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
            httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin");
            httpResponse.setHeader("Cache-Control", "no-store");
            if (isSecureRequest(httpRequest, config)) {
                httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }
        }
        chain.doFilter(request, response);
    }

    private boolean shouldRedirectToHttps(HttpServletRequest request, SecurityRuntimeConfig config) {
        return config.forceHttps() && !isSecureRequest(request, config);
    }

    private boolean isSecureRequest(HttpServletRequest request, SecurityRuntimeConfig config) {
        if (request.isSecure()) {
            return true;
        }
        if (!config.trustForwardedProto()) {
            return false;
        }
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forwardedProto != null && "https".equalsIgnoreCase(forwardedProto.trim());
    }

    private String buildHttpsUrl(HttpServletRequest request, int httpsPort) {
        StringBuilder target = new StringBuilder("https://").append(request.getServerName());
        if (httpsPort > 0 && httpsPort != 443) {
            target.append(':').append(httpsPort);
        }
        target.append(request.getRequestURI());
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            target.append('?').append(request.getQueryString());
        }
        return target.toString();
    }
}
