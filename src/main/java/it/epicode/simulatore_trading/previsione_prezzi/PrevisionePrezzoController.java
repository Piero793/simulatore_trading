package it.epicode.simulatore_trading.previsione_prezzi;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Azione non trovata: " + e.getMessage());
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Errore di validazione: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Errore nel calcolo della previsione: " + e.getMessage());
        }
    }
}