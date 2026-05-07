package ma.fss.web;

import lombok.AllArgsConstructor;
import ma.fss.entities.Medecin;
import ma.fss.repositories.MedecinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api/medecins")
public class MedecinController {
    private MedecinRepository medecinRepository;

    @GetMapping("/all")
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<Medecin> getMedecinByUsername(@PathVariable String username) {
        Medecin m = medecinRepository.findByUsername(username);
        return m != null ? ResponseEntity.ok(m) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public Page<Medecin> getMedecins(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return medecinRepository.findByNomContains(keyword, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medecin> getMedecin(@PathVariable Long id) {
        return medecinRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Medecin createMedecin(@Valid @RequestBody Medecin medecin) {
        medecin.setId(null);
        return medecinRepository.save(medecin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medecin> updateMedecin(@PathVariable Long id, @Valid @RequestBody Medecin medecin) {
        if (!medecinRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        medecin.setId(id);
        return ResponseEntity.ok(medecinRepository.save(medecin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedecin(@PathVariable Long id) {
        if (!medecinRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        medecinRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
