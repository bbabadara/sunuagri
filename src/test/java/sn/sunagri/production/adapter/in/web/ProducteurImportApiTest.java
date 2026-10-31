package sn.sunagri.production.adapter.in.web;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProducteurImportApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void importe_un_fichier_csv_et_rapporte_les_rejets() throws Exception {
        String csv = """
                nom;prénom;téléphone;village
                Diop;Awa;77 000 00 01;Thiès
                Invalide;Trois;colonnes
                """;
        MockMultipartFile fichier = new MockMultipartFile("fichier", "producteurs.csv", "text/csv",
                csv.getBytes(UTF_8));

        mvc.perform(multipart("/api/v1/producteurs/import")
                        .file(fichier)
                        .header("X-User-Id", "agent-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLignes").value(2))
                .andExpect(jsonPath("$.acceptees").value(1))
                .andExpect(jsonPath("$.refusees").value(1))
                .andExpect(jsonPath("$.producteurs[0].nom").value("Diop"))
                .andExpect(jsonPath("$.producteurs[0].telephone").value("770000001"))
                .andExpect(jsonPath("$.rejets[0].ligne").value(3));
    }

    @Test
    void refuse_un_fichier_vide() throws Exception {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "vide.csv", "text/csv", new byte[0]);

        mvc.perform(multipart("/api/v1/producteurs/import")
                        .file(fichier)
                        .header("X-User-Id", "agent-1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("FICHIER_VIDE"));
    }

    @Test
    void refuse_un_import_par_un_producteur() throws Exception {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "producteurs.csv", "text/csv",
                "Diop;Awa;770000001;Thiès".getBytes(UTF_8));

        mvc.perform(multipart("/api/v1/producteurs/import")
                        .file(fichier)
                        .header("X-User-Id", "producteur:00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refuse_une_requete_sans_fichier() throws Exception {
        mvc.perform(multipart("/api/v1/producteurs/import")
                        .header("X-User-Id", "agent-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }
}
