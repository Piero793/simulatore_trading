package it.epicode.simulatore_trading.utenti;


import it.epicode.simulatore_trading.portfolio.PortfolioResponse;
import it.epicode.simulatore_trading.portfolio.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtenteInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteInitializationService.class);

    private final PortfolioService portfolioService;
    private final UtenteRepository utenteRepository;

    @Autowired
    public UtenteInitializationService(PortfolioService portfolioService, UtenteRepository utenteRepository) {
        this.portfolioService = portfolioService;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    @EventListener
    public void handleUtenteRegistratoEvent(UtenteRegistratoEvent event) {
        Utente utente = event.getUtente();
        logger.info("Ricevuto evento UtenteRegistratoEvent per l'utente ID: {}", utente.getId());

        logger.info("Inizio creazione portfolio per l'utente ID: {}", utente.getId());
        PortfolioResponse portfolioCreato = portfolioService.creaPortfolio(utente);
        logger.info("Portfolio creato con ID: {} per l'utente ID: {}", portfolioCreato.getId(), utente.getId());

        portfolioService.findById(portfolioCreato.getId())
                .ifPresent(portfolio -> {
                    utente.setPortfolio(portfolio);
                    utenteRepository.save(utente);
                    logger.info("Portfolio ID {} assegnato all'utente ID: {}", portfolio.getId(), utente.getId());
                });
    }
}
