package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.azioni.AzioneResponse;
import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.utenti.Utente;
import it.epicode.simulatore_trading.utenti.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);


    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AzioneRepository azioneRepository;

    @Autowired
    private PortfolioAzioneRepository portfolioAzioneRepository;

    @Autowired
    private UtenteRepository utenteRepository;


    public PortfolioResponse getPortfolio(String nomeUtente) {
        logger.info("Inizio recupero portfolio per l'utente (tramite ricerca Utente per nome): {}", nomeUtente);

        // 1. Trova l'utente per nome
        logger.info("Ricerca Utente con nome: {}", nomeUtente);
        Utente utente = utenteRepository.findByNome(nomeUtente)
                .orElseThrow(() -> {
                    logger.error("ERRORE: Utente NON trovato durante recupero portfolio per nome: {}", nomeUtente);
                    return new EntityNotFoundException("Utente non trovato per nome: " + nomeUtente);
                });

        logger.info("Utente trovato con ID: {} per il recupero portfolio.", utente.getId());

        // 2. Accedi al portfolio tramite la relazione OneToOne sull'entità Utente
        logger.info("Accesso al portfolio associato all'utente ID: {}", utente.getId());
        Portfolio portfolio = utente.getPortfolio();

        if (portfolio == null) {
            logger.error("ERRORE: Portfolio NON associato all'utente ID: {} (nome: {})", utente.getId(), nomeUtente);
            throw new EntityNotFoundException("Portfolio non trovato per l'utente: " + nomeUtente);
        }

        logger.info("Portfolio trovato con ID: {} associato all'utente: {}", portfolio.getId(), nomeUtente);

        PortfolioResponse response = new PortfolioResponse();
        BeanUtils.copyProperties(portfolio, response);
        // Mappa le PortfolioAzione in AzioneResponse includendo la quantità
        response.setAzioni(portfolio.getPortfolioAzioni().stream()
                .map(pa -> new AzioneResponse(
                        pa.getAzione().getId(),
                        pa.getAzione().getNome(),
                        pa.getAzione().getValoreAttuale(),
                        pa.getAzione().getVariazione(),
                        pa.getQuantita()
                ))
                .collect(Collectors.toList()));

        logger.info("Recupero portfolio completato con successo per l'utente: {}", nomeUtente);
        return response;
    }


    @Transactional // Assicurati che questo metodo sia transazionale
    public PortfolioResponse creaPortfolio(PortfolioRequest request) {
        logger.info("Inizio creazione portfolio per l'utente: {}", request.getNomeUtente());

        if (request.getNomeUtente() == null || request.getNomeUtente().isEmpty()) {
            logger.error("Errore creazione portfolio: Il nome utente nella request è vuoto.");
            throw new ConstraintViolationException("Il nome utente non può essere vuoto!", null);
        }

        // 1. Trova l'utente per nome
        logger.info("Ricerca Utente per creare portfolio con nome: {}", request.getNomeUtente());
        Utente utente = utenteRepository.findByNome(request.getNomeUtente())
                .orElseThrow(() -> {
                    logger.error("ERRORE: Utente NON trovato durante creazione portfolio per nome: {}", request.getNomeUtente());
                    return new EntityNotFoundException("Utente non trovato per creare portfolio: " + request.getNomeUtente());
                });

        // 2. Verifica se l'utente ha già un portfolio associato
        if (utente.getPortfolio() != null) {
            logger.warn("Tentativo di creare un portfolio per un utente che ne ha già uno. Utente ID: {}", utente.getId());
            throw new ExceptionHandlerClass.EmailAlreadyExistsException("Esiste già un portfolio per questo utente!");
        }

        // 3. Crea un nuovo portfolio
        Portfolio nuovoPortfolio = new Portfolio();

        // 4. Associa il nuovo portfolio all'utente
        nuovoPortfolio.setUtente(utente);
        utente.setPortfolio(nuovoPortfolio);

        // 5. Salva il nuovo portfolio
        Portfolio salvato = portfolioRepository.save(nuovoPortfolio);
        logger.info("Portfolio salvato con ID: {} e associato all'utente ID: {}", salvato.getId(), utente.getId());


        PortfolioResponse response = new PortfolioResponse();
        // Copia le proprietà dal portfolio salvato alla response
        BeanUtils.copyProperties(salvato, response);
        response.setAzioni(new ArrayList<>());
        response.setId(salvato.getId());

        logger.info("Creazione portfolio completata con successo per l'utente: {}", utente.getNome());
        return response;
    }


    @Transactional
    public PortfolioResponse aggiungiAzione(Long portfolioId, Long azioneId, int quantita) {
        logger.info("Inizio aggiunta/aggiornamento azione ID {} con quantità {} al portfolio ID {}", azioneId, quantita, portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> {
                    logger.error("ERRORE: Portfolio NON trovato durante aggiunta azione con ID: {}", portfolioId);
                    return new EntityNotFoundException("Portfolio non trovato con ID: " + portfolioId);
                });
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> {
                    logger.error("ERRORE: Azione NON trovata durante aggiunta azione con ID: {}", azioneId);
                    return new EntityNotFoundException("Azione non trovata con ID: " + azioneId);
                });

        // Se la quantità è 0, non facciamo nulla
        if (quantita == 0) {
            logger.warn("Tentativo di aggiungere/rimuovere quantità zero di azione ID {} al portfolio ID {}", azioneId, portfolioId);
            return getPortfolio(portfolio.getUtente().getNome());
        }


        Optional<PortfolioAzione> existingPortfolioAzioneOptional = portfolioAzioneRepository.findByPortfolioAndAzione(portfolio, azione);

        if (existingPortfolioAzioneOptional.isPresent()) {
            // Se esiste, aggiorna la quantità
            PortfolioAzione existingPortfolioAzione = existingPortfolioAzioneOptional.get();
            int nuovaQuantita = existingPortfolioAzione.getQuantita() + quantita;

            if (nuovaQuantita > 0) {
                existingPortfolioAzione.setQuantita(nuovaQuantita);
                portfolioAzioneRepository.save(existingPortfolioAzione);
                logger.info("Aggiornata quantità azione ID {} nel portfolio ID {}. Nuova quantità: {}", azioneId, portfolioId, nuovaQuantita);
            } else {
                // Se la nuova quantità è zero o negativa, rimuovi l'associazione
                portfolioAzioneRepository.delete(existingPortfolioAzione);
                portfolio.getPortfolioAzioni().remove(existingPortfolioAzione);
                logger.info("Rimossa azione ID {} dal portfolio ID {} (quantità scesa a zero/negativa)", azioneId, portfolioId);
            }
        } else {
            // Se non esiste, crea una nuova PortfolioAzione (solo se la quantità da aggiungere è positiva)
            if (quantita > 0) {
                PortfolioAzione nuovaPortfolioAzione = new PortfolioAzione(portfolio, azione, quantita);
                portfolioAzioneRepository.save(nuovaPortfolioAzione);
                portfolio.getPortfolioAzioni().add(nuovaPortfolioAzione);
                logger.info("Aggiunta nuova azione ID {} con quantità {} al portfolio ID {}", azioneId, quantita, portfolioId);
            } else {
                // Tentativo di vendere un'azione che l'utente non possiede
                logger.error("Errore aggiunta azione: Tentativo di vendere azione ID {} che l'utente non possiede nel portfolio ID {}", azioneId, portfolioId);
                throw new ExceptionHandlerClass.InsufficientQuantityException("Non possiedi questa azione per venderla!");
            }
        }

        logger.info("Recupero portfolio aggiornato dopo aggiunta/rimozione azione...");
        return getPortfolio(portfolio.getUtente().getNome());
    }


    // Metodo per trovare un portfolio per ID (utile se necessario)
    public Optional<Portfolio> findById(Long id) {
        return portfolioRepository.findById(id);
    }

    // opzionale
    public Optional<Portfolio> findByNomeUtente(String nomeUtente) {
        logger.warn("Chiamata a findByNomeUtente su PortfolioService. Questo metodo potrebbe non funzionare più come previsto dopo le modifiche alle entità.");

        return Optional.empty();
    }
}
