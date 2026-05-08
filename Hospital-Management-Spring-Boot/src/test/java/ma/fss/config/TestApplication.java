package ma.fss.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Classe de configuration Spring Boot dédiée aux tests.
 * Exclut SecurityAutoConfiguration pour éviter le chargement du CommandLineRunner (saveUsers)
 * de PatientApplication qui nécessite SecurityService.
 */
@SpringBootApplication(
    scanBasePackages = {"ma.fss.entities", "ma.fss.repositories", "ma.fss.service", "ma.fss.web"},
    exclude = {SecurityAutoConfiguration.class}
)
@EntityScan("ma.fss.entities")
@EnableJpaRepositories("ma.fss.repositories")
public class TestApplication {
}
