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

(à compléter phase par phase)

## 3. Règles métier par user story

(à compléter phase par phase)

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
