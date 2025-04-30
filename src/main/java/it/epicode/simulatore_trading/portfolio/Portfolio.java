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
    private Utente utente; // Un portfolio appartiene a un utente


    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioAzione> portfolioAzioni = new ArrayList<>();

    // Un portfolio può avere molte transazioni
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

    public void rimuoviAzione(Azione azione) {
        portfolioAzioni.removeIf(pa -> pa.getAzione().equals(azione));
    }

    // Metodo per aggiornare la quantità di un'azione esistente nel portfolio
    public void aggiornaQuantitaAzione(Azione azione, int nuovaQuantita) {
        portfolioAzioni.stream()
                .filter(pa -> pa.getAzione().equals(azione))
                .findFirst()
                .ifPresent(pa -> pa.setQuantita(nuovaQuantita));
    }


    public int getQuantitaAzione(Azione azione) {
        return portfolioAzioni.stream()
                .filter(pa -> pa.getAzione().equals(azione))
                .mapToInt(PortfolioAzione::getQuantita)
                .findFirst()
                .orElse(0);
    }

    // Metodo per ottenere la lista di azioni (potrebbe essere utile per la response)
    public List<Azione> getAzioni() {
        return portfolioAzioni.stream()
                .map(PortfolioAzione::getAzione)
                .toList();
    }
}
