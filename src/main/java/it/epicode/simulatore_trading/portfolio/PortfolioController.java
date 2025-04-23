package it.epicode.simulatore_trading.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/{nomeUtente}")
    public PortfolioResponse getPortfolio(@PathVariable String nomeUtente) {
        return portfolioService.getPortfolio(nomeUtente);
    }

    @PostMapping
    public PortfolioResponse creaPortfolio(@RequestBody PortfolioRequest request) {
        return portfolioService.creaPortfolio(request);
    }

    @PutMapping("/{portfolioId}/aggiungiAzione/{azioneId}")
    public PortfolioResponse aggiungiAzione(
            @PathVariable Long portfolioId,
            @PathVariable Long azioneId,
            @RequestParam int quantita
    ) {
        if (quantita <= 0) {
            throw new RuntimeException("Errore: La quantità deve essere maggiore di zero!");
        }

        return portfolioService.aggiungiAzione(portfolioId, azioneId, quantita);
    }
}

