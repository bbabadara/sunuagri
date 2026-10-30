# ADR-0004 — Répartition des paiements et arrondis

## Contexte

Lors de la livraison d'une commande (US-13), le montant doit être réparti entre les
producteurs proportionnellement aux quantités apportées dans les lots consommés, après
retenue d'une commission de 5 %. La somme des paiements et de la commission doit être
exactement égale au montant de la commande, sans perdre ni créer de franc.

## Décision

1. Les montants sont des entiers en francs CFA (XOF).
2. La commission est calculée ainsi :
   `commission = arrondi(montant × tauxCommission, HALF_UP)`.
3. Le reste à répartir est `reste = montant − commission`.
4. Les lots sont consommés du plus ancien au plus récent (FIFO), par culture et qualité.
5. Les quantités apportées sont agrégées par producteur.
6. Le reste est réparti au prorata avec la **méthode des plus forts restes** (Hamilton) :
   - part entière de chaque producteur = `floor(reste × qᵢ / totalQty)` ;
   - les francs restants sont attribués aux plus grandes parties fractionnaires, avec
     égalité tranchée par identifiant de producteur croissant (déterminisme).
7. Les paiements d'un montant nul après répartition ne sont pas créés.

## Conséquences

- Invariant garanti et testé : `Σ paiements + commission = montant`.
- Résultat déterministe et reproductible.
- La règle est isolée dans un service de domaine (`RepartitionPaiement`) et testée
  indépendamment de l'API et de la base.
