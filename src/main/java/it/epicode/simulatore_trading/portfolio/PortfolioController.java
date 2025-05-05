package it.epicode.simulatore_trading.portfolio;

import it.epicode.simulatore_trading.utenti.Utente;
import it.epicode.simulatore_trading.utenti.UtenteService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioResponse getMyPortfolio() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            Long userId = ((Utente) authentication.getPrincipal()).getId();
            return portfolioService.getPortfolioByUserId(userId);
        }
        throw new AccessDeniedException("Utente non autenticato o informazioni insufficienti.");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse creaPortfolio() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            Long userId = ((Utente) authentication.getPrincipal()).getId();
            Utente utente = utenteService.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
            return portfolioService.creaPortfolio(utente);
        }
        throw new AccessDeniedException("Utente non autenticato o informazioni insufficienti.");
    }

    @PutMapping("/{portfolioId}/azioni/{azioneId}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioResponse aggiungiAzione(
            @PathVariable Long portfolioId,
            @PathVariable Long azioneId,
            @RequestParam int quantita
    ) throws AccessDeniedException {
        if (quantita <= 0) {
            throw new ConstraintViolationException("La quantità deve essere maggiore di zero!", null);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utente) {
            Long userId = ((Utente) authentication.getPrincipal()).getId();
            return portfolioService.aggiungiAzione(portfolioId, azioneId, quantita, userId);
        }
        throw new AccessDeniedException("Utente non autenticato o informazioni insufficienti.");
    }
}