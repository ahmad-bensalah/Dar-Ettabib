package ma.fss.web;

import lombok.AllArgsConstructor;
import ma.fss.entities.RendezVous;
import ma.fss.repositories.RendezVousRepository;
import ma.fss.service.IHopitalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/rendezvous")
public class RDVController {
    private RendezVousRepository rendezVousRepository;
    private IHopitalService hopitalService;

    @GetMapping("/patient/{username}")
    public List<RendezVous> getRendezVousByPatient(@PathVariable String username) {
        return rendezVousRepository.findByPatientUsername(username);
    }

    @GetMapping("/doctor/{username}")
    public List<RendezVous> getRendezVousByDoctor(@PathVariable String username) {
        return rendezVousRepository.findByMedecinUsername(username);
    }

    @GetMapping
    public Page<RendezVous> getRendezVous(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        return rendezVousRepository.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVous> getRendezVousById(@PathVariable Long id) {
        return rendezVousRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRendezVous(@Valid @RequestBody RendezVous rendezVous) {
        if (rendezVous.getTime() != null && rendezVous.getMedecin() != null && rendezVous.getMedecin().getId() != null && rendezVous.getDate() != null && rendezVous.getStatusRDV() != ma.fss.entities.StatusRDV.CANCELED) {
            List<RendezVous> existing = rendezVousRepository.findByMedecinId(rendezVous.getMedecin().getId());
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String newDateStr = fmt.format(rendezVous.getDate());

            try {
                String[] newTime = rendezVous.getTime().split(":");
                int newMins = Integer.parseInt(newTime[0]) * 60 + Integer.parseInt(newTime[1]);

                for (RendezVous r : existing) {
                    if (r.getStatusRDV() != ma.fss.entities.StatusRDV.CANCELED && r.getTime() != null && r.getDate() != null && fmt.format(r.getDate()).equals(newDateStr)) {
                        String[] oldTime = r.getTime().split(":");
                        int oldMins = Integer.parseInt(oldTime[0]) * 60 + Integer.parseInt(oldTime[1]);
                        if (Math.abs(newMins - oldMins) < 15) {
                            return ResponseEntity.badRequest()
                                .body(java.util.Collections.singletonMap("message", "Le médecin a déjà un rendez-vous à une heure trop proche (moins de 15 min)."));
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors, let default save proceed
            }
        }
        rendezVous.setId(null);
        return ResponseEntity.ok(hopitalService.saveRendezVous(rendezVous));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRendezVous(@PathVariable Long id, @Valid @RequestBody RendezVous rendezVous) {
        if (!rendezVousRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (rendezVous.getTime() != null && rendezVous.getMedecin() != null && rendezVous.getMedecin().getId() != null && rendezVous.getDate() != null && rendezVous.getStatusRDV() != ma.fss.entities.StatusRDV.CANCELED) {
            List<RendezVous> existing = rendezVousRepository.findByMedecinId(rendezVous.getMedecin().getId());
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String newDateStr = fmt.format(rendezVous.getDate());

            try {
                String[] newTime = rendezVous.getTime().split(":");
                int newMins = Integer.parseInt(newTime[0]) * 60 + Integer.parseInt(newTime[1]);

                for (RendezVous r : existing) {
                    if (!r.getId().equals(id) && r.getStatusRDV() != ma.fss.entities.StatusRDV.CANCELED && r.getTime() != null && r.getDate() != null && fmt.format(r.getDate()).equals(newDateStr)) {
                        String[] oldTime = r.getTime().split(":");
                        int oldMins = Integer.parseInt(oldTime[0]) * 60 + Integer.parseInt(oldTime[1]);
                        if (Math.abs(newMins - oldMins) < 15) {
                            return ResponseEntity.badRequest()
                                .body(java.util.Collections.singletonMap("message", "Le médecin a déjà un rendez-vous à une heure trop proche (moins de 15 min)."));
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors, let default save proceed
            }
        }
        rendezVous.setId(id);
        return ResponseEntity.ok(rendezVousRepository.save(rendezVous));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRendezVous(@PathVariable Long id) {
        if (!rendezVousRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rendezVousRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
