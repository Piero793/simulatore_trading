package it.epicode.simulatore_trading.transazioni;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.portfolio.Portfolio;
import it.epicode.simulatore_trading.portfolio.PortfolioRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<TransazioneResponse> getTransazioni() {
        return transazioneRepository.findAll().stream().map(transazione -> {
            TransazioneResponse response = new TransazioneResponse();
            BeanUtils.copyProperties(transazione, response);
            response.setAzioneId(transazione.getAzione().getId());
            response.setNomeAzione(transazione.getAzione().getNome());
            return response;
        }).collect(Collectors.toList());
    }

    public TransazioneResponse salvaTransazione(TransazioneRequest request) {
        //  Valido che il nome utente sia presente
        if (request.getNomeUtente() == null || request.getNomeUtente().isEmpty()) {
            throw new RuntimeException("Errore: Nome utente non fornito!");
        }

        // Recupero l'azione dal database
        Azione azione = azioneRepository.findById(request.getAzioneId())
                .orElseThrow(() -> new RuntimeException("Azione non trovata"));

        // Creo la transazione e salvo la quantità correttamente
        Transazione nuovaTransazione = new Transazione();
        BeanUtils.copyProperties(request, nuovaTransazione);
        nuovaTransazione.setAzione(azione);
        nuovaTransazione.setQuantita(request.getQuantita());

        // Salvo la transazione nel database
        Transazione salvata = transazioneRepository.save(nuovaTransazione);

        // Recupero il portfolio dell'utente
        Portfolio portfolio = portfolioRepository.findByNomeUtente(request.getNomeUtente())
                .orElseThrow(() -> new RuntimeException("Portfolio non trovato"));

        if (portfolio.getAzioni() == null) {
            portfolio.setAzioni(new ArrayList<>());
        }

        // Logica di acquisto/vendita con gestione quantità
        if ("Acquisto".equalsIgnoreCase(request.getTipoTransazione())) {
            portfolio.getAzioni().stream()
                    .filter(a -> a.getId().equals(azione.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            a -> a.setQuantita(a.getQuantita() + request.getQuantita()), // Se esiste, aggiorna la quantità
                            () -> {
                                azione.setQuantita(request.getQuantita()); // Se non esiste, aggiungiamo con la quantità giusta
                                portfolio.getAzioni().add(azione);
                            }
                    );
        } else if ("Vendita".equalsIgnoreCase(request.getTipoTransazione())) {
            portfolio.getAzioni().stream()
                    .filter(a -> a.getId().equals(azione.getId()))
                    .findFirst()
                    .ifPresent(a -> {
                        int nuovaQuantita = a.getQuantita() - request.getQuantita();
                        if (nuovaQuantita > 0) {
                            a.setQuantita(nuovaQuantita); // Se rimangono azioni, aggiorna la quantità
                        } else {
                            portfolio.getAzioni().removeIf(az -> az.getId().equals(azione.getId())); // Se la quantità è 0, rimuoviamo l'azione
                        }
                    });
        } else {
            throw new RuntimeException("Errore: Tipo di transazione non valido!");
        }

        // Salvo il portfolio aggiornato
        portfolioRepository.save(portfolio);

        // Preparo la risposta con la quantità corretta
        TransazioneResponse response = new TransazioneResponse();
        BeanUtils.copyProperties(salvata, response);
        response.setAzioneId(salvata.getAzione().getId());
        response.setNomeAzione(salvata.getAzione().getNome());
        response.setQuantita(salvata.getQuantita());

        return response;
    }
}