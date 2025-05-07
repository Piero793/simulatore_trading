package it.epicode.simulatore_trading.utenti;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

    private static final Logger logger = LoggerFactory.getLogger(UtenteController.class);

    private final UtenteService utenteService;

    @Autowired
    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @PostMapping("/register")
    public ResponseEntity<UtenteResponse> registerUser(@RequestBody @Valid UtenteRequest utenteRequest) {
        logger.info("Richiesta di registrazione ricevuta per email: {}", utenteRequest.getEmail());
        UtenteResponse utenteResponse = utenteService.registraUtente(utenteRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utenteResponse);
    }

    // Endpoint per ottenere il saldo dell'utente autenticato
    @GetMapping("/saldo")
    public ResponseEntity<SaldoResponse> getMySaldo(@AuthenticationPrincipal Utente utenteAutenticato) {
        logger.info("DEBUG: Richiesta GET /api/utenti/saldo ricevuta per l'utente autenticato.");
        if (utenteAutenticato != null) {
            Long userId = utenteAutenticato.getId();
            Double saldo = utenteService.getSaldoById(userId);
            if (saldo != null) {
                logger.info("Saldo trovato per l'utente ID {}: {}", userId, saldo);
                return ResponseEntity.ok(new SaldoResponse(saldo));
            } else {
                logger.warn("Saldo non trovato (null) per l'utente ID: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}