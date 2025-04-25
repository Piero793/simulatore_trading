package it.epicode.simulatore_trading.utenti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtenteResponse {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String imgUrl;

    public UtenteResponse(Long id, String nome, String cognome, String email) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }
}
