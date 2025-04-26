package it.epicode.simulatore_trading.utenti;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utenti")
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

    /**
     * Recupera il saldo di un utente dato il suo nome.
     * @param nomeUtente Il nome dell'utente di cui recuperare il saldo.
     * @return Un oggetto JSON contenente il saldo dell'utente.
     */
    @GetMapping("/saldo/{nomeUtente}")
    public ResponseEntity<SaldoResponse> getSaldoUtente(@PathVariable String nomeUtente) {
        Double saldo = utenteService.getSaldoByNome(nomeUtente);
        if (saldo != null) {
            return ResponseEntity.ok(new SaldoResponse(saldo));
        } else {
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