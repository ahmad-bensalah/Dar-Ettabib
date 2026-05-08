package ma.fss.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.fss.entities.Patient;
import ma.fss.entities.Titre;
import ma.fss.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur REST PatientController.
 * Utilise MockitoExtension + MockMvc standalone pour tester la couche web isolément.
 */
@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    // ========== Tests GET ==========

    @Test
    @DisplayName("GET /api/patients - Doit retourner la liste paginée des patients")
    void shouldReturnPaginatedPatients() throws Exception {
        Patient p1 = new Patient();
        p1.setId(1L);
        p1.setNom("Ahmed");
        p1.setDateNaissance(new Date());
        p1.setTitre(Titre.Mr);

        Patient p2 = new Patient();
        p2.setId(2L);
        p2.setNom("Fatima");
        p2.setDateNaissance(new Date());
        p2.setTitre(Titre.MMe);

        Page<Patient> page = new PageImpl<>(Arrays.asList(p1, p2), PageRequest.of(0, 5), 2);
        when(patientRepository.findByNomContains(anyString(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/patients")
                        .param("page", "0")
                        .param("size", "5")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nom", is("Ahmed")))
                .andExpect(jsonPath("$.content[1].nom", is("Fatima")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Doit retourner un patient par ID")
    void shouldReturnPatientById() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setNom("Ahmed");
        patient.setAdresse("Casablanca");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("Ahmed")))
                .andExpect(jsonPath("$.adresse", is("Casablanca")));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Doit retourner 404 si patient inexistant")
    void shouldReturn404WhenPatientNotFound() throws Exception {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/999"))
                .andExpect(status().isNotFound());
    }

    // ========== Tests POST ==========

    @Test
    @DisplayName("POST /api/patients - Doit créer un nouveau patient")
    void shouldCreateNewPatient() throws Exception {
        Patient saved = new Patient();
        saved.setId(10L);
        saved.setNom("Nouveau Patient");
        saved.setMalade(false);
        saved.setTitre(Titre.Mr);

        when(patientRepository.save(any(Patient.class))).thenReturn(saved);

        Patient input = new Patient();
        input.setNom("Nouveau Patient");
        input.setDateNaissance(new Date());
        input.setMalade(false);
        input.setTitre(Titre.Mr);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.nom", is("Nouveau Patient")));
    }

    // ========== Tests DELETE ==========

    @Test
    @DisplayName("DELETE /api/patients/{id} - Doit supprimer un patient existant")
    void shouldDeleteExistingPatient() throws Exception {
        when(patientRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());

        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} - Doit retourner 404 si patient inexistant")
    void shouldReturn404WhenDeletingNonExistentPatient() throws Exception {
        when(patientRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/patients/999"))
                .andExpect(status().isNotFound());

        verify(patientRepository, never()).deleteById(999L);
    }

    // ========== Tests PUT ==========

    @Test
    @DisplayName("PUT /api/patients/{id} - Doit mettre à jour un patient")
    void shouldUpdatePatient() throws Exception {
        Patient updated = new Patient();
        updated.setId(1L);
        updated.setNom("Ahmed Modifié");
        updated.setDateNaissance(new Date());
        updated.setMalade(true);
        updated.setTitre(Titre.Mr);

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.save(any(Patient.class))).thenReturn(updated);

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("Ahmed Modifié")))
                .andExpect(jsonPath("$.malade", is(true)));
    }
}
