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

        // 1. Autentica l'utente usando l'AuthenticationManager di Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()) // Password in chiaro
        );

        // 2. Imposta l'oggetto Authentication nel SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Genera il token JWT per l'utente autenticato
        String jwt = jwtTokenProvider.generateToken(authentication);
        logger.info("Token JWT generato per l'utente: {}", loginRequest.getEmail());


        // 4. Recupera i dettagli dell'utente autenticato per la risposta
        Utente utente = utenteService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.error("Errore critico: Utente non trovato nel DB dopo autenticazione riuscita per email: {}", loginRequest.getEmail());
                    return new RuntimeException("Errore nel recupero utente dopo autenticazione");
                });

        UtenteResponse utenteResponse = utenteService.mapUtenteToUtenteResponse(utente);

        // 5. Restituisci il token JWT e i dettagli dell'utente nella risposta
        LoginResponse loginResponse = new LoginResponse(jwt, utenteResponse);
        logger.info("Login riuscito per l'utente: {}", loginRequest.getEmail());

        return ResponseEntity.ok(loginResponse);
    }

   // Endpoint per ottenere il saldo di un utente
    @GetMapping("/saldo/{nomeUtente}")
    public ResponseEntity<SaldoResponse> getSaldoUtente(@PathVariable String nomeUtente) {
        logger.info("DEBUG: Richiesta GET /api/utenti/saldo ricevuta per utente: {}", nomeUtente);

        Double saldo = utenteService.getSaldoByNome(nomeUtente);
        if (saldo != null) {
            logger.info("Saldo trovato per l'utente {}: {}", nomeUtente, saldo);
            return ResponseEntity.ok(new SaldoResponse(saldo));
        } else {
            // Restituire 404 qui è appropriato se l'utente esiste ma non ha un saldo (o non è stato inizializzato)
            logger.warn("Saldo non trovato (null) per l'utente: {}", nomeUtente);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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

