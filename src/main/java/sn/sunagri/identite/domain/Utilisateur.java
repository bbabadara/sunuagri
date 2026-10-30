package sn.sunagri.identite.domain;

public record Utilisateur(String id, Role role, String producteurId, String acheteurId) {

    public boolean estAgentOuAdmin() {
        return role == Role.AGENT || role == Role.ADMIN;
    }
}
