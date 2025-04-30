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
@AllArgsConstructor
@NoArgsConstructor
public class Utente implements UserDetails { // Implementa l'interfaccia UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 30, nullable = false)
    private String nome;

    @Column(length = 30, nullable = false)
    private String cognome;

    @Column(length = 50, nullable = false, unique = true)
    private String email; // Questo sarà lo username per Spring Security

    // La password cifrata con BCrypt.
    @Column(nullable = false)
    private String password;

    private String imgUrl;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Portfolio portfolio;

    private Double saldo;

    public Utente(Long id, String nome, String cognome, String email, String password, Double saldo) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.saldo = saldo;
    }

    // --- Metodi richiesti dall'interfaccia UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        // Restituisce il campo password dall'entità
        return this.password;
    }

    @Override
    public String getUsername() {
        // Usiamo l'email come username per Spring Security
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Restituisce true se l'account non è scaduto.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Restituisce true se l'account non è bloccato.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Restituisce true se le credenziali (password) non sono scadute.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Restituisce true se l'utente è abilitato.
        return true;
    }
}