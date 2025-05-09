package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/previsione")
public class PrevisionePrezzoController {

    private final PrevisionePrezzoService previsionePrezzoService;
    private final AlertPrevisioneService alertPrevisioneService;

    public PrevisionePrezzoController(PrevisionePrezzoService previsionePrezzoService,
                                      AlertPrevisioneService alertPrevisioneService) {
        this.previsionePrezzoService = previsionePrezzoService;
        this.alertPrevisioneService = alertPrevisioneService;
    }

    @GetMapping("/{azioneId}")
    public ResponseEntity<?> getDatiStoriciPrevisione(@PathVariable Long azioneId) {
        try {
            Azione azione = previsionePrezzoService.getAzioneById(azioneId);
            List<PrevisionePrezzo> datiStorici = previsionePrezzoService.getDatiStoriciPerAzione(azione);
            return ResponseEntity.ok(datiStorici);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Azione non trovata: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("Errore nel recupero dei dati storici: "
                    + e.getMessage());
        }
    }

    @GetMapping("/alert/{azioneId}")
    public ResponseEntity<?> verificaAlert(@PathVariable Long azioneId) {
        try {
            String messaggioAlert = alertPrevisioneService.verificaPrevisione(azioneId);
            return ResponseEntity.ok(messaggioAlert);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Azione non trovata: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nel controllo alert: "
                    + e.getMessage());
        }
    }
}