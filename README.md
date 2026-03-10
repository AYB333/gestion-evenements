# Gestion Evenements

## Build and deploy

```powershell
mvn clean -DskipTests package
Copy-Item -Path .\target\gestion-evenements.war -Destination C:\apache-tomcat-11.0.18\webapps\gestion-evenements.war -Force
```

## TP2 checklist

- Secure authentication and role-based access with Jakarta Security
- Participant reservation, payment, cancellation, transfer, QR code
- Organizer dashboard with sales, revenue, auto-refresh, CSV export
- Admin dashboard with validation, platform statistics, CSV export
- Admin user management with account activation and participant/organizer role changes
- REST API for events and stats
- Email notifications via SMTP or local mock fallback
- Security hardening with CSRF, server-side validation, CSP, HSTS on HTTPS, and security headers

## Reporting and dashboards

- `GET /gestion-evenements/admin/events?export=csv`
- `GET /gestion-evenements/admin/users?export=csv`
- `GET /gestion-evenements/organisateur/events?export=csv`

The admin and organizer dashboards auto-refresh every 30 seconds to keep sales and reservation metrics current during a live demo.

## Payment providers

Current project supports the full payment workflow in-app with 3 providers:

- `CARTE`
- `PAYPAL`
- `STRIPE`

The validation and persistence flow are implemented locally inside the app. If you want a real external gateway later, connect the submit action in `PaiementService` to Stripe Checkout or PayPal Orders API.

## REST API (JAX-RS)

- `GET /gestion-evenements/api/events`
- `GET /gestion-evenements/api/events/{id}`
- `GET /gestion-evenements/api/stats`

Examples:

```text
http://localhost:8081/gestion-evenements/api/events?category=concert&city=casablanca&date=2026-03-10
http://localhost:8081/gestion-evenements/api/stats
```

## SMTP config (optional)

If SMTP variables are missing, emails are saved as mock files in `%TEMP%\gestion-evenements-mails`.

- `SMTP_HOST`
- `SMTP_PORT` (default: `587`)
- `SMTP_USER`
- `SMTP_PASS`
- `SMTP_FROM`
- `SMTP_TLS` (`true`/`false`, default: `true`)

With no SMTP variables, the app writes mock emails into `%TEMP%\gestion-evenements-mails`.

## HTTPS setup (Tomcat 11)

Project-side support is already implemented:

- `SecurityHeadersFilter` can force HTTPS redirects when `APP_FORCE_HTTPS=true`
- secure sessions use cookies only with `HttpOnly`
- HSTS is sent automatically on HTTPS responses

Local Tomcat is now configured with:

- `https://localhost:8443/gestion-evenements/auth`
- automatic `301` redirect from `http://localhost:8081/gestion-evenements/auth`

1. Create a local keystore:

```powershell
keytool -genkeypair -alias gestion-evenements -keyalg RSA -keysize 2048 -validity 3650 `
  -keystore C:\apache-tomcat-11.0.18\conf\gestion-evenements.p12 -storetype PKCS12 `
  -storepass changeit -dname "CN=localhost, OU=Dev, O=GestionEvenements, L=Casablanca, S=Casablanca, C=MA"
```

2. In `C:\apache-tomcat-11.0.18\conf\server.xml`, add an HTTPS connector inside `<Service name="Catalina">`:

```xml
<Connector port="8443"
           protocol="org.apache.coyote.http11.Http11NioProtocol"
           SSLEnabled="true"
           scheme="https"
           secure="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/gestion-evenements.p12"
                     certificateKeystorePassword="changeit"
                     certificateKeystoreType="PKCS12" />
    </SSLHostConfig>
</Connector>
```

3. Restart Tomcat and open:

```text
https://localhost:8443/gestion-evenements/auth
```

On HTTPS requests, the application automatically sends HSTS and additional browser security headers.

Environment variables used by the app-side HTTPS filter:

- `APP_FORCE_HTTPS=true`
- `APP_HTTPS_PORT=8443`
- `APP_TRUST_X_FORWARDED_PROTO=true`
