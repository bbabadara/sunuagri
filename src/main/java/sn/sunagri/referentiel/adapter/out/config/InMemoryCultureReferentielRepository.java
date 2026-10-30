package sn.sunagri.referentiel.adapter.out.config;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import sn.sunagri.referentiel.application.port.out.CultureReferentielRepository;
import sn.sunagri.referentiel.domain.CultureReferentiel;
import sn.sunagri.shared.domain.TypeCulture;

@Repository
public class InMemoryCultureReferentielRepository implements CultureReferentielRepository {

    private final Map<TypeCulture, CultureReferentiel> cultures = new EnumMap<>(TypeCulture.class);

    public InMemoryCultureReferentielRepository(ReferentielProperties proprietes) {
        proprietes.getCultures().forEach((cle, valeur) -> {
            TypeCulture type = TypeCulture.valueOf(cle.trim().toUpperCase(Locale.ROOT));
            cultures.put(type, new CultureReferentiel(
                    type,
                    valeur.getLibelle() == null ? type.name() : valeur.getLibelle(),
                    valeur.getRendementKgHa(),
                    valeur.getHumiditeMax(),
                    valeur.getCycleMinJours() == null ? 1 : valeur.getCycleMinJours()));
        });
    }

    @Override
    public Optional<CultureReferentiel> parType(TypeCulture type) {
        return Optional.ofNullable(cultures.get(type));
    }

    @Override
    public List<CultureReferentiel> toutes() {
        return List.copyOf(cultures.values());
    }
}
