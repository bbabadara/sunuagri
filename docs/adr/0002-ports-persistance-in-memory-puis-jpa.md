# ADR-0002 — Ports de persistance : in-memory d'abord, JPA ensuite

## Contexte

Les règles métier doivent être testables sans base de données (E1) et le choix de la
base doit pouvoir changer sans modifier le domaine (E2). Le projet doit néanmoins être
démontrable rapidement.

## Décision

1. Les agrégats sont persistés via des interfaces de dépôt (`port/out`).
2. L'implémentation initiale est **en mémoire** (`adapter/out/memory`), branchée comme
   bean Spring. Elle sert à la démonstration et aux tests.
3. Une implémentation JPA/H2 pourra être ajoutée plus tard, sans modifier le domaine ni
   les services applicatifs, en remplaçant le bean d'adapter.

## Conséquences

- Domaine et cas d'usage testables en pur Java.
- Les tests d'intégration BDD restent optionnels.
- L'implémentation en mémoire n'offre pas de durabilité : elle est documentée comme
  adapter de démonstration, remplaçable.
- Les agrégats restent des objets métier, pas des entités JPA.
