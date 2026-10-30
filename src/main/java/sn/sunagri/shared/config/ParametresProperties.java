package sn.sunagri.shared.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sunagri.parametres")
public class ParametresProperties {

    private BigDecimal superficieMaxProducteurHa = BigDecimal.valueOf(50);
    private BigDecimal commandeMinimaleKg = BigDecimal.valueOf(50);
    private BigDecimal tauxCommission = new BigDecimal("0.05");
    private BigDecimal ecartPrixAvertissement = new BigDecimal("0.30");

    public BigDecimal getSuperficieMaxProducteurHa() {
        return superficieMaxProducteurHa;
    }

    public void setSuperficieMaxProducteurHa(BigDecimal superficieMaxProducteurHa) {
        this.superficieMaxProducteurHa = superficieMaxProducteurHa;
    }

    public BigDecimal getCommandeMinimaleKg() {
        return commandeMinimaleKg;
    }

    public void setCommandeMinimaleKg(BigDecimal commandeMinimaleKg) {
        this.commandeMinimaleKg = commandeMinimaleKg;
    }

    public BigDecimal getTauxCommission() {
        return tauxCommission;
    }

    public void setTauxCommission(BigDecimal tauxCommission) {
        this.tauxCommission = tauxCommission;
    }

    public BigDecimal getEcartPrixAvertissement() {
        return ecartPrixAvertissement;
    }

    public void setEcartPrixAvertissement(BigDecimal ecartPrixAvertissement) {
        this.ecartPrixAvertissement = ecartPrixAvertissement;
    }
}
