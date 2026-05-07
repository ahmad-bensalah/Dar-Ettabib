package ma.fss.web.dto;

import lombok.Data;
import java.util.Date;
import ma.fss.entities.Titre;

@Data
public class SignupDto {
    private String username;
    private String password;
    
    // Shared
    private String nom;

    // Medecin specific
    private String email;
    private String specialite;
    
    // Patient specific
    private Date dateNaissance;
    private boolean malade;
    private String adresse;
    private String codePostal;
    private String numeroTelephone;
    private Titre titre;
}
