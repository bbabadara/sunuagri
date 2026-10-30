package sn.sunagri.referentiel.adapter.out.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sunagri.referentiel")
public class ReferentielProperties {

    private Map<String, CultureProperties> cultures = new LinkedHashMap<>();

    public Map<String, CultureProperties> getCultures() {
        return cultures;
    }

    public void setCultures(Map<String, CultureProperties> cultures) {
        this.cultures = cultures;
    }

    public static class CultureProperties {

        private String libelle;
        private BigDecimal rendementKgHa;
        private BigDecimal humiditeMax;
        private Integer cycleMinJours;

        public String getLibelle() {
            return libelle;
        }

        public void setLibelle(String libelle) {
            this.libelle = libelle;
        }

        public BigDecimal getRendementKgHa() {
            return rendementKgHa;
        }

        public void setRendementKgHa(BigDecimal rendementKgHa) {
            this.rendementKgHa = rendementKgHa;
        }

        public BigDecimal getHumiditeMax() {
            return humiditeMax;
        }

        public void setHumiditeMax(BigDecimal humiditeMax) {
            this.humiditeMax = humiditeMax;
        }

        public Integer getCycleMinJours() {
            return cycleMinJours;
        }

        public void setCycleMinJours(Integer cycleMinJours) {
            this.cycleMinJours = cycleMinJours;
        }
    }
}
