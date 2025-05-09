package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.exceptions.ExceptionHandlerClass;
import it.epicode.simulatore_trading.portfolio.Portfolio;
import it.epicode.simulatore_trading.utenti.Utente;
import it.epicode.simulatore_trading.utenti.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransazioneService {

    private final TransazioneRepository transazioneRepository;
    private final AzioneRepository azioneRepository;
    private final UtenteRepository utenteRepository;
    private final TradingService tradingService;

    public TransazioneService(TransazioneRepository transazioneRepository,
                              AzioneRepository azioneRepository, UtenteRepository utenteRepository,
                              TradingService tradingService) {
        this.transazioneRepository = transazioneRepository;
        this.azioneRepository = azioneRepository;
        this.utenteRepository = utenteRepository;
        this.tradingService = tradingService;
    }


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


    @Transactional
    public TransazioneResponse salvaTransazione(TransazioneRequest request, Long userId) throws AccessDeniedException,
            ExceptionHandlerClass.InsufficientBalanceException, ExceptionHandlerClass.InsufficientQuantityException {
        log.info("Salvataggio transazione per utente con ID: {}", userId);

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Portfolio portfolio = utente.getPortfolio();
        if (portfolio == null) {
            throw new EntityNotFoundException("Portfolio non trovato per l'utente con ID: " + userId);
        }

        if (!portfolio.getId().equals(request.getPortfolioId())) {
            log.warn("Tentativo di transazione sul portfolio {} da parte dell'utente {}, ma non corrispondono.",
                    request.getPortfolioId(), userId);
            throw new AccessDeniedException("Accesso negato a questo portfolio.");
        }

        Azione azione = azioneRepository.findById(request.getAzioneId())
                .orElseThrow(() -> new EntityNotFoundException("Azione non trovata con ID: " + request.getAzioneId()));

        Transazione nuovaTransazione = new Transazione();
        BeanUtils.copyProperties(request, nuovaTransazione);
        nuovaTransazione.setAzione(azione);
        nuovaTransazione.setUtente(utente);
        nuovaTransazione.setPortfolio(portfolio);

        if ("Acquisto".equalsIgnoreCase(request.getTipoTransazione())) {
            tradingService.eseguiAcquisto(utente, portfolio, azione, request.getQuantita());
        } else if ("Vendita".equalsIgnoreCase(request.getTipoTransazione())) {
            tradingService.eseguiVendita(utente, portfolio, azione, request.getQuantita());
        } else {
            throw new ConstraintViolationException("Errore: Tipo di transazione non valido!", null);
        }

        Transazione salvata = transazioneRepository.save(nuovaTransazione);

        TransazioneResponse response = new TransazioneResponse();
        BeanUtils.copyProperties(salvata, response);
        response.setAzioneId(salvata.getAzione().getId());
        response.setNomeAzione(salvata.getAzione().getNome());

        return response;
    }
}