package it.epicode.simulatore_trading.portfolio;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequest {
    @NotNull(message = "Il nome utente non può essere vuoto")
    private String nomeUtente;
}
