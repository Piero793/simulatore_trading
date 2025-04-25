package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "utenti")
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 30, nullable = false)
    private String nome;

    @Column(length = 30, nullable = false)
    private String cognome;

    @Column(length = 30, nullable = false)
    private String email;

    @Column(length = 30, nullable = false)
    private String password;

    private String imgUrl;

    @OneToOne
    private Portfolio portfolio;


    public Utente(Long id, String nome, String cognome, String email, String password) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }
}
