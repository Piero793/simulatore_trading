package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@Entity
@AllArgsConstructor
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

    private Double saldo;

    public Utente() {
        this.saldo = 10000.0; // Imposta il saldo predefinito
    }

    public Utente(Long id, String nome, String cognome, String email, String password, Double saldo) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.saldo = saldo;
    }
}