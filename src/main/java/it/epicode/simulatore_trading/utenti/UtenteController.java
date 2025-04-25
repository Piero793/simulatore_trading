package it.epicode.simulatore_trading.utenti;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UtenteController {

    @Autowired
    private UtenteService utenteService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UtenteResponse registerUser(@RequestBody @Valid UtenteRequest utenteRequest) {
        return utenteService.registraUtente(utenteRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<UtenteResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        UtenteResponse utenteResponse = utenteService.loginUtente(loginRequest.getEmail(), loginRequest.getPassword());
        if (utenteResponse != null) {
            return ResponseEntity.ok(utenteResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}