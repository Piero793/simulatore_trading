package it.epicode.simulatore_trading.portfolio;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequest {
    @NotBlank(message = "Il nome utente non può essere vuoto")
    private String nomeUtente;
}
