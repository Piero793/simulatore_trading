package it.epicode.simulatore_trading.utenti;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByNome(String nome);

    Optional<Utente> findByEmail(String email);

    Optional<Utente> findByEmailAndPassword(String email, String password);
}
