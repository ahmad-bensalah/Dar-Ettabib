package ma.fss.service;

import ma.fss.entities.*;
import ma.fss.repositories.ConsultationRepository;
import ma.fss.repositories.MedecinRepository;
import ma.fss.repositories.PatientRepository;
import ma.fss.repositories.RendezVousRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour IHospitalServiceImpl.
 * Utilise Mockito pour mocker les repositories (pas de base de données).
 */
@ExtendWith(MockitoExtension.class)
class IHospitalServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedecinRepository medecinRepository;
    @Mock
    private RendezVousRepository rendezVousRepository;
    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private IHospitalServiceImpl hospitalService;

    private Patient testPatient;
    private Medecin testMedecin;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setNom("Ahmed");
        testPatient.setDateNaissance(new Date());
        testPatient.setMalade(false);

        testMedecin = new Medecin();
        testMedecin.setId(1L);
        testMedecin.setNom("Dr. Hassan");
        testMedecin.setSpecialite("Cardiologie");
    }

    // ========== Tests pour savePatient ==========

    @Test
    @DisplayName("Doit sauvegarder un patient avec succès")
    void shouldSavePatient() {
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        Patient result = hospitalService.savePatient(testPatient);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Ahmed");
        verify(patientRepository, times(1)).save(testPatient);
    }

    // ========== Tests pour saveMedecin ==========

    @Test
    @DisplayName("Doit sauvegarder un médecin avec succès")
    void shouldSaveMedecin() {
        when(medecinRepository.save(any(Medecin.class))).thenReturn(testMedecin);

        Medecin result = hospitalService.saveMedecin(testMedecin);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Dr. Hassan");
        assertThat(result.getSpecialite()).isEqualTo("Cardiologie");
        verify(medecinRepository, times(1)).save(testMedecin);
    }

    // ========== Tests pour saveRendezVous ==========

    @Test
    @DisplayName("Doit sauvegarder un rendez-vous avec patient et médecin existants")
    void shouldSaveRendezVousWithExistingPatientAndMedecin() {
        RendezVous rdv = new RendezVous();
        rdv.setDate(new Date());
        rdv.setStatusRDV(StatusRDV.PENDING);
        rdv.setPatient(testPatient);
        rdv.setMedecin(testMedecin);

        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rdv);

        RendezVous result = hospitalService.saveRendezVous(rdv);

        assertThat(result).isNotNull();
        assertThat(result.getPatient().getNom()).isEqualTo("Ahmed");
        assertThat(result.getMedecin().getNom()).isEqualTo("Dr. Hassan");
    }

    @Test
    @DisplayName("Doit assigner un patient par défaut si aucun n'est fourni")
    void shouldAssignDefaultPatientWhenNoneProvided() {
        RendezVous rdv = new RendezVous();
        rdv.setDate(new Date());
        rdv.setStatusRDV(StatusRDV.PENDING);
        rdv.setPatient(null);
        rdv.setMedecin(testMedecin);

        when(patientRepository.findAll()).thenReturn(Arrays.asList(testPatient));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rdv);

        hospitalService.saveRendezVous(rdv);

        assertThat(rdv.getPatient()).isEqualTo(testPatient);
    }

    @Test
    @DisplayName("Doit lever une exception si aucun patient n'existe")
    void shouldThrowExceptionWhenNoPatientExists() {
        RendezVous rdv = new RendezVous();
        rdv.setPatient(null);
        rdv.setMedecin(testMedecin);

        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> hospitalService.saveRendezVous(rdv))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("patient");
    }

    @Test
    @DisplayName("Doit lever une exception si aucun médecin n'existe")
    void shouldThrowExceptionWhenNoMedecinExists() {
        RendezVous rdv = new RendezVous();
        rdv.setPatient(testPatient);
        rdv.setMedecin(null);

        when(medecinRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> hospitalService.saveRendezVous(rdv))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("médecin");
    }

    // ========== Tests pour saveConsultation ==========

    @Test
    @DisplayName("Doit sauvegarder une consultation avec un rendez-vous existant")
    void shouldSaveConsultationWithExistingRendezVous() {
        RendezVous rdv = new RendezVous();
        rdv.setId(1L);

        Consultation consultation = new Consultation();
        consultation.setDateConsultation(new Date());
        consultation.setRapport("Patient en bonne santé");
        consultation.setRendezVous(rdv);

        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

        Consultation result = hospitalService.saveConsultation(consultation);

        assertThat(result).isNotNull();
        assertThat(result.getRapport()).isEqualTo("Patient en bonne santé");
        verify(consultationRepository, times(1)).save(consultation);
    }

    @Test
    @DisplayName("Doit lever une exception si aucun rendez-vous n'existe pour la consultation")
    void shouldThrowExceptionWhenNoRendezVousExists() {
        Consultation consultation = new Consultation();
        consultation.setRendezVous(null);

        when(rendezVousRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> hospitalService.saveConsultation(consultation))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("rendez-vous");
    }
}
