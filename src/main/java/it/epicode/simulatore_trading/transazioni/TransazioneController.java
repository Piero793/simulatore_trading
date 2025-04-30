package it.epicode.simulatore_trading.transazioni;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transazioni")
public class TransazioneController {

    @Autowired
    private TransazioneService transazioneService;

    // Recupera tutte le transazioni per l'utente specificato
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TransazioneResponse> getTransazioniPerUtente(@RequestParam String nomeUtente) {
        return transazioneService.getTransazioniPerUtente(nomeUtente);
    }

    // Registra una nuova transazione di acquisto o vendita
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TransazioneResponse> salvaTransazione(@Valid @RequestBody TransazioneRequest request) {
        return ResponseEntity.ok(transazioneService.salvaTransazione(request));
    }
}