package it.epicode.simulatore_trading.azioni;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AzioneResponse {
    private Long id;
    private String nome;
    private double valoreAttuale;
    private double variazione;
    private int quantita;
}
