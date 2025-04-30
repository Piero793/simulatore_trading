package it.epicode.simulatore_trading.utenti;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UtenteResponse utente;

    // Costruttore aggiuntivo per semplicità
    public LoginResponse(String token, UtenteResponse utente) {
        this.token = token;
        this.utente = utente;
    }
}

