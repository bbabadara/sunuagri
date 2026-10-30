package sn.sunagri.production.domain;

import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.RegleMetierException;

public class Campagne {

    private final UUID id;
    private final Saison saison;
    private final int annee;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private StatutCampagne statut;

    private Campagne(UUID id, Saison saison, int annee, LocalDate dateDebut, LocalDate dateFin) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant de la campagne est obligatoire.");
        }
        if (saison == null) {
            throw new RegleMetierException("SAISON_INVALIDE", "La saison est obligatoire.");
        }
        if (annee <= 0) {
            throw new RegleMetierException("ANNEE_INVALIDE", "L'année de la campagne doit être positive.");
        }
        if (dateDebut == null || dateFin == null) {
            throw new RegleMetierException("DATES_INVALIDES", "Les dates de début et de fin sont obligatoires.");
        }
        if (!dateFin.isAfter(dateDebut)) {
            throw new RegleMetierException("DATES_INVALIDES",
                    "La date de fin doit être postérieure à la date de début.");
        }
        this.id = id;
        this.saison = saison;
        this.annee = annee;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = StatutCampagne.OUVERTE;
    }

    public static Campagne ouvrir(UUID id, Saison saison, int annee, LocalDate dateDebut, LocalDate dateFin) {
        return new Campagne(id, saison, annee, dateDebut, dateFin);
    }

    public void cloturer() {
        if (statut == StatutCampagne.CLOTUREE) {
            throw new RegleMetierException("CAMPAGNE_DEJA_CLOTUREE", "La campagne est déjà clôturée.");
        }
        statut = StatutCampagne.CLOTUREE;
    }

    public void exigerOuverte() {
        if (statut == StatutCampagne.CLOTUREE) {
            throw new RegleMetierException("CAMPAGNE_CLOTUREE",
                    "La campagne est clôturée : aucune nouvelle culture ni récolte n'est acceptée.");
        }
    }

    public boolean estOuverte() {
        return statut == StatutCampagne.OUVERTE;
    }

    public UUID id() {
        return id;
    }

    public Saison saison() {
        return saison;
    }

    public int annee() {
        return annee;
    }

    public LocalDate dateDebut() {
        return dateDebut;
    }

    public LocalDate dateFin() {
        return dateFin;
    }

    public StatutCampagne statut() {
        return statut;
    }
}
