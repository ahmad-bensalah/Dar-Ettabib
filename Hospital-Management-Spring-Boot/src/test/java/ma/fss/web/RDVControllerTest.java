package ma.fss.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.fss.entities.*;
import ma.fss.repositories.RendezVousRepository;
import ma.fss.service.IHopitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur Rendez-vous.
 * Vérifie notamment la détection de conflits d'horaires.
 */
@ExtendWith(MockitoExtension.class)
class RDVControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RendezVousRepository rendezVousRepository;
    @Mock
    private IHopitalService hopitalService;

    @InjectMocks
    private RDVController rdvController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rdvController).build();
    }

    @Test
    @DisplayName("GET /api/rendezvous/patient/{username} - Doit retourner les RDV d'un patient")
    void shouldReturnRendezVousByPatient() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setNom("Ahmed");

        Medecin medecin = new Medecin();
        medecin.setId(1L);
        medecin.setNom("Dr. Hassan");

        RendezVous rdv = new RendezVous();
        rdv.setId(1L);
        rdv.setDate(new Date());
        rdv.setTime("10:00");
        rdv.setStatusRDV(StatusRDV.PENDING);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);

        when(rendezVousRepository.findByPatientUsername("ahmed")).thenReturn(Arrays.asList(rdv));

        mockMvc.perform(get("/api/rendezvous/patient/ahmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].time", is("10:00")))
                .andExpect(jsonPath("$[0].statusRDV", is("PENDING")));
    }

    @Test
    @DisplayName("POST /api/rendezvous - Doit détecter un conflit d'horaire (< 15 min)")
    void shouldDetectSchedulingConflict() throws Exception {
        Medecin medecin = new Medecin();
        medecin.setId(1L);
        medecin.setNom("Dr. Hassan");

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setNom("Ahmed");

        Date today = new Date();

        RendezVous existingRdv = new RendezVous();
        existingRdv.setId(1L);
        existingRdv.setDate(today);
        existingRdv.setTime("10:00");
        existingRdv.setStatusRDV(StatusRDV.PENDING);
        existingRdv.setMedecin(medecin);
        existingRdv.setPatient(patient);

        when(rendezVousRepository.findByMedecinId(1L)).thenReturn(Arrays.asList(existingRdv));

        RendezVous newRdv = new RendezVous();
        newRdv.setDate(today);
        newRdv.setTime("10:10");
        newRdv.setStatusRDV(StatusRDV.PENDING);
        newRdv.setMedecin(medecin);
        newRdv.setPatient(patient);

        mockMvc.perform(post("/api/rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRdv)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("15 min")));
    }

    @Test
    @DisplayName("POST /api/rendezvous - Doit accepter un RDV sans conflit")
    void shouldAcceptNonConflictingAppointment() throws Exception {
        Medecin medecin = new Medecin();
        medecin.setId(1L);
        medecin.setNom("Dr. Hassan");

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setNom("Ahmed");

        Date today = new Date();

        RendezVous existingRdv = new RendezVous();
        existingRdv.setId(1L);
        existingRdv.setDate(today);
        existingRdv.setTime("10:00");
        existingRdv.setStatusRDV(StatusRDV.PENDING);
        existingRdv.setMedecin(medecin);
        existingRdv.setPatient(patient);

        when(rendezVousRepository.findByMedecinId(1L)).thenReturn(Arrays.asList(existingRdv));

        RendezVous newRdv = new RendezVous();
        newRdv.setDate(today);
        newRdv.setTime("11:00");
        newRdv.setStatusRDV(StatusRDV.PENDING);
        newRdv.setMedecin(medecin);
        newRdv.setPatient(patient);

        RendezVous savedRdv = new RendezVous();
        savedRdv.setId(2L);
        savedRdv.setDate(today);
        savedRdv.setTime("11:00");
        savedRdv.setStatusRDV(StatusRDV.PENDING);
        savedRdv.setMedecin(medecin);
        savedRdv.setPatient(patient);

        when(hopitalService.saveRendezVous(any(RendezVous.class))).thenReturn(savedRdv);

        mockMvc.perform(post("/api/rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRdv)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time", is("11:00")));
    }
}
