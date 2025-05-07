package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.AzioneResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AzioneNelPortfolioResponse {
    private AzioneResponse azione;
    private int quantita;
}
