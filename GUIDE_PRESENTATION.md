# Guide Presentation

## 1. Idee du projet

Gestion Evenements est une application web Jakarta EE pour gerer:

- l'inscription et la connexion des utilisateurs
- la publication et la reservation d'evenements
- le paiement et la gestion des billets
- la supervision de la plateforme par un admin

Il y a 3 roles:

- `PARTICIPANT`
- `ORGANISATEUR`
- `ADMIN`

## 2. Architecture simple

Le projet suit une architecture MVC:

- `model/`: les entites JPA (`User`, `Event`, `Ticket`, `Organisateur`, `Paiement`)
- `dao/`: acces base de donnees
- `service/`: logique metier
- `controller/`: servlets web pour les pages JSP
- `api/`: endpoints REST JAX-RS
- `webapp/`: vues JSP

Phrase simple a dire:

"La servlet recoit la requete, appelle le service, le service utilise le DAO/JPA, puis la servlet envoie les donnees a la JSP."

## 3. Base de donnees

Tables principales:

- `users`
- `organisateurs`
- `events`
- `tickets`
- `paiements`

Relations a retenir:

- un `User` participant peut avoir plusieurs `Ticket`
- un `Organisateur` peut avoir plusieurs `Event`
- un `Event` peut avoir plusieurs `Ticket`
- un `Ticket` peut avoir un `Paiement`

## 4. Flow 1: authentification

Point d'entree:

- `/auth`

Classes importantes:

- `AuthServlet`
- `AuthService`
- `UserIdentityStore`
- `SecurityConfig`

Resume:

1. L'utilisateur ouvre `/auth`
2. Il choisit login ou register
3. `AuthServlet` valide les champs
4. `AuthService` cree le compte si besoin
5. Jakarta Security verifie le mot de passe via `UserIdentityStore`
6. La session garde `user`, `role` et `csrfToken`
7. Redirection vers la page du role

## 5. Flow 2: participant

Pages principales:

- `/events`
- `/reserver`
- `/mes-billets`
- `/paiement`

Scenario:

1. Le participant voit les evenements publies
2. Il filtre par categorie, ville, date ou texte
3. Il reserve un billet
4. Il paie le billet
5. Le billet devient `PAYE`
6. Il peut voir son historique, annuler ou transferer un billet

Classes importantes:

- `EventServlet`
- `ReservationServlet`
- `MesBilletsServlet`
- `PaiementServlet`
- `TicketService`
- `PaiementService`

## 6. Flow 3: organisateur

Page principale:

- `/organisateur/events`

Ce que fait l'organisateur:

- creer un evenement
- modifier un evenement
- supprimer un evenement non publie
- voir ses statistiques
- exporter un rapport CSV
- suivre un dashboard rafraichi automatiquement

Important:

- un nouvel evenement passe en `EN_ATTENTE`
- si un evenement publie est modifie, il repart en validation admin

Classe importante:

- `OrganisateurEventServlet`

## 7. Flow 4: admin

Pages principales:

- `/admin/events`
- `/admin/users`

Ce que fait l'admin:

- valider ou refuser les evenements
- voir les statistiques globales
- activer/desactiver des comptes
- changer un compte participant en organisateur
- exporter des rapports CSV sur les evenements et utilisateurs

Classes importantes:

- `AdminEventServlet`
- `AdminUserServlet`

## 8. Paiement, email et QR

Le paiement est simule dans l'application avec:

- `CARTE`
- `PAYPAL`
- `STRIPE`

Apres paiement:

- creation du paiement
- passage du ticket en `PAYE`
- envoi d'un email
- generation d'un QR code

Classes importantes:

- `PaiementService`
- `TicketEmailService`
- `MailService`
- `QrCodeService`

## 9. Securite

Points a citer:

- Jakarta Security pour login
- `@RolesAllowed` pour proteger les pages
- token CSRF dans la session
- validations cote serveur
- headers de securite dans `SecurityHeadersFilter`
- HTTPS actif localement sur `https://localhost:8443/gestion-evenements/auth`
- redirection automatique de `http://localhost:8081/...` vers HTTPS
- cookie de session en `HttpOnly`, et `Secure` quand la connexion est en HTTPS

Phrase simple:

"Chaque action sensible est verifiee par le role, la session et un token CSRF."

## 10. API REST

Routes REST:

- `GET /gestion-evenements/api/events`
- `GET /gestion-evenements/api/events/{id}`
- `GET /gestion-evenements/api/stats`

But:

- exposer les evenements publies
- exposer un resume statistique rapide

## 11. Rapports et suivi temps reel

Rapports disponibles:

- export CSV admin des evenements
- export CSV admin des utilisateurs
- export CSV organisateur de ses evenements

Pour la demo:

- les dashboards admin et organisateur se rafraichissent automatiquement toutes les 30 secondes
- cela permet de montrer une mise a jour quasi temps reel des ventes, reservations et revenus

## 12. Demo script pour demain

Si tu as 5 minutes:

1. Ouvrir `/gestion-evenements/auth`
2. Se connecter en admin
3. Montrer validation des evenements + stats + export CSV
4. Se connecter en organisateur
5. Montrer creation/modification d'un evenement + export CSV
6. Se connecter en participant
7. Montrer liste des evenements + reservation
8. Montrer paiement
9. Montrer mes billets + QR + annulation ou transfert
10. Finir par `/api/stats`

## 13. Reponses courtes si le prof pose des questions

### C'est quoi MVC ici ?

"Les servlets sont les controllers, les JSP sont les vues, et les services/DAO gerent la logique metier et la base."

### Ou est la securite ?

"Dans Jakarta Security, les roles, les tokens CSRF, les headers HTTP de securite et maintenant le passage en HTTPS avec redirection automatique."

### Comment la capacite reste correcte ?

"La reservation verrouille l'evenement avec un lock pessimiste avant de diminuer la capacite."

### Le paiement est-il reel ?

"Non, il est simule dans l'application, mais la structure permet de brancher un vrai provider plus tard."

### Comment l'email marche ?

"Avec Jakarta Mail. Si SMTP n'est pas configure, on genere un email mock local pour la demo."

### Comment vous gerez les rapports ?

"J'ai ajoute des exports CSV pour l'admin et l'organisateur afin de telecharger les statistiques et les listes importantes de la plateforme."

### Comment vous gerez le suivi temps reel ?

"Les dashboards admin et organisateur se rafraichissent automatiquement toutes les 30 secondes pour mettre a jour les ventes, reservations et revenus."

## 14. Fichiers les plus importants a connaitre

- `controller/AuthServlet.java`
- `controller/EventServlet.java`
- `controller/ReservationServlet.java`
- `controller/PaiementServlet.java`
- `controller/MesBilletsServlet.java`
- `controller/OrganisateurEventServlet.java`
- `controller/AdminEventServlet.java`
- `controller/AdminUserServlet.java`
- `service/TicketService.java`
- `service/PaiementService.java`
- `service/CsvReportService.java`
- `service/AdminUserManagementService.java`
- `security/UserIdentityStore.java`

## 15. Resume ultra-court pour ouvrir la presentation

"J'ai developpe une application web Jakarta EE de gestion d'evenements avec 3 roles: participant, organisateur et administrateur. Le participant reserve et paie des billets, l'organisateur gere ses evenements, et l'administrateur valide et supervise la plateforme. L'application utilise Servlets, JSP, JPA, CDI, Jakarta Security et une petite API REST pour les evenements et les statistiques."
