package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.portfolio.Portfolio;
import it.epicode.simulatore_trading.portfolio.PortfolioAzioneRepository;
import it.epicode.simulatore_trading.portfolio.PortfolioRepository;
import it.epicode.simulatore_trading.utenti.Utente;
import it.epicode.simulatore_trading.utenti.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransazioneService {

    @Autowired
    private TransazioneRepository transazioneRepository;

    @Autowired
    private AzioneRepository azioneRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PortfolioAzioneRepository portfolioAzioneRepository;

    /**
     * Recupera tutte le transazioni per l'utente specificato tramite ID utente.
     */
    public List<TransazioneResponse> getTransazioniByUserId(Long userId) {
        log.info("Recupero transazioni per l'utente con ID: {}", userId);
        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        List<Transazione> transazioni = transazioneRepository.findByUtente(utente);

        if (transazioni.isEmpty()) {
            return new ArrayList<>();
        }

        return transazioni.stream().map(transazione -> {
            TransazioneResponse response = new TransazioneResponse();
            BeanUtils.copyProperties(transazione, response);
            response.setAzioneId(transazione.getAzione().getId());
            response.setNomeAzione(transazione.getAzione().getNome());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * Registra una nuova transazione di acquisto o vendita per l'utente autenticato.
     *
     * @param request Oggetto TransazioneRequest contenente i dettagli della transazione.
     * @param userId  ID dell'utente autenticato che sta effettuando la transazione.
     * @return Oggetto TransazioneResponse della transazione salvata.
     */
    @Transactional
    public TransazioneResponse salvaTransazione(TransazioneRequest request, Long userId) throws AccessDeniedException {
        log.info("Salvataggio transazione per utente con ID: {}", userId);

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Portfolio portfolio = utente.getPortfolio();
        if (portfolio == null) {
            throw new EntityNotFoundException("Portfolio non trovato per l'utente con ID: " + userId);
        }

        if (!portfolio.getId().equals(request.getPortfolioId())) {
            log.warn("Tentativo di transazione sull'portfolio {} da parte dell'utente {}, ma non corrispondono.", request.getPortfolioId(), userId);
            throw new AccessDeniedException("Accesso negato a questo portfolio.");
        }

        Azione azione = azioneRepository.findById(request.getAzioneId())
                .orElseThrow(() -> new EntityNotFoundException("Azione non trovata con ID: " + request.getAzioneId()));

        double valoreTotale = (double) request.getQuantita() * azione.getValoreAttuale();

        // Logica di acquisto
        if ("Acquisto".equalsIgnoreCase(request.getTipoTransazione())) {
            if (utente.getSaldo() == null || utente.getSaldo() < valoreTotale) {
                throw new ExceptionHandlerClass.InsufficientBalanceException("Saldo insufficiente per l'acquisto! Saldo attuale: " + utente.getSaldo());
            }
            utente.setSaldo(utente.getSaldo() - valoreTotale);
            portfolio.aggiungiAzione(azione, request.getQuantita());
        }
        // Logica di vendita
        else if ("Vendita".equalsIgnoreCase(request.getTipoTransazione())) {
            int quantitaPosseduta = portfolio.getQuantitaAzione(azione);
            if (request.getQuantita() > quantitaPosseduta) {
                throw new ExceptionHandlerClass.InsufficientQuantityException("Quantità insufficiente di azioni per la vendita! Possedute: " + quantitaPosseduta);
            }
            utente.setSaldo(utente.getSaldo() + valoreTotale);
            portfolio.aggiungiAzione(azione, -request.getQuantita());
        } else {
            throw new ConstraintViolationException("Errore: Tipo di transazione non valido!", null);
        }

        Transazione nuovaTransazione = new Transazione();
        BeanUtils.copyProperties(request, nuovaTransazione);
        nuovaTransazione.setAzione(azione);
        nuovaTransazione.setUtente(utente);
        nuovaTransazione.setPortfolio(portfolio);

        Transazione salvata = transazioneRepository.save(nuovaTransazione);

        utenteRepository.save(utente);
        portfolioRepository.save(portfolio);

        TransazioneResponse response = new TransazioneResponse();
        BeanUtils.copyProperties(salvata, response);
        response.setAzioneId(salvata.getAzione().getId());
        response.setNomeAzione(salvata.getAzione().getNome());

        return response;
    }
}
