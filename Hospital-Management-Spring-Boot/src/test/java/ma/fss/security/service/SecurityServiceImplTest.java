package ma.fss.security.service;

import ma.fss.security.entities.AppRole;
import ma.fss.security.entities.AppUser;
import ma.fss.security.repositories.AppRoleRepository;
import ma.fss.security.repositories.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SecurityServiceImpl.
 * Vérifie la création d'utilisateurs, l'assignation de rôles et le hachage des mots de passe.
 */
@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AppRoleRepository appRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecurityServiceImpl securityService;

    private AppUser testUser;
    private AppRole testRole;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setUserId("test-uuid");
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setActive(true);
        testUser.setAppRoles(new ArrayList<>());

        testRole = new AppRole();
        testRole.setRoleId(1L);
        testRole.setRoleName("ADMIN");
        testRole.setDescription("Administrator");
    }

    // ========== Tests pour saveNewUser ==========

    @Test
    @DisplayName("Doit créer un nouvel utilisateur avec mot de passe haché")
    void shouldCreateNewUserWithHashedPassword() {
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedValue");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = securityService.saveNewUser("newuser", "password123", "password123");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPassword()).isEqualTo("$2a$10$hashedValue");
        assertThat(result.isActive()).isTrue();
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("Doit lever une exception si les mots de passe ne correspondent pas")
    void shouldThrowExceptionWhenPasswordsDontMatch() {
        assertThatThrownBy(() ->
                securityService.saveNewUser("user", "password1", "password2"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("correspond");
    }

    // ========== Tests pour saveNewRole ==========

    @Test
    @DisplayName("Doit créer un nouveau rôle")
    void shouldCreateNewRole() {
        when(appRoleRepository.findByRoleName("DOCTOR")).thenReturn(null);
        when(appRoleRepository.save(any(AppRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppRole result = securityService.saveNewRole("DOCTOR", "Doctor role");

        assertThat(result).isNotNull();
        assertThat(result.getRoleName()).isEqualTo("DOCTOR");
        assertThat(result.getDescription()).isEqualTo("Doctor role");
    }

    @Test
    @DisplayName("Doit lever une exception si le rôle existe déjà")
    void shouldThrowExceptionWhenRoleAlreadyExists() {
        when(appRoleRepository.findByRoleName("ADMIN")).thenReturn(testRole);

        assertThatThrownBy(() ->
                securityService.saveNewRole("ADMIN", "Administrator"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("exist");
    }

    // ========== Tests pour addRoleToUser ==========

    @Test
    @DisplayName("Doit ajouter un rôle à un utilisateur")
    void shouldAddRoleToUser() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(testUser);
        when(appRoleRepository.findByRoleName("ADMIN")).thenReturn(testRole);

        securityService.addRoleToUser("testuser", "ADMIN");

        assertThat(testUser.getAppRoles()).contains(testRole);
        verify(appUserRepository).save(testUser);
    }

    @Test
    @DisplayName("Doit lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
        when(appUserRepository.findByUsername("inconnu")).thenReturn(null);

        assertThatThrownBy(() ->
                securityService.addRoleToUser("inconnu", "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Doit lever une exception si le rôle n'existe pas")
    void shouldThrowExceptionWhenRoleNotFoundForUser() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(testUser);
        when(appRoleRepository.findByRoleName("FAKE")).thenReturn(null);

        assertThatThrownBy(() ->
                securityService.addRoleToUser("testuser", "FAKE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");
    }

    // ========== Tests pour removeRoleFromUser ==========

    @Test
    @DisplayName("Doit retirer un rôle d'un utilisateur")
    void shouldRemoveRoleFromUser() {
        testUser.getAppRoles().add(testRole);
        when(appUserRepository.findByUsername("testuser")).thenReturn(testUser);
        when(appRoleRepository.findByRoleName("ADMIN")).thenReturn(testRole);

        securityService.removeRoleFromUser("testuser", "ADMIN");

        assertThat(testUser.getAppRoles()).doesNotContain(testRole);
    }

    // ========== Tests pour loadUserByUsername ==========

    @Test
    @DisplayName("Doit charger un utilisateur par son username")
    void shouldLoadUserByUsername() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(testUser);

        AppUser result = securityService.loadUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Doit retourner null si l'utilisateur n'existe pas")
    void shouldReturnNullForUnknownUser() {
        when(appUserRepository.findByUsername("inconnu")).thenReturn(null);

        AppUser result = securityService.loadUserByUsername("inconnu");

        assertThat(result).isNull();
    }
}
