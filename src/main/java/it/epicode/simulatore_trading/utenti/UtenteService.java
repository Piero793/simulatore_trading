package it.epicode.simulatore_trading.utenti;

import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UtenteService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UtenteResponse registraUtente(UtenteRequest request) {
        logger.info("Inizio registrazione utente con email: {}", request.getEmail());

        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Tentativo di registrazione con email già esistente: {}", request.getEmail());
            throw new ExceptionHandlerClass.EmailAlreadyExistsException("L'email fornita è già registrata.");
        }

        Utente nuovoUtente = new Utente();
        BeanUtils.copyProperties(request, nuovoUtente);
        nuovoUtente.setPassword(passwordEncoder.encode(request.getPassword()));
        nuovoUtente.setSaldo(10000.0);

        nuovoUtente.setRuolo(Utente.Ruolo.USER);

        logger.info("Utente creato (prima del salvataggio): {}", nuovoUtente);

        Utente utenteSalvato = utenteRepository.save(nuovoUtente);
        logger.info("Utente salvato con ID: {}", utenteSalvato.getId());

        eventPublisher.publishEvent(new UtenteRegistratoEvent(utenteSalvato));
        logger.info("Evento UtenteRegistratoEvent pubblicato per l'utente ID: {}", utenteSalvato.getId());

        return mapUtenteToUtenteResponse(utenteSalvato);
    }

    public UtenteResponse mapUtenteToUtenteResponse(Utente utente) {
        UtenteResponse response = new UtenteResponse();
        BeanUtils.copyProperties(utente, response);
        if (utente.getPortfolio() != null) {
            response.setPortfolioId(utente.getPortfolio().getId());
            logger.info("Portfolio ID {} aggiunto alla UtenteResponse (mapping) per utente ID {}",
                    utente.getPortfolio().getId(), utente.getId());
        } else {
            logger.warn("Portfolio non associato all'utente ID {} (mapping). Portfolio ID non sarà incluso nella UtenteResponse.", utente.getId());
        }
        response.setSaldo(utente.getSaldo());
        response.setRuolo(utente.getRuolo());
        return response;
    }

    public Double getSaldoById(Long userId) {
        logger.info("Recupero saldo per l'utente con ID: {}", userId);
        return utenteRepository.findById(userId)
                .map(Utente::getSaldo)
                .orElseThrow(() -> new ExceptionHandlerClass.UserNotFoundException("Utente non trovato con ID: " + userId));
    }

    public Optional<Utente> findByEmail(String email) {
        return utenteRepository.findByEmail(email);
    }

    public Optional<Utente> findByNome(String nome) {
        return utenteRepository.findByNome(nome);
    }

    public Optional<Utente> findById(Long id) {
        return utenteRepository.findById(id);
    }
}