package it.epicode.simulatore_trading.portfolio;


import it.epicode.simulatore_trading.azioni.Azione;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface PortfolioAzioneRepository extends JpaRepository<PortfolioAzione, PortfolioAzioneId> {
    List<PortfolioAzione> findByPortfolio(Portfolio portfolio);
    Optional<PortfolioAzione> findByPortfolioAndAzione(Portfolio portfolio, Azione azione);
}
