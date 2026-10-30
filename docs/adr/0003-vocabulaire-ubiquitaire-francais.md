# ADR-0003 — Vocabulaire ubiquitaire en français

## Contexte

Le sujet fournit un vocabulaire métier en français (producteur, parcelle, campagne,
culture, récolte, lot, offre, commande, paiement dû). L'exigence E4 demande que ce
vocabulaire se retrouve dans le code, l'API et la documentation.

## Décision

Utiliser le vocabulaire métier français à tous les niveaux :

- classes et méthodes du domaine (`Producteur`, `Parcelle`, `Culture`, `Offre`,
  `Commande`, `PaiementDu`) ;
- noms de ressources et de champs JSON (`/producteurs`, `superficieEmblavee`,
  `prixKg`) ;
- messages d'erreur et documentation.

Les termes purement techniques restent en anglais lorsqu'ils n'ont pas d'équivalent
métier (`Repository`, `Controller`, `Port`).

## Conséquences

- Lisibilité immédiate pour la coopérative et le correcteur.
- Cohérence entre les stories, le contrat OpenAPI et le code.
- Les conventions Java restent respectées (PascalCase, camelCase).
