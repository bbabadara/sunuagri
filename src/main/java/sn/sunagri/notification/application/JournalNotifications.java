package sn.sunagri.notification.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import sn.sunagri.notification.domain.EntreeJournal;

@Component
public class JournalNotifications {

    private final List<EntreeJournal> entrees = new CopyOnWriteArrayList<>();

    public void succes(UUID paiementId, String telephone, String message, LocalDate date) {
        entrees.add(new EntreeJournal(paiementId, telephone, message, true, null, date));
    }

    public void echec(UUID paiementId, String telephone, String message, String motif, LocalDate date) {
        entrees.add(new EntreeJournal(paiementId, telephone, message, false, motif, date));
    }

    public List<EntreeJournal> entrees() {
        return List.copyOf(entrees);
    }
}
