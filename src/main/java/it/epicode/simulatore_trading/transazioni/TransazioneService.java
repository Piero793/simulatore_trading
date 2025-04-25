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

    @Transactional
    public TransazioneResponse salvaTransazione(TransazioneRequest request) {
        // Validazione del nome utente
        if (request.getNomeUtente() == null || request.getNomeUtente().isEmpty()) {
            throw new ConstraintViolationException("Il nome utente è obbligatorio!", null);
        }

        // Recupero l'utente
        Utente utente = utenteRepository.findByNome(request.getNomeUtente())
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        // Recupero l'azione dal database
        Azione azione = azioneRepository.findById(request.getAzioneId())
                .orElseThrow(() -> new EntityNotFoundException("Azione non trovata"));

        // Recupero il portfolio dell'utente
        Portfolio portfolio = portfolioRepository.findByNomeUtente(request.getNomeUtente())
                .orElseThrow(() -> new EntityNotFoundException("Portfolio non trovato"));

        double valoreTotale = request.getQuantita() * azione.getValoreAttuale();

        // Logica di acquisto
        if ("Acquisto".equalsIgnoreCase(request.getTipoTransazione())) {
            if (utente.getSaldo() < valoreTotale) {
                throw new ExceptionHandlerClass.InsufficientBalanceException("Saldo insufficiente per l'acquisto!");
            }
            utente.setSaldo(utente.getSaldo() - valoreTotale);
            utenteRepository.save(utente);

            portfolio.aggiungiAzione(azione, request.getQuantita());
            portfolioRepository.save(portfolio);
        }
        // Logica di vendita
        else if ("Vendita".equalsIgnoreCase(request.getTipoTransazione())) {
            int quantitaPosseduta = portfolio.getQuantitaAzione(azione);
            if (request.getQuantita() > quantitaPosseduta) {
                throw new ExceptionHandlerClass.InsufficientQuantityException("Quantità insufficiente di azioni per la vendita!");
            }
            utente.setSaldo(utente.getSaldo() + valoreTotale);
            utenteRepository.save(utente);

            // Rimuovi o aggiorna la quantità di azioni nel portfolio
            int nuovaQuantita = quantitaPosseduta - request.getQuantita();
            if (nuovaQuantita > 0) {
                portfolio.aggiungiAzione(azione, nuovaQuantita - quantitaPosseduta); // Aggiorna la quantità
            } else {
                portfolio.rimuoviAzione(azione);
            }
            portfolioRepository.save(portfolio);
        } else {
            throw new ConstraintViolationException("Errore: Tipo di transazione non valido!", null);
        }

        // Creo la transazione e la salvo
        Transazione nuovaTransazione = new Transazione();
        BeanUtils.copyProperties(request, nuovaTransazione);
        nuovaTransazione.setAzione(azione);
        nuovaTransazione.setQuantita(request.getQuantita());
        Transazione salvata = transazioneRepository.save(nuovaTransazione);

        TransazioneResponse response = new TransazioneResponse();
        BeanUtils.copyProperties(salvata, response);
        response.setAzioneId(salvata.getAzione().getId());
        response.setNomeAzione(salvata.getAzione().getNome());
        response.setQuantita(salvata.getQuantita());

        return response;
    }
}