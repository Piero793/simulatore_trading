package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.utenti.Utente;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transazioni")
@Slf4j
public class TransazioneController {

    private final TransazioneService transazioneService;

    public TransazioneController(TransazioneService transazioneService) {
        this.transazioneService = transazioneService;
    }

    private Long getAuthenticatedUserId() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            return ((Utente) authentication.getPrincipal()).getId();
        }
        log.warn("Tentativo di accesso alle transazioni da utente non autenticato.");
        throw new AccessDeniedException("Utente non autenticato o informazioni insufficienti.");
    }

    // Recupera tutte le transazioni per l'utente autenticato
    @GetMapping("/me")
    public ResponseEntity<List<TransazioneResponse>> getMyTransazioni() {
        log.info("Richiesta per visualizzare le transazioni dell'utente autenticato.");
        try {
            Long userId = getAuthenticatedUserId();
            List<TransazioneResponse> transazioni = transazioneService.getTransazioniByUserId(userId);
            log.info("Transazioni recuperate con successo per l'utente ID: {}", userId);
            return ResponseEntity.ok(transazioni);
        } catch (AccessDeniedException e) {
            log.warn("Accesso negato durante la visualizzazione delle transazioni: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
    }

    // Registra una nuova transazione di acquisto o vendita
    @PostMapping
    public ResponseEntity<?> salvaTransazione(@Valid @RequestBody TransazioneRequest request) {
        log.info("Richiesta per salvare una nuova transazione: {}", request);
        try {
            Long userId = getAuthenticatedUserId();
            TransazioneResponse response = transazioneService.salvaTransazione(request, userId);
            log.info("Transazione salvata con successo per l'utente ID: {}, Transazione ID: {}", userId, response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            log.warn("Accesso negato durante il salvataggio della transazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        } catch (ExceptionHandlerClass.InsufficientBalanceException e) {
            log.warn("Saldo insufficiente durante il salvataggio della transazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionHandlerClass.ErrorResponse(e.getMessage())); // 400 Bad Request
        } catch (ExceptionHandlerClass.InsufficientQuantityException e) {
            log.warn("Quantità insufficiente durante il salvataggio della transazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionHandlerClass.ErrorResponse(e.getMessage()));
        } catch (ConstraintViolationException e) {
            log.warn("Errore di validazione durante il salvataggio della transazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionHandlerClass.ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore inatteso durante il salvataggio della transazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}