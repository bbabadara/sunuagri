# ADR-0001 — Monolithe modulaire et architecture hexagonale

## Contexte

Le projet est individuel, doit rester simple à lancer et à corriger, tout en séparant
clairement le métier des détails techniques (base de données, SMS, API). Les exigences
E2 (interchangeabilité des adapters) et E3 (règle métier unique) imposent une frontière
forte entre domaine et infrastructure.

## Décision

Adopter un **monolithe modulaire** découpé en contextes bornés, chacun organisé selon
l'architecture hexagonale :

```
<contexte>/domain            règles métier pures, sans dépendance framework
<contexte>/application       cas d'usage, ports entrants et sortants
<contexte>/adapter/in|out    REST, persistance, SMS, intégrations
```

Un contexte ne dépend du domaine d'un autre contexte que via un port. Les dépendances
sont vérifiées par ArchUnit. Aucun import Spring dans les packages `domain`.

## Conséquences

- Tests métier rapides, sans serveur ni base (E1).
- Remplacement d'un adapter sans toucher au domaine (E2).
- Un seul déploiement, mais des frontières explicites ; pas de coût opérationnel de
  microservices.
- Discipline requise sur les dépendances entre contextes, automatisée par ArchUnit.
