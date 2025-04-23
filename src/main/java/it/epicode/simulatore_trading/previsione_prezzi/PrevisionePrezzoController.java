package it.epicode.simulatore_trading.previsione_prezzi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/previsione")
@CrossOrigin(origins = "*")
public class PrevisionePrezzoController {

    private final PrevisionePrezzoService previsionePrezzoService;

    public PrevisionePrezzoController(PrevisionePrezzoService previsionePrezzoService) {
        this.previsionePrezzoService = previsionePrezzoService;
    }

    // API per ottenere la previsione di un asset specifico
    @GetMapping("/{azioneId}")
    public ResponseEntity<?> calcolaPrevisione(@PathVariable Long azioneId) {
        try {
            double previsione = previsionePrezzoService.prevediPrezzoPerAzione(azioneId);
            return ResponseEntity.ok(previsione);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Nessun dato disponibile per questa azione.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Errore nel calcolo della previsione: " + e.getMessage());
        }
    }
}