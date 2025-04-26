package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.portfolio.Portfolio;
import it.epicode.simulatore_trading.portfolio.PortfolioRequest;
import it.epicode.simulatore_trading.portfolio.PortfolioResponse;
import it.epicode.simulatore_trading.portfolio.PortfolioService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UtenteService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Transactional
    public UtenteResponse registraUtente(UtenteRequest request) {
        logger.info("Inizio registrazione utente con email: {}", request.getEmail());

        // 1. Verifica se l'email è già utilizzata
        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Tentativo di registrazione con email già esistente: {}", request.getEmail());
            throw new ExceptionHandlerClass.EmailAlreadyExistsException("L'email fornita è già registrata.");
        }

        // 2. Crea una nuova entity Utente
        Utente nuovoUtente = new Utente();
        BeanUtils.copyProperties(request, nuovoUtente);
        logger.info("Utente creato (prima del salvataggio): {}", nuovoUtente);

        // 3. Salva l'utente nel database
        Utente utenteSalvato = utenteRepository.save(nuovoUtente);
        logger.info("Utente salvato con ID: {}", utenteSalvato.getId());

        // 4. Crea un nuovo Portfolio per l'utente
        logger.info("Inizio creazione portfolio per l'utente: {}", utenteSalvato.getNome());
        PortfolioRequest portfolioRequest = new PortfolioRequest();
        portfolioRequest.setNomeUtente(utenteSalvato.getNome()); // Potresti usare un altro identificativo
        PortfolioResponse portfolioCreato = portfolioService.creaPortfolio(portfolioRequest);
        logger.info("Portfolio creato con ID: {} per l'utente: {}", portfolioCreato.getId(), utenteSalvato.getNome());

        // 5. Assegna il portfolio all'utente salvato
        Portfolio portfolioRiferimento = new Portfolio();
        portfolioRiferimento.setId(portfolioCreato.getId());
        utenteSalvato.setPortfolio(portfolioRiferimento);
        utenteRepository.save(utenteSalvato);
        logger.info("Portfolio con ID: {} assegnato all'utente con ID: {}", portfolioCreato.getId(), utenteSalvato.getId());        utenteRepository.save(utenteSalvato);

        // 6. Crea e restituisci la UtenteResponse
        UtenteResponse response = new UtenteResponse();
        BeanUtils.copyProperties(utenteSalvato, response);
        logger.info("Registrazione utente completata con successo per email: {}", response.getEmail());
        return response;
    }

    // Metodo per la gestione del login
    public UtenteResponse loginUtente(String email, String password) {
        logger.info("Tentativo di login per l'utente con email: {}", email);
        return utenteRepository.findByEmailAndPassword(email, password)
                .map(utente -> {
                    UtenteResponse response = new UtenteResponse();
                    BeanUtils.copyProperties(utente, response);
                    logger.info("Login riuscito per l'utente con email: {}", email);
                    return response;
                })
                .orElseThrow(() -> {
                    logger.warn("Tentativo di login fallito per l'utente con email: {}", email);
                    return new ExceptionHandlerClass.UserNotFoundException("Credenziali di accesso non valide.");
                });
    }
}