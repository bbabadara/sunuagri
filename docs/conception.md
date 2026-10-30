# Document de conception — SunuAgri

> Ce document est complété au fil des phases. Il présente les contextes bornés, les
> agrégats et les règles métier portées par chaque user story.

## 1. Contextes bornés

| Contexte | Responsabilité | User stories |
| --- | --- | --- |
| `production` | Producteurs, parcelles, campagnes, cultures, récoltes | US-01 à US-07 |
| `entrepot` | Livraisons, lots, stock | US-08, US-09 |
| `commercialisation` | Offres, commandes, répartition des paiements | US-10 à US-13 |
| `referentiel` | Paramètres des cultures (rendement, humidité, cycle) | US-05, US-19 |
| `notification` | Envoi simulé de SMS | US-14 |
| `identite` | Identité et rôles simulés | US-17 |
| `shared` | Noyau partagé (erreurs, unités, pagination, horloge) | transversal |

Les contextes communiquent par des ports (interfaces) et non par des dépendances
directes entre leurs domaines. Les adapters réalisent le branchement.

## 2. Agrégats et invariants

### Contexte `production`

| Agrégat | Entités / value objects | Invariants |
| --- | --- | --- |
| `Producteur` | `Parcelle` | nom, prénom, village obligatoires ; téléphone unique (application) ; superficie > 0 ; noms de parcelles uniques (insensible à la casse) ; total des superficies ≤ maximum paramétré (50 ha par défaut). |
| `Campagne` | `Saison`, `StatutCampagne` | saison obligatoire ; année positive ; date de fin > date de début ; unicité (saison, année) ; clôture irréversible ; une campagne clôturée refuse cultures, semis et récoltes. |
| `Culture` | `StatutCulture`, `ParametresCulture` | campagne ouverte ; type du référentiel ; superficie > 0 ; somme des superficies emblavées ≤ superficie de la parcelle ; statut PLANIFIEE → SEMEE → RECOLTEE sans retour ; récolte ≥ semis + cycle minimal ; rendement ≤ 2 × rendement de référence ; une seule récolte. |

`ParametresCulture` est un **instantané** des valeurs de référence (rendement, humidité,
cycle) pris à la déclaration de la culture : modifier le référentiel n'affecte pas les
cultures existantes (préparation US-19).

## 3. Règles métier par user story

### US-01 — Enregistrer un producteur

- Téléphone unique : vérifié dans `ProducteurService.enregistrer` avant création.
- Nom, prénom, village obligatoires : `Producteur.creer` via `Validations`.
- Téléphone déjà utilisé → HTTP 409 avec le code `TELEPHONE_DEJA_UTILISE`.
- Identifiant UUID généré et renvoyé.

### US-02 — Déclarer une parcelle

- Superficie strictement positive : `Parcelle` + `@DecimalMin`.
- Total ≤ 50 ha : `Producteur.ajouterParcelle` avec la valeur paramétrée.
- Nom unique par producteur : `Producteur.ajouterParcelle` (insensible à la casse).
- Une parcelle appartient à un seul producteur par construction (entité interne).

### US-03 — Consulter les parcelles d'un producteur

- Liste et détail exposés.
- Producteur inexistant → HTTP 404. Producteur d'autrui → HTTP 404 (US-17).

### US-04 — Ouvrir et clôturer une campagne

- Saison limitée à `HIVERNAGE` ou `CONTRE_SAISON`.
- Date de fin > date de début.
- Unicité (saison, année) → HTTP 409 `CAMPAGNE_DEJA_OUVERTE`.
- Clôture idempotente refusée : double clôture → 422.

### US-05 — Déclarer une culture

- Campagne ouverte exigée.
- Type présent dans le référentiel configuré.
- Superficie > 0 et cumul ≤ superficie de la parcelle.
- Statut initial `PLANIFIEE`, paramètres de référence figés.

### US-06 — Suivre l'avancement d'une culture

- `semer` uniquement depuis `PLANIFIEE`.
- `recolter` uniquement depuis `SEMEE`.
- La date de récolte doit être ≥ date de semis + cycle minimal.
- Aucun retour en arrière possible.

### US-07 — Déclarer une récolte

- La culture doit être semée.
- Quantité > 0.
- Rendement = quantité / superficie emblavée ; refus si > 2 × rendement de référence.
- Une culture ne peut être récoltée qu'une fois (statut `RECOLTEE`).

### Contexte `entrepot`

| Agrégat | Entités / value objects | Invariants |
| --- | --- | --- |
| `Lot` | `Livraison`, `StockLigne` | quantité > 0 ; consommation jamais supérieure au disponible ; un lot est identifié, rattaché à un producteur et daté. |
| `Stock` (projection) | `StockLigne` | total = somme des lots disponibles ; réservé = somme des offres en cours ; disponible = total − réservé. |

### US-08 — Livrer une récolte à l'entrepôt

