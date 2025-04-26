package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transazioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transazione {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String tipoTransazione;
    private int quantita;
    private double prezzoUnitario;

    @ManyToOne
    private Azione azione;

    @ManyToOne
    private Utente utente;
}
