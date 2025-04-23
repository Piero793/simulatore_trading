package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.AzioneResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private Long id;
    private String nomeUtente;
    private List<AzioneResponse> azioni; //uso AzioneResponse per evitre di esporre la entity principale!
    private int quantita;
}
