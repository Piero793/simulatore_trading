package it.epicode.simulatore_trading.utenti;


import it.epicode.simulatore_trading.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UtenteService utenteService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UtenteService utenteService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.utenteService = utenteService;
    }

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
                    logger.error("Errore critico: Utente non trovato nel DB dopo autenticazione riuscita per email: {}",
                            loginRequest.getEmail());
                    return new RuntimeException("Errore nel recupero utente dopo autenticazione");
                });

        UtenteResponse utenteResponse = utenteService.mapUtenteToUtenteResponse(utente);

        LoginResponse loginResponse = new LoginResponse(jwt, utenteResponse);
        logger.info("Login riuscito per l'utente: {}", loginRequest.getEmail());

        return ResponseEntity.ok(loginResponse);
    }
}
