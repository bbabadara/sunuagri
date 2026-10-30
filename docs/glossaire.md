# Glossaire du projet SunuAgri

Le vocabulaire ci-dessous est le vocabulaire ubiquitaire du projet. Il est utilisé à
l'identique dans le code, l'API et la documentation (exigence E4).

| Terme | Définition |
| --- | --- |
| **Producteur** | Agriculteur membre de la coopérative. Cultive des parcelles et apporte sa récolte. Identifié par son numéro de téléphone, unique dans la coopérative. |
| **Parcelle** | Terrain appartenant à un seul producteur, caractérisé par un nom, une commune et une superficie en hectares. |
| **Campagne** | Saison agricole (`HIVERNGE` ou `CONTRE_SAISON`) d'une année donnée, bornée par une date de début et une date de fin. Une campagne peut être ouverte ou clôturée. |
| **Culture** | Plante cultivée sur une parcelle pendant une campagne. Porte une superficie emblavée et un statut : `PLANIFIEE`, `SEMEE`, `RECOLTEE`. |
| **Récolte** | Quantité produite par une culture, exprimée en kilogrammes. Une culture ne peut être récoltée qu'une seule fois. |
| **Livraison** | Apport d'une récolte à l'entrepôt de la coopérative. Une livraison acceptée crée un lot de stock. |
| **Lot** | Quantité reçue d'un producteur pour une culture et une qualité données, avec sa date de réception. Les lots sont consommés du plus ancien au plus récent. |
| **Qualité** | Classe d'une livraison ou d'un lot : `A`, `B` ou `C`. |
| **Stock** | Ensemble des lots disponibles, agrégés par culture et par qualité. Se décompose en total, réservé et disponible. |
| **Offre** | Quantité mise en vente par la coopérative pour une culture et une qualité, à un prix au kilogramme en XOF. Statuts : `OUVERTE`, `EPUISEE`, `RETIREE`. |
| **Commande** | Demande d'un acheteur sur une offre. Statuts : `EN_ATTENTE`, `LIVREE`, `ANNULEE`. |
| **Paiement dû** | Part du montant d'une commande revenant à un producteur. Statut : `A_PAYER` ou `PAYE`. |
| **Commission** | Part retenue par la coopérative sur le montant d'une commande (5 % par défaut). |
| **Prix de référence** | Prix marché d'une culture fourni par un service externe (US-18, hors périmètre). |

## Unités et devises

- Superficie : hectares (ha), valeurs décimales.
- Quantités : kilogrammes (kg).
- Montants : francs CFA (XOF), entiers (aucun centime).
