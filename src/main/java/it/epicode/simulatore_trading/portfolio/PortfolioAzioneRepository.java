package it.epicode.simulatore_trading.portfolio;


import it.epicode.simulatore_trading.azioni.Azione;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface PortfolioAzioneRepository extends JpaRepository<PortfolioAzione, PortfolioAzioneId> {
    // Metodi personalizzati che potrebbero essere utili:

    // Trova tutte le PortfolioAzione per un dato portfolio
    List<PortfolioAzione> findByPortfolio(Portfolio portfolio);

    // Trova la PortfolioAzione specifica per un dato portfolio e una data azione
    Optional<PortfolioAzione> findByPortfolioAndAzione(Portfolio portfolio, Azione azione);
}
