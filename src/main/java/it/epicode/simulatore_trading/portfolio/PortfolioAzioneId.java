package it.epicode.simulatore_trading.portfolio;

import lombok.Data;
import java.io.Serializable;

@Data
public class PortfolioAzioneId implements Serializable {
    private Long portfolio;
    private Long azione;

}