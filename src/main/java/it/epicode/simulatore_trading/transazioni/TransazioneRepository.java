package it.epicode.simulatore_trading.transazioni;


import it.epicode.simulatore_trading.utenti.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransazioneRepository extends JpaRepository<Transazione, Long> {
    List<Transazione> findByUtente(Utente utente);
}
