package ma.fss.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.fss.repositories.MedecinRepository;
import ma.fss.repositories.PatientRepository;
import ma.fss.security.entities.AppRole;
import ma.fss.security.entities.AppUser;
import ma.fss.security.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur d'authentification.
 * Vérifie le login, l'inscription et la gestion des erreurs.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SecurityService securityService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedecinRepository medecinRepository;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // ========== Tests Login ==========

    @Test
    @DisplayName("POST /api/auth/login - Doit authentifier un utilisateur valide")
    void shouldLoginSuccessfully() throws Exception {
        AppRole adminRole = new AppRole(1L, "ADMIN", "Administrator");
        AppUser user = new AppUser();
        user.setUserId("uuid-1");
        user.setUsername("admin");
        user.setPassword("$2a$10$hashedPassword");
        user.setActive(true);
        user.setAppRoles(Arrays.asList(adminRole));

        when(securityService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("admin123", "$2a$10$hashedPassword")).thenReturn(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.roles", hasItem("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Doit rejeter un mot de passe invalide")
    void shouldRejectInvalidPassword() throws Exception {
        AppUser user = new AppUser();
        user.setUserId("uuid-1");
        user.setUsername("admin");
        user.setPassword("$2a$10$hashedPassword");
        user.setActive(true);
        user.setAppRoles(new ArrayList<>());

        when(securityService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Invalid")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Doit rejeter un utilisateur inexistant")
    void shouldRejectUnknownUser() throws Exception {
        when(securityService.loadUserByUsername("inconnu")).thenReturn(null);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "inconnu");
        credentials.put("password", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Doit rejeter un compte désactivé")
    void shouldRejectDisabledAccount() throws Exception {
        AppUser user = new AppUser();
        user.setUserId("uuid-1");
        user.setUsername("disabled");
        user.setPassword("$2a$10$hashedPassword");
        user.setActive(false);
        user.setAppRoles(new ArrayList<>());

        when(securityService.loadUserByUsername("disabled")).thenReturn(user);
        when(passwordEncoder.matches("password", "$2a$10$hashedPassword")).thenReturn(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "disabled");
        credentials.put("password", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("disabled")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Doit rejeter les champs manquants")
    void shouldRejectMissingFields() throws Exception {
        Map<String, String> credentials = new HashMap<>();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest());
    }

    // ========== Tests Signup ==========

    @Test
    @DisplayName("POST /api/auth/signup/patient - Doit rejeter un username existant")
    void shouldRejectDuplicateUsername() throws Exception {
        AppUser existing = new AppUser();
        existing.setUsername("existinguser");
        when(securityService.loadUserByUsername("existinguser")).thenReturn(existing);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "existinguser");
        payload.put("password", "password");
        payload.put("nom", "Test");

        mockMvc.perform(post("/api/auth/signup/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("exists")));
    }
}
