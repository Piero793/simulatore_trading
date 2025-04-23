package it.epicode.simulatore_trading.portfolio;

import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/{nomeUtente}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioResponse getPortfolio(@PathVariable String nomeUtente) {
        return portfolioService.getPortfolio(nomeUtente);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse creaPortfolio(@RequestBody PortfolioRequest request) {
        return portfolioService.creaPortfolio(request);
    }

    @PutMapping("/{portfolioId}/aggiungiAzione/{azioneId}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioResponse aggiungiAzione(
            @PathVariable Long portfolioId,
            @PathVariable Long azioneId,
            @RequestParam int quantita
    ) {
        if (quantita <= 0) {
            throw new ConstraintViolationException("La quantità deve essere maggiore di zero!", null);
        }

        return portfolioService.aggiungiAzione(portfolioId, azioneId, quantita);
    }
}
