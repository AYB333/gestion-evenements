package com.ayoub.gestionevenements.web;

final class SecurityRuntimeConfig {

    private final boolean forceHttps;
    private final int httpsPort;
    private final boolean trustForwardedProto;

    private SecurityRuntimeConfig(boolean forceHttps, int httpsPort, boolean trustForwardedProto) {
        this.forceHttps = forceHttps;
        this.httpsPort = httpsPort;
        this.trustForwardedProto = trustForwardedProto;
    }

    static SecurityRuntimeConfig load() {
        return new SecurityRuntimeConfig(
                parseBoolean(System.getenv("APP_FORCE_HTTPS"), false),
                parseInt(System.getenv("APP_HTTPS_PORT"), 8443),
                parseBoolean(System.getenv("APP_TRUST_X_FORWARDED_PROTO"), true));
    }

    boolean forceHttps() {
        return forceHttps;
    }

    int httpsPort() {
        return httpsPort;
    }

    boolean trustForwardedProto() {
        return trustForwardedProto;
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
