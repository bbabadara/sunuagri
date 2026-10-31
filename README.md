# SunuAgri

Plateforme de gestion d'une coopérative agricole sénégalaise : producteurs, parcelles,
campagnes, cultures, récoltes, stock, offres, commandes et paiements dus.

Projet réalisé en Java 21 / Spring Boot 3, selon une architecture hexagonale et un
découpage en contextes bornés (DDD).

## Prérequis

- JDK 21
- Maven 3.9+

## Lancer l'application

```bash
mvn spring-boot:run
```

- API : http://localhost:8080/api/v1
- Documentation OpenAPI interactive : http://localhost:8080/swagger-ui.html
- Contrat brut : http://localhost:8080/v3/api-docs

## Lancer les tests

```bash
mvn test
```

Les tests métier s'exécutent sans serveur web ni base de données (exigence E1).

## Identité simulée

L'identité est portée par l'en-tête `X-User-Id` :

| Valeur | Rôle |
| --- | --- |
| `agent-1` | agent de coopérative |
| `admin-1` | administrateur |
| `acheteur:<identifiant>` | acheteur (ex. `acheteur:client-1`) |
| `producteur:<uuid>` | producteur (uuid renvoyé à l'enregistrement) |

Sans en-tête valide, l'API répond `401`.

## Tester l'API avec Swagger UI

1. Lancer l'application : `mvn spring-boot:run`.
2. Ouvrir http://localhost:8080/swagger-ui.html.
3. Cliquer sur **Authorize** et saisir une identité (`agent-1`, `admin-1`,
   `acheteur:client-1`, `producteur:<uuid>`).
4. Choisir un endpoint, cliquer sur **Try it out**, renseigner les paramètres puis
   **Execute**.

Le contrat OpenAPI est généré à la volée sur http://localhost:8080/v3/api-docs.

## Scénario de démonstration

Voir `docs/demonstration.md` pour le scénario complet, de l'enregistrement d'un
producteur jusqu'au paiement dû.

## Documentation

- `docs/glossaire.md` — vocabulaire ubiquitaire
- `docs/conception.md` — contextes, agrégats, règles par user story
- `docs/adr/` — décisions d'architecture
- `openapi.yaml` — contrat d'API

## User stories réalisées

| Story | Intitulé | Statut |
| --- | --- | --- |
| US-01 | Enregistrer un producteur | réalisée |
| US-02 | Déclarer une parcelle | réalisée |
| US-03 | Consulter les parcelles d'un producteur | réalisée |
| US-04 | Ouvrir et clôturer une campagne | réalisée |
| US-05 | Déclarer une culture | réalisée |
| US-06 | Suivre l'avancement d'une culture | réalisée |
| US-07 | Déclarer une récolte | réalisée |
| US-08 | Livrer une récolte à l'entrepôt | réalisée |
| US-09 | Consulter le stock | réalisée |
| US-10 | Publier une offre de vente | réalisée |
| US-11 | Passer une commande | réalisée |
| US-12 | Annuler une commande | réalisée |
| US-13 | Livrer une commande et répartir le paiement | réalisée |
| US-14 | Être prévenu par SMS d'un paiement | réalisée |
| US-15 | Importer des producteurs en lot | réalisée |
| US-16 | Consulter l'historique de mes commandes | réalisée |
| US-17 | Accéder uniquement à ce qui me concerne | réalisée |
