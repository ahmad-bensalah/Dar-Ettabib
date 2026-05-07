package ma.fss.web;

import lombok.AllArgsConstructor;
import ma.fss.entities.Medecin;
import ma.fss.entities.Patient;
import ma.fss.repositories.MedecinRepository;
import ma.fss.repositories.PatientRepository;
import ma.fss.security.entities.AppUser;
import ma.fss.security.service.SecurityService;
import ma.fss.web.dto.SignupDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private SecurityService securityService;
    private PasswordEncoder passwordEncoder;
    private PatientRepository patientRepository;
    private MedecinRepository medecinRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Username and password are required"));
        }

        AppUser user = securityService.loadUserByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Account is disabled"));
        }

        List<String> roles = user.getAppRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("roles", roles);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/patient")
    public ResponseEntity<?> signupPatient(@RequestBody SignupDto dto) {
        try {
            if (securityService.loadUserByUsername(dto.getUsername()) != null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Username already exists"));
            }

            // Create user
            securityService.saveNewUser(dto.getUsername(), dto.getPassword(), dto.getPassword());
            securityService.addRoleToUser(dto.getUsername(), "PATIENT");

            // Create patient
            Patient patient = new Patient();
            patient.setUsername(dto.getUsername());
            patient.setNom(dto.getNom());
            patient.setDateNaissance(dto.getDateNaissance());
            patient.setMalade(dto.isMalade());
            patient.setAdresse(dto.getAdresse());
            patient.setCodePostal(dto.getCodePostal());
            patient.setNumeroTelephone(dto.getNumeroTelephone());
            patient.setTitre(dto.getTitre());
            patientRepository.save(patient);

            return ResponseEntity.ok(Collections.singletonMap("message", "Patient registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signup/doctor")
    public ResponseEntity<?> signupDoctor(@RequestBody SignupDto dto) {
        try {
            if (securityService.loadUserByUsername(dto.getUsername()) != null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Username already exists"));
            }

            // Create user
            securityService.saveNewUser(dto.getUsername(), dto.getPassword(), dto.getPassword());
            securityService.addRoleToUser(dto.getUsername(), "DOCTOR");

            // Create doctor
            Medecin medecin = new Medecin();
            medecin.setUsername(dto.getUsername());
            medecin.setNom(dto.getNom());
            medecin.setEmail(dto.getEmail());
            medecin.setSpecialite(dto.getSpecialite());
            medecinRepository.save(medecin);

            return ResponseEntity.ok(Collections.singletonMap("message", "Doctor registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Registration failed: " + e.getMessage()));
        }
    }
}
