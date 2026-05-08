package ma.fss.repositories;

import ma.fss.entities.Medecin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour MedecinRepository.
 * Vérifie les opérations CRUD et les requêtes personnalisées.
 */
@DataJpaTest
@ContextConfiguration(classes = ma.fss.config.TestApplication.class)
class MedecinRepositoryTest {

    @Autowired
    private MedecinRepository medecinRepository;

    private Medecin medecin1;

    @BeforeEach
    void setUp() {
        medecinRepository.deleteAll();

        medecin1 = new Medecin();
        medecin1.setUsername("dr_hassan");
        medecin1.setNom("Dr. Hassan");
        medecin1.setEmail("hassan@hospital.ma");
        medecin1.setSpecialite("Cardiologie");
        medecin1 = medecinRepository.save(medecin1);

        Medecin medecin2 = new Medecin();
        medecin2.setUsername("dr_amina");
        medecin2.setNom("Dr. Amina");
        medecin2.setEmail("amina@hospital.ma");
        medecin2.setSpecialite("Pédiatrie");
        medecinRepository.save(medecin2);
    }

    @Test
    @DisplayName("Doit trouver un médecin par son username")
    void shouldFindByUsername() {
        Medecin found = medecinRepository.findByUsername("dr_hassan");

        assertThat(found).isNotNull();
        assertThat(found.getNom()).isEqualTo("Dr. Hassan");
        assertThat(found.getSpecialite()).isEqualTo("Cardiologie");
    }

    @Test
    @DisplayName("Doit retourner null pour un username inexistant")
    void shouldReturnNullForUnknownUsername() {
        Medecin found = medecinRepository.findByUsername("dr_inconnu");

        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Doit trouver les médecins par nom (contient)")
    void shouldFindByNomContains() {
        Page<Medecin> result = medecinRepository.findByNomContains("Hassan", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("hassan@hospital.ma");
    }

    @Test
    @DisplayName("Doit retourner tous les médecins")
    void shouldReturnAllMedecins() {
        assertThat(medecinRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Doit mettre à jour un médecin")
    void shouldUpdateMedecin() {
        medecin1.setSpecialite("Neurologie");
        medecinRepository.save(medecin1);

        Medecin updated = medecinRepository.findByUsername("dr_hassan");
        assertThat(updated.getSpecialite()).isEqualTo("Neurologie");
    }
}
