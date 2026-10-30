# ADR-0005 — API versionnée et erreurs Problem Details

## Contexte

L'API doit être décrite par un contrat OpenAPI cohérent (E5), exposer des erreurs dans
un format unique sans détail interne (E6) et évoluer sans casser les clients (E7).

## Décision

- Toutes les routes sont préfixées par `/api/v1`.
- Les évolutions futures se font de façon additive (nouveaux champs, nouvelles routes)
  ou via `/api/v2` ; aucun champ existant n'est renommé ou supprimé dans `v1`.
- Les erreurs suivent la RFC 9457 *Problem Details for HTTP APIs* :
  `type`, `title`, `status`, `detail`, `instance`, complétés par `code` (code métier
  stable) et, le cas échéant, `erreurs` (liste de champs invalides).
- Aucune stack trace ni message interne n'est renvoyé.
- Les collections sont paginées (`page`, `size`, `totalElements`, `totalPages`).
- Les opérations sensibles acceptent un en-tête `Idempotency-Key`.
- Le contrat `openapi.yaml` est généré par springdoc puis commité ; un test vérifie qu'il
  se génère correctement.

## Correspondance des statuts

| Situation | Statut HTTP |
| --- | --- |
| Validation de forme (Bean Validation) | 400 |
| Identité absente ou inconnue | 401 |
| Ressource d'autrui (non révélée) | 404 |
| Ressource inexistante | 404 |
| Conflit (téléphone, campagne en double) | 409 |
| Règle métier violée | 422 |

## Conséquences

- Clients stables et erreurs exploitables par programme.
- Messages métier en français, codes stables en anglais technique.
- Le contrat est la référence, versionnée avec le code.
