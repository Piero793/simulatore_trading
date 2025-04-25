package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.azioni.AzioneResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AzioneRepository azioneRepository;

    @Autowired
    private PortfolioAzioneRepository portfolioAzioneRepository;

    public PortfolioResponse getPortfolio(String nomeUtente) {
        Portfolio portfolio = portfolioRepository.findByNomeUtente(nomeUtente)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio non trovato"));

        PortfolioResponse response = new PortfolioResponse();
        BeanUtils.copyProperties(portfolio, response);
        response.setAzioni(portfolio.getPortfolioAzioni().stream()
                .map(pa -> new AzioneResponse(
                        pa.getAzione().getId(),
                        pa.getAzione().getNome(),
                        pa.getAzione().getValoreAttuale(),
                        pa.getAzione().getVariazione(),
                        pa.getQuantita()
                ))
                .collect(Collectors.toList()));

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

        Portfolio salvato = portfolioRepository.save(nuovoPortfolio);

        PortfolioResponse response = new PortfolioResponse();
        BeanUtils.copyProperties(salvato, response);
        response.setAzioni(new ArrayList<>());
        response.setId(salvato.getId());

        return response;
    }

    @Transactional
    public PortfolioResponse aggiungiAzione(Long portfolioId, Long azioneId, int quantita) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio non trovato"));
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Azione non trovata"));

        if (quantita <= 0) {
            throw new ConstraintViolationException("La quantità deve essere maggiore di zero!", null);
        }

        Optional<PortfolioAzione> existingPortfolioAzioneOptional = portfolioAzioneRepository.findByPortfolioAndAzione(portfolio, azione);

        if (existingPortfolioAzioneOptional.isPresent()) {
            // Se esiste, aggiorna la quantità
            PortfolioAzione existingPortfolioAzione = existingPortfolioAzioneOptional.get();
            existingPortfolioAzione.setQuantita(existingPortfolioAzione.getQuantita() + quantita);
            portfolioAzioneRepository.save(existingPortfolioAzione);
        } else {
            // Se non esiste, crea una nuova PortfolioAzione
            PortfolioAzione nuovaPortfolioAzione = new PortfolioAzione(portfolio, azione, quantita);
            portfolioAzioneRepository.save(nuovaPortfolioAzione);
        }

        return getPortfolio(portfolio.getNomeUtente());
    }
}