package it.epicode.simulatore_trading.transazioni;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransazioneResponse {
    private Long id;
    private String tipoTransazione;
    private int quantita;
    private double prezzoUnitario;
    private Long azioneId;
    private String nomeAzione;
}
