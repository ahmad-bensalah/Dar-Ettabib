package ma.fss.repositories;

import ma.fss.entities.Patient;
import ma.fss.entities.Titre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour PatientRepository.
 * Utilise @DataJpaTest qui configure automatiquement une base H2 en mémoire.
 */
@DataJpaTest
@ContextConfiguration(classes = ma.fss.config.TestApplication.class)
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

        patient1 = new Patient();
        patient1.setUsername("ahmed");
        patient1.setNom("Ahmed Bensalah");
        patient1.setDateNaissance(new Date());
        patient1.setMalade(false);
        patient1.setAdresse("Casablanca");
        patient1.setCodePostal("20000");
        patient1.setNumeroTelephone("0612345678");
        patient1.setTitre(Titre.Mr);
        patient1 = patientRepository.save(patient1);

        patient2 = new Patient();
        patient2.setUsername("fatima");
        patient2.setNom("Fatima Zahra");
        patient2.setDateNaissance(new Date());
        patient2.setMalade(true);
        patient2.setAdresse("Rabat");
        patient2.setCodePostal("10000");
        patient2.setNumeroTelephone("0698765432");
        patient2.setTitre(Titre.MMe);
        patient2 = patientRepository.save(patient2);
    }

    @Test
    @DisplayName("Doit trouver un patient par son username")
    void shouldFindPatientByUsername() {
        Patient found = patientRepository.findByUsername("ahmed");

        assertThat(found).isNotNull();
        assertThat(found.getNom()).isEqualTo("Ahmed Bensalah");
        assertThat(found.getAdresse()).isEqualTo("Casablanca");
    }

    @Test
    @DisplayName("Doit retourner null pour un username inexistant")
    void shouldReturnNullForUnknownUsername() {
        Patient found = patientRepository.findByUsername("inconnu");

        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Doit trouver les patients par nom (contient)")
    void shouldFindByNomContains() {
        Page<Patient> result = patientRepository.findByNomContains("Ahmed", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Ahmed Bensalah");
    }

    @Test
    @DisplayName("Doit retourner tous les patients si le mot-clé est vide")
    void shouldReturnAllPatientsForEmptyKeyword() {
        Page<Patient> result = patientRepository.findByNomContains("", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Doit paginer correctement les résultats")
    void shouldPaginateResults() {
        Page<Patient> page1 = patientRepository.findByNomContains("", PageRequest.of(0, 1));
        Page<Patient> page2 = patientRepository.findByNomContains("", PageRequest.of(1, 1));

        assertThat(page1.getContent()).hasSize(1);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page1.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Doit sauvegarder et retrouver un patient par ID")
    void shouldSaveAndFindById() {
        Patient newPatient = new Patient();
        newPatient.setNom("Youssef Test");
        newPatient.setDateNaissance(new Date());
        newPatient.setMalade(false);
        newPatient.setTitre(Titre.Mr);

        Patient saved = patientRepository.save(newPatient);

        assertThat(saved.getId()).isNotNull();
        assertThat(patientRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("Doit supprimer un patient par ID")
    void shouldDeletePatientById() {
        Long id = patient1.getId();
        patientRepository.deleteById(id);

        assertThat(patientRepository.findById(id)).isEmpty();
        assertThat(patientRepository.count()).isEqualTo(1);
    }
}
