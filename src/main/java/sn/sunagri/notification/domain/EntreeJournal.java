package sn.sunagri.notification.domain;

import java.time.LocalDate;
import java.util.UUID;

public record EntreeJournal(
        UUID paiementId,
        String telephone,
        String message,
        boolean succes,
        String motif,
        LocalDate date) {
}