- La culture doit être au statut `RECOLTEE`.
- Quantité > 0.
- Humidité ≤ seuil capturé dans les paramètres de la culture.
- Cumul des livraisons ≤ quantité récoltée.
- Chaque livraison acceptée crée un `Lot` (producteur, culture, qualité, date, quantité).
- Toutes les vérifications ont lieu avant toute écriture : une livraison refusée ne
  modifie rien.

### US-09 — Consulter le stock

- Agrégation par culture et par qualité.
- `total` = somme des quantités encore disponibles dans les lots.
- `réservé` = quantité réservée par les offres non retirées (port
  `ReservationsStockPort`, fourni par le contexte commercialisation).
- `disponible` = total − réservé, borné à zéro.
- Les lots sont consommés du plus ancien au plus récent (FIFO) via `StockPort`.

### Contexte `commercialisation`

| Agrégat | Entités / value objects | Invariants |
| --- | --- | --- |
| `Offre` | `StatutOffre` | quantité et prix > 0 ; la quantité publiée ne dépasse pas le stock disponible ; statuts `OUVERTE` → `EPUISEE` / `RETIREE` ; retirer libère la quantité non vendue ; une commande ne dépasse pas la quantité restante. |
| `Commande` | `StatutCommande` | acheteur obligatoire ; quantité > 0 et ≥ minimum paramétré ; montant = quantité × prix arrondi au franc (`HALF_UP`) ; statut `EN_ATTENTE` → `LIVREE` ou `ANNULEE` ; une commande livrée ne peut plus être annulée. |
| `PaiementDu` | `StatutPaiement` | montant > 0 ; statut initial `A_PAYER` ; un SMS au plus par paiement (`smsEnvoye`). |
| `RepartitionPaiement` | `ResultatRepartition` | commission prélevée avant répartition ; somme des parts + commission = montant ; plus forts restes avec départage par UUID (ADR-0004). |

### US-10 — Publier une offre de vente

- Culture et qualité obligatoires, quantité et prix strictement positifs.
- `quantite ≤ stock disponible` : `OffreService.publier` interroge `StockPort.quantiteDisponible`.
- Une offre publiée réserve sa quantité : `ReservationsStockPort` est fourni par
  `commercialisation/adapter/out/stock/ReservationsStockAdapter`.
- Statuts : `OUVERTE`, `EPUISEE`, `RETIREE`. `retirer` déplace la quantité restante vers
  `quantiteRetiree`, ce qui libère la réservation du stock.

### US-11 — Passer une commande

- Réservé au rôle `ACHETEUR`.
- Quantité ≥ `sunagri.parametres.commande-minimale-kg` (50 kg par défaut).
- Quantité ≤ quantité restante de l'offre ; l'offre devient `EPUISEE` quand elle atteint zéro.
- Montant = quantité × prix au kg, arrondi au franc (`HALF_UP`).
- Statut initial `EN_ATTENTE`.

### US-12 — Annuler une commande

- Un acheteur ne peut annuler que sa propre commande ; un tiers reçoit un 404.
- Annulation possible uniquement avant livraison.
- La quantité annulée retourne dans `quantiteRestante` ; une offre `EPUISEE` redevient
  `OUVERTE`.
- Si l'offre a été retirée entre-temps, la quantité annulée rejoint `quantiteRetiree`.

### US-13 — Livrer une commande et répartir le paiement

- Réservé aux rôles `AGENT` et `ADMIN` ; seule une commande `EN_ATTENTE` est livrable.
- `StockPort.consommer` prélève les lots FIFO par culture et qualité.
- Les quantités apportées sont agrégées par producteur ; la commission paramétrée
  (5 % par défaut) est retenue puis le reste est réparti par `RepartitionPaiement`.
- Chaque part strictement positive crée un `PaiementDu` au statut `A_PAYER`.
- `Σ paiements + commission = montant de la commande` (testé).
- La livraison met à jour la quantité livrée de l'offre, le statut de la commande et
  déclenche la notification (US-14).

### US-14 — Être prévenu par SMS d'un paiement

- `NotificationPaiementSimulee` envoie un SMS par paiement via `SmsSender` (simulé).
- Le message est en français et contient le montant en XOF.
- Un SMS au plus par paiement : `PaiementDu.smsEnvoye` n'est positionné qu'après succès.
- Un échec d'envoi n'annule pas la livraison ; il est tracé dans `JournalNotifications`.

## 4. Hypothèses retenues

- Une seule coopérative : pas de multi-tenant.
- L'identité est simulée via l'en-tête `X-User-Id`.
- Les montants sont des entiers en XOF ; la commission est arrondie au franc supérieur
  en cas de demi-franc (`HALF_UP`), puis le reste est réparti par la méthode des plus
  forts restes.
- Les valeurs de référence sont chargées depuis la configuration, jamais codées en dur
  dans les règles.
- Les identifiants sont des UUID.
- Les dates métier sont des `LocalDate` ; l'horloge système est injectée via `ClockPort`.
