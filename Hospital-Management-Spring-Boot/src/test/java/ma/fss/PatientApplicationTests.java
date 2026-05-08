package ma.fss;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de base pour vérifier que le framework de test fonctionne.
 */
class PatientApplicationTests {

	@Test
	void applicationClassExists() {
		// Vérifie que la classe principale existe et peut être instanciée
		PatientApplication app = new PatientApplication();
		assertThat(app).isNotNull();
	}

}
