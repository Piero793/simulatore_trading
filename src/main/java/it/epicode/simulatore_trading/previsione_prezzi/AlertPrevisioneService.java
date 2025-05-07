package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertPrevisioneService {

    private final PrevisionePrezzoRepository previsionePrezzoRepository;
    private final AzioneRepository azioneRepository;
    private final double sogliaVariazione;

    public AlertPrevisioneService(PrevisionePrezzoRepository previsionePrezzoRepository, AzioneRepository azioneRepository, @Value("${previsione.soglia-variazione}") double sogliaVariazione) {
        this.previsionePrezzoRepository = previsionePrezzoRepository;
        this.azioneRepository = azioneRepository;
        this.sogliaVariazione = sogliaVariazione;
    }

    public String verificaPrevisione(Long azioneId) {
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata!"));

        List<PrevisionePrezzo> datiStorici = previsionePrezzoRepository.findByAzione(azione);

        if (datiStorici.size() < 2) return "❌ Nessun dato sufficiente per verificare la variazione!";

        double ultimaPrevisione = datiStorici.get(datiStorici.size() - 2).getPrezzoPrevisto();
        double nuovaPrevisione = datiStorici.getLast().getPrezzoPrevisto();
        double variazione = Math.abs((nuovaPrevisione - ultimaPrevisione) / ultimaPrevisione);

        if (variazione > this.sogliaVariazione) {
            return "🚨 ALLERTA: La previsione di " + azione.getNome() + " è cambiata significativamente! Nuovo prezzo previsto: "
                    + String.format("%.2f", nuovaPrevisione) + "€ (variazione del " + String.format("%.2f", variazione * 100) + "%)";
        }

        return "✅ Nessuna variazione significativa per l'azione " + azione.getNome() + ".";
    }
}