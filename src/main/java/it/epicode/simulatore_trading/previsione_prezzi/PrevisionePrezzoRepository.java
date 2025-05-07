package it.epicode.simulatore_trading.previsione_prezzi;


import it.epicode.simulatore_trading.azioni.Azione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrevisionePrezzoRepository extends JpaRepository<PrevisionePrezzo, Long> {
    List<PrevisionePrezzo> findByAzione(Azione azione);
    List<PrevisionePrezzo> findByAzioneOrderByGiornoAsc(Azione azione);
}
