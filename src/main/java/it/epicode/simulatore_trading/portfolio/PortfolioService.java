package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.azioni.AzioneResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AzioneRepository azioneRepository;

    public PortfolioResponse getPortfolio(String nomeUtente) {
        Portfolio portfolio = portfolioRepository.findByNomeUtente(nomeUtente)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio non trovato"));

        PortfolioResponse response = new PortfolioResponse();
        BeanUtils.copyProperties(portfolio, response);

        response.setAzioni(portfolio.getAzioni() != null ? portfolio.getAzioni().stream()
                .map(azione -> new AzioneResponse(
                        azione.getId(),
                        azione.getNome(),
                        azione.getValoreAttuale(),
                        azione.getVariazione(),
                        azione.getQuantita()
                ))
                .collect(Collectors.toList()) : new ArrayList<>());

        return response;
    }

    public PortfolioResponse creaPortfolio(PortfolioRequest request) {
        if (request.getNomeUtente() == null || request.getNomeUtente().isEmpty()) {
            throw new ConstraintViolationException("Il nome utente non può essere vuoto!", null);
        }

        if (portfolioRepository.findByNomeUtente(request.getNomeUtente()).isPresent()) {
            throw new ConstraintViolationException("Esiste già un portfolio per questo utente!", null);
        }

        Portfolio nuovoPortfolio = new Portfolio();
        BeanUtils.copyProperties(request, nuovoPortfolio);
        nuovoPortfolio.setAzioni(new ArrayList<>());

        Portfolio salvato = portfolioRepository.save(nuovoPortfolio);

        PortfolioResponse response = new PortfolioResponse();
        BeanUtils.copyProperties(salvato, response);
        response.setAzioni(new ArrayList<>());

        return response;
    }

    public PortfolioResponse aggiungiAzione(Long portfolioId, Long azioneId, int quantita) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio non trovato"));
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Azione non trovata"));

        if (quantita <= 0) {
            throw new ConstraintViolationException("La quantità deve essere maggiore di zero!", null);
        }

        if (portfolio.getAzioni() == null) {
            portfolio.setAzioni(new ArrayList<>());
        }

        portfolio.getAzioni().stream()
                .filter(a -> a.getId().equals(azione.getId()))
                .findFirst()
                .ifPresentOrElse(
                        a -> a.setQuantita(a.getQuantita() + quantita), // Se esiste, aggiorna la quantità
                        () -> {
                            azione.setQuantita(quantita); // Se non esiste, aggiungiamo l'azione con quantità corretta
                            portfolio.getAzioni().add(azione);
                        }
                );

        portfolioRepository.save(portfolio);

        return getPortfolio(portfolio.getNomeUtente());
    }
}