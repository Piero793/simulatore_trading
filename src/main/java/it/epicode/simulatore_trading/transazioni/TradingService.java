package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.portfolio.Portfolio;
import it.epicode.simulatore_trading.portfolio.PortfolioRepository;
import it.epicode.simulatore_trading.utenti.Utente;
import it.epicode.simulatore_trading.utenti.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradingService {

    private final UtenteRepository utenteRepository;
    private final PortfolioRepository portfolioRepository;

    public TradingService(UtenteRepository utenteRepository, PortfolioRepository portfolioRepository) {
        this.utenteRepository = utenteRepository;
        this.portfolioRepository = portfolioRepository;
    }


    @Transactional
    public void eseguiAcquisto(Utente utente, Portfolio portfolio, Azione azione, int quantita)
            throws ExceptionHandlerClass.InsufficientBalanceException {
        double valoreTotale = (double) quantita * azione.getValoreAttuale();
        if (utente.getSaldo() == null || utente.getSaldo() < valoreTotale) {
            throw new ExceptionHandlerClass.InsufficientBalanceException("Saldo insufficiente per l'acquisto! Saldo attuale: " + utente.getSaldo());
        }
        utente.setSaldo(utente.getSaldo() - valoreTotale);
        portfolio.aggiungiAzione(azione, quantita);
        utenteRepository.save(utente);
        portfolioRepository.save(portfolio);
    }


    @Transactional
    public void eseguiVendita(Utente utente, Portfolio portfolio, Azione azione, int quantita)
            throws ExceptionHandlerClass.InsufficientQuantityException {
        int quantitaPosseduta = portfolio.getQuantitaAzione(azione);
        if (quantita > quantitaPosseduta) {
            throw new ExceptionHandlerClass.InsufficientQuantityException(
                    "Quantità insufficiente di azioni per la vendita! Possedute: " + quantitaPosseduta);
        }
        double valoreTotale = (double) quantita * azione.getValoreAttuale();
        utente.setSaldo(utente.getSaldo() + valoreTotale);
        portfolio.aggiungiAzione(azione, -quantita);
        utenteRepository.save(utente);
        portfolioRepository.save(portfolio);
    }
}
