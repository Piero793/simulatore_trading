package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "previsioni_prezzo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrevisionePrezzo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    private Azione azione;
    private int giorno;
    private double prezzoPrevisto;
}
