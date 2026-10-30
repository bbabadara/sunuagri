package sn.sunagri;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "sn.sunagri")
class ArchitectureTest {

    @ArchTest
    static final ArchRule le_domaine_ne_depend_pas_de_spring =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .allowEmptyShould(true).because("le domaine doit rester pur et testable sans framework");

    @ArchTest
    static final ArchRule les_adapters_entrants_ne_dependent_pas_des_adapters_sortants =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
                    .allowEmptyShould(true).because("les adapters ne se parlent pas directement");

    @ArchTest
    static final ArchRule production_ne_depend_pas_de_commercialisation =
            noClasses().that().resideInAPackage("sn.sunagri.production..")
                    .should().dependOnClassesThat().resideInAPackage("sn.sunagri.commercialisation..")
                    .allowEmptyShould(true).because("les contextes sont découplés par des ports");

    @ArchTest
    static final ArchRule entrepot_ne_depend_pas_de_commercialisation =
            noClasses().that().resideInAPackage("sn.sunagri.entrepot..")
                    .should().dependOnClassesThat().resideInAPackage("sn.sunagri.commercialisation..")
                    .allowEmptyShould(true).because("les contextes sont découplés par des ports");
}
