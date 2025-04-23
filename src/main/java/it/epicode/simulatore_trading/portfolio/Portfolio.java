package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.Azione;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String nomeUtente;
    @OneToMany // Unidirezionale: Portfolio possiede Azioni
    private List<Azione> azioni = new ArrayList<>();
    private int quantita;
}
