package it.epicode.simulatore_trading.transazioni;


import org.springframework.data.jpa.repository.JpaRepository;

public interface TransazioneRepository extends JpaRepository<Transazione, Long> {
}
