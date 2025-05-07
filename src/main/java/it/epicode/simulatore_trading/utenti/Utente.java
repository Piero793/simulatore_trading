package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "utenti")
@NoArgsConstructor
public class Utente implements UserDetails {

    public enum Ruolo {
        USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 30, nullable = false)
    private String nome;

    @Column(length = 30, nullable = false)
    private String cognome;

    @Column(length = 50, nullable = false, unique = true)
    private String email; // Questo sarà lo username per Spring Security

    @Column(nullable = false)
    private String password; // La password cifrata con BCrypt.

    private String imgUrl;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Portfolio portfolio;

    private Double saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
    private Ruolo ruolo;

    // Costruttore senza ruolo (imposta di default USER)
    public Utente(Long id, String nome, String cognome, String email, String password, Double saldo, String imgUrl, Portfolio portfolio) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.saldo = saldo;
        this.imgUrl = imgUrl;
        this.portfolio = portfolio;
        this.ruolo = Ruolo.USER;
    }

    // Costruttore completo (opzionale se necessario)
    public Utente(Long id, String nome, String cognome, String email, String password, String imgUrl, Portfolio portfolio, Double saldo, Ruolo ruolo) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.imgUrl = imgUrl;
        this.portfolio = portfolio;
        this.saldo = saldo;
        this.ruolo = ruolo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + ruolo.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}