package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.Azione;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "portfolio_azioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PortfolioAzioneId.class) // Indica la classe per la chiave primaria composita
public class PortfolioAzione {
    @Id
    @ManyToOne
    private Portfolio portfolio;

    @Id
    @ManyToOne
    private Azione azione;

    @Column(nullable = false)
    private int quantita;
}
