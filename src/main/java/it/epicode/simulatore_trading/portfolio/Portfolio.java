package it.epicode.simulatore_trading.portfolio;


import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.transazioni.Transazione;
import it.epicode.simulatore_trading.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Utente utente;


    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioAzione> portfolioAzioni = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transazione> transazioni = new ArrayList<>();


    // Metodi helper per aggiungere e rimuovere azioni con quantità
    public void aggiungiAzione(Azione azione, int quantita) {
        // Verifica se l'azione è già presente nel portfolio
        portfolioAzioni.stream()
                .filter(pa -> pa.getAzione().equals(azione))
                .findFirst()
                .ifPresentOrElse(
                        pa -> pa.setQuantita(pa.getQuantita() + quantita), // Se esiste, aggiorna la quantità
                        () -> {
                            PortfolioAzione portfolioAzione = new PortfolioAzione(this, azione, quantita);
                            portfolioAzioni.add(portfolioAzione);
                        }
                );
    }

    public int getQuantitaAzione(Azione azione) {
        return portfolioAzioni.stream()
                .filter(pa -> pa.getAzione().equals(azione))
                .mapToInt(PortfolioAzione::getQuantita)
                .findFirst()
                .orElse(0);
    }

    // Metodo per ottenere la lista di azioni
    public List<Azione> getAzioni() {
        return portfolioAzioni.stream()
                .map(PortfolioAzione::getAzione)
                .toList();
    }
}
