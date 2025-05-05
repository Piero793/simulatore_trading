package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

    private static final Logger logger = LoggerFactory.getLogger(UtenteController.class);

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UtenteResponse registerUser(@RequestBody @Valid UtenteRequest utenteRequest) {
        logger.info("Richiesta di registrazione ricevuta per email: {}", utenteRequest.getEmail());
        return utenteService.registraUtente(utenteRequest);
    }

    // Endpoint per il login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("Tentativo di login per l'utente con email: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        logger.info("Token JWT generato per l'utente: {}", loginRequest.getEmail());

        Utente utente = utenteService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.error("Errore critico: Utente non trovato nel DB dopo autenticazione riuscita per email: {}", loginRequest.getEmail());
                    return new RuntimeException("Errore nel recupero utente dopo autenticazione");
                });

        UtenteResponse utenteResponse = utenteService.mapUtenteToUtenteResponse(utente);

        LoginResponse loginResponse = new LoginResponse(jwt, utenteResponse);
        logger.info("Login riuscito per l'utente: {}", loginRequest.getEmail());

        return ResponseEntity.ok(loginResponse);
    }

    // Endpoint per ottenere il saldo dell'utente autenticato
    @GetMapping("/saldo")
    public ResponseEntity<SaldoResponse> getMySaldo() {
        logger.info("DEBUG: Richiesta GET /api/utenti/saldo ricevuta per l'utente autenticato.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            Long userId = ((Utente) authentication.getPrincipal()).getId();
            Double saldo = utenteService.getSaldoById(userId);
            if (saldo != null) {
                logger.info("Saldo trovato per l'utente ID {}: {}", userId, saldo);
                return ResponseEntity.ok(new SaldoResponse(saldo));
            } else {
                logger.warn("Saldo non trovato (null) per l'utente ID: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        // Se l'utente non è autenticato correttamente, restituisci 401 Unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Classe interna per la risposta del saldo
    private static class SaldoResponse {
        private Double saldo;

        public SaldoResponse(Double saldo) {
            this.saldo = saldo;
        }

        public Double getSaldo() {
            return saldo;
        }

        public void setSaldo(Double saldo) {
            this.saldo = saldo;
        }
    }
}

