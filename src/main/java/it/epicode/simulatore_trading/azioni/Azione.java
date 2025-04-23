package it.epicode.simulatore_trading.azioni;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "azioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Azione {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String nome;
    private double valoreAttuale;
    private double variazione;
    private int quantita;
}
