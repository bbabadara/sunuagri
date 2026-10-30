package sn.sunagri.shared.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import sn.sunagri.shared.domain.AccesRefuseException;
import sn.sunagri.shared.domain.AuthentificationException;
import sn.sunagri.shared.domain.ConflitException;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RegleMetierException.class)
    public ResponseEntity<ProblemDetail> regleMetier(RegleMetierException ex, HttpServletRequest requete) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCode(), ex.getMessage(), requete);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ProblemDetail> introuvable(RessourceIntrouvableException ex, HttpServletRequest requete) {
        return reponse(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), requete);
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ProblemDetail> conflit(ConflitException ex, HttpServletRequest requete) {
        return reponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), requete);
    }

    @ExceptionHandler(AuthentificationException.class)
    public ResponseEntity<ProblemDetail> nonAuthentifie(AuthentificationException ex, HttpServletRequest requete) {
        return reponse(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessage(), requete);
    }

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<ProblemDetail> accesRefuse(AccesRefuseException ex, HttpServletRequest requete) {
        return reponse(HttpStatus.FORBIDDEN, ex.getCode(), ex.getMessage(), requete);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex, HttpServletRequest requete) {
        ProblemDetail probleme = probleme(HttpStatus.BAD_REQUEST, "VALIDATION",
                "La requête est invalide.", requete);
        List<Map<String, String>> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .map(erreur -> Map.of(
                        "champ", erreur.getField(),
                        "message", erreur.getDefaultMessage() == null ? "valeur invalide" : erreur.getDefaultMessage()))
                .toList();
        probleme.setProperty("erreurs", erreurs);
        return ResponseEntity.badRequest().body(probleme);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> contrainte(ConstraintViolationException ex, HttpServletRequest requete) {
        ProblemDetail probleme = probleme(HttpStatus.BAD_REQUEST, "VALIDATION",
                "La requête est invalide.", requete);
        List<Map<String, String>> erreurs = ex.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "champ", violation.getPropertyPath().toString(),
                        "message", violation.getMessage()))
                .toList();
        probleme.setProperty("erreurs", erreurs);
        return ResponseEntity.badRequest().body(probleme);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ProblemDetail> requeteInvalide(Exception ex, HttpServletRequest requete) {
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE",
                "La requête est mal formée ou contient une valeur invalide.", requete);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> fichierTropVolumineux(MaxUploadSizeExceededException ex,
            HttpServletRequest requete) {
        return reponse(HttpStatus.PAYLOAD_TOO_LARGE, "FICHIER_TROP_VOLUMINEUX",
                "Le fichier envoyé est trop volumineux.", requete);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> erreurInterne(Exception ex, HttpServletRequest requete) {
        log.error("Erreur non gérée sur {} {}", requete.getMethod(), requete.getRequestURI(), ex);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE",
                "Une erreur interne est survenue.", requete);
    }

    private ResponseEntity<ProblemDetail> reponse(HttpStatus statut, String code, String detail,
            HttpServletRequest requete) {
        return ResponseEntity.status(statut).body(probleme(statut, code, detail, requete));
    }

    private ProblemDetail probleme(HttpStatus statut, String code, String detail, HttpServletRequest requete) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setProperty("code", code);
        probleme.setInstance(URI.create(requete.getRequestURI()));
        return probleme;
    }
}
