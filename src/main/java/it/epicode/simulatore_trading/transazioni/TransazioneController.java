package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.utenti.Utente;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/transazioni")
public class TransazioneController {

    @Autowired
    private TransazioneService transazioneService;

    // Recupera tutte le transazioni per l'utente autenticato
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<TransazioneResponse> getMyTransazioni() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            Long userId = ((Utente) authentication.getPrincipal()).getId();
            return transazioneService.getTransazioniByUserId(userId);
        }
        throw new AccessDeniedException("Utente non autenticato o informazioni insufficienti.");
    }

    // Registra una nuova transazione di acquisto o vendita
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TransazioneResponse> salvaTransazione(@Valid @RequestBody TransazioneRequest request) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            Long userId = ((Utente) authentication.getPrincipal()).getId();
            return ResponseEntity.ok(transazioneService.salvaTransazione(request, userId));
        }
        throw new AccessDeniedException("Utente non autenticato o informazioni insufficienti.");
    }
}