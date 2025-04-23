package it.epicode.simulatore_trading.transazioni;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transazioni")
@CrossOrigin(origins = "*")
public class TransazioneController {

    @Autowired
    private TransazioneService transazioneService;

    /**
     * Recupera tutte le transazioni registrate.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TransazioneResponse> getTransazioni() {
        return transazioneService.getTransazioni();
    }

    /**
     * Registra una nuova transazione di acquisto o vendita.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TransazioneResponse> salvaTransazione(@Valid @RequestBody TransazioneRequest request) {
        return ResponseEntity.ok(transazioneService.salvaTransazione(request));
    }
}
