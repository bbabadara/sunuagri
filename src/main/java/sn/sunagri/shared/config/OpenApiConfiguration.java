package sn.sunagri.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    static final String SCHEMA_IDENTITE = "identiteSimulee";

    @Bean
    public OpenAPI sunuAgriOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SunuAgri API")
                        .version("0.5.1")
                        .description("""
                                API de la coopérative agricole SunuAgri : producteurs, parcelles,
                                campagnes, cultures, récoltes, stock, offres, commandes et paiements.

                                L'identité est simulée par l'en-tête `X-User-Id`. Utilisez le bouton
                                Authorize avec l'une des valeurs suivantes : `agent-1`, `admin-1`,
                                `acheteur:<identifiant>` ou `producteur:<uuid>`.
                                """))
                .addSecurityItem(new SecurityRequirement().addList(SCHEMA_IDENTITE))
                .components(new Components().addSecuritySchemes(SCHEMA_IDENTITE,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("Identité simulée de l'appelant.")));
    }

    @Bean
    public OperationCustomizer masquerEnteteIdentite() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().removeIf(parameter -> "header".equalsIgnoreCase(parameter.getIn())
                        && "X-User-Id".equalsIgnoreCase(parameter.getName()));
            }
            return operation;
        };
    }
}
