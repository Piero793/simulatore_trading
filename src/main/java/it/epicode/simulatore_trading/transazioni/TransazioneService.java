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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
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
     * Recupera tutte le transazioni (probabilmente solo per l'utente autenticato in un'app reale).
     * Nota: Questo metodo non usa il nomeUtente passato, ma recupera tutte le transazioni.
     * Considero di rimuoverlo o modificarlo per recuperare le transazioni dell'utente autenticato.
     */
    public List<TransazioneResponse> getTransazioni() {
        List<Transazione> transazioni = transazioneRepository.findAll();

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
     * Recupera tutte le transazioni per l'utente specificato tramite nome utente.
     * Nota: Questo metodo si basa ancora sul nome utente. In un'applicazione reale
     * protetta da JWT, dovrei recuperare l'utente autenticato dal token.
     */
    public List<TransazioneResponse> getTransazioniPerUtente(String nomeUtente) {
        // Recupero l'utente tramite nome utente (meno sicuro rispetto al token)
        Utente utente = utenteRepository.findByNome(nomeUtente)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con nome: " + nomeUtente));

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


    // Registra una nuova transazione di acquisto o vendita utilizzando l'ID del portfolio.
    @Transactional // Assicura che l'operazione sia atomica
    public TransazioneResponse salvaTransazione(TransazioneRequest request) {

        Portfolio portfolio = portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new EntityNotFoundException("Portfolio non trovato con ID: " + request.getPortfolioId()));

        Utente utente = portfolio.getUtente();
        if (utente == null) {
            throw new EntityNotFoundException("Utente non associato al portfolio trovato.");
        }

        // Recupero l'azione dal database
        Azione azione = azioneRepository.findById(request.getAzioneId())
                .orElseThrow(() -> new EntityNotFoundException("Azione non trovata con ID: " + request.getAzioneId()));


        double valoreTotale = (double) request.getQuantita() * azione.getValoreAttuale(); // Cast a double per sicurezza

        // Logica di acquisto
        if ("Acquisto".equalsIgnoreCase(request.getTipoTransazione())) {
            if (utente.getSaldo() == null || utente.getSaldo() < valoreTotale) { // Controlla se il saldo è null
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

            int nuovaQuantita = quantitaPosseduta - request.getQuantita();
            if (nuovaQuantita > 0) {

                portfolio.aggiungiAzione(azione, -request.getQuantita());
            } else {
                // Se la quantità diventa zero o negativa, rimuovi l'azione dal portfolio.
                portfolio.rimuoviAzione(azione);
            }

        } else {
            throw new ConstraintViolationException("Errore: Tipo di transazione non valido!", null);
        }

        // Creo la transazione e la salvo
        Transazione nuovaTransazione = new Transazione();
        // Copia le proprietà dalla request
        BeanUtils.copyProperties(request, nuovaTransazione);
        nuovaTransazione.setAzione(azione);
        nuovaTransazione.setUtente(utente);
        nuovaTransazione.setPortfolio(portfolio);

        Transazione salvata = transazioneRepository.save(nuovaTransazione);

        // Aggiorna il saldo dell'utente nel database
        utenteRepository.save(utente);
        // Aggiorna il portfolio nel database
        portfolioRepository.save(portfolio);


        // Crea la response
        TransazioneResponse response = new TransazioneResponse();
        BeanUtils.copyProperties(salvata, response);
        response.setAzioneId(salvata.getAzione().getId());
        response.setNomeAzione(salvata.getAzione().getNome());

        return response;
    }
}
