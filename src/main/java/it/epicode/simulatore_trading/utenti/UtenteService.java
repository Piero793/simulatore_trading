package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.portfolio.Portfolio;
import it.epicode.simulatore_trading.portfolio.PortfolioResponse;
import it.epicode.simulatore_trading.portfolio.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class UtenteService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PasswordEncoder passwordEncoder; // PasswordEncoder (BCrypt)

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

        // crea la password cifrata prima di salvarla
        String passwordCifrata = passwordEncoder.encode(request.getPassword());
        nuovoUtente.setPassword(passwordCifrata);

        // inizializza il saldo del nuovo utente
        nuovoUtente.setSaldo(10000.0); // Saldo virtuale iniziale
        logger.info("Saldo iniziale impostato per il nuovo utente: {}", nuovoUtente.getSaldo());
        // -----------------------------------------------------------

        logger.info("Utente creato (prima del salvataggio): {}", nuovoUtente);

        // 3. Salva l'utente nel database
        Utente utenteSalvato = utenteRepository.save(nuovoUtente);
        logger.info("Utente salvato con ID: {}", utenteSalvato.getId());

        // 4. Crea un nuovo Portfolio per l'utente
        logger.info("Inizio creazione portfolio per l'utente con ID: {}", utenteSalvato.getId());
        PortfolioResponse portfolioCreato = portfolioService.creaPortfolio(utenteSalvato);
        logger.info("Portfolio creato con ID: {} per l'utente ID: {}", portfolioCreato.getId(), utenteSalvato.getId());

        // 5. Assegna il portfolio all'utente salvato
        Portfolio portfolioEntity = portfolioService.findById(portfolioCreato.getId())
                .orElseThrow(() -> new RuntimeException("Errore nel recupero dell'entità Portfolio dopo la creazione"));

        utenteSalvato.setPortfolio(portfolioEntity); // Associa l'entità portfolio completa all'utente
        utenteRepository.save(utenteSalvato); // Salva di nuovo l'utente con il riferimento al portfolio
        logger.info("Entità Portfolio con ID: {} assegnata all'utente con ID: {}", portfolioEntity.getId(), utenteSalvato.getId());


        // 6. Crea e restituisci la UtenteResponse
        return mapUtenteToUtenteResponse(utenteSalvato);
    }


    // --- METODO loginUtente NON PIÙ USATO PER L'AUTENTICAZIONE CON SPRING SECURITY ---
    /*
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
    */

    // Metodo per mappare l'entità Utente a UtenteResponse
    public UtenteResponse mapUtenteToUtenteResponse(Utente utente) {
        UtenteResponse response = new UtenteResponse();
        BeanUtils.copyProperties(utente, response);

        if (utente.getPortfolio() != null) {
            response.setPortfolioId(utente.getPortfolio().getId());
            logger.info("Portfolio ID {} aggiunto alla UtenteResponse (mapping) per utente ID {}", utente.getPortfolio().getId(), utente.getId());
        } else {
            logger.warn("Portfolio non associato all'utente ID {} (mapping). Portfolio ID non sarà incluso nella UtenteResponse.", utente.getId());
        }

        response.setSaldo(utente.getSaldo());

        return response;
    }

    // Recupera il saldo di un utente dato il suo ID.
    public Double getSaldoById(Long userId) {
        logger.info("Recupero saldo per l'utente con ID: {}", userId);
        return utenteRepository.findById(userId)
                .map(Utente::getSaldo)
                .orElse(null);
    }

    // Metodo per trovare un utente per email
    public Optional<Utente> findByEmail(String email) {
        return utenteRepository.findByEmail(email);
    }

    // Metodo per trovare un utente per nome
    public Optional<Utente> findByNome(String nome) {
        return utenteRepository.findByNome(nome);
    }

    // Metodo per trovare un utente per ID
    public Optional<Utente> findById(Long id) {
        return utenteRepository.findById(id);
    }
}