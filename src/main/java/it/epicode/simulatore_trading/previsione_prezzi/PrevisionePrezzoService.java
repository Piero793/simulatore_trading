package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.Random;
import org.apache.commons.math3.stat.regression.SimpleRegression;


import java.util.List;

@Service
public class PrevisionePrezzoService {

    @Autowired
    private PrevisionePrezzoRepository previsionePrezzoRepository;

    @Autowired
    private AzioneRepository azioneRepository;

    private static final double SOGLIA_VARIAZIONE = 0.05; // Alert per variazioni superiori al 5%

    // Task automatico: aggiorna il database ogni 10 minuti con nuovi dati per ogni asset
    @Scheduled(cron = "0 */10 * * * ?")
    public void aggiornaDatiStorici() {
        List<Azione> azioni = azioneRepository.findAll();
        Random random = new Random();

        for (Azione azione : azioni) {
            double nuovoPrezzo = azione.getValoreAttuale() + (2 * random.nextDouble() - 1); // Simula una variazione casuale
            PrevisionePrezzo nuovoDato = new PrevisionePrezzo(null, azione, ottieniUltimoGiorno(azione) + 1, nuovoPrezzo);
            previsionePrezzoRepository.save(nuovoDato);

            System.out.println("✅ Nuovo prezzo registrato per " + azione.getNome() + ": " + nuovoPrezzo);

            // Controllo se serve un alert
            verificaPrevisione(azione.getId(), nuovoPrezzo);
        }
    }

    private int ottieniUltimoGiorno(Azione azione) {
        return previsionePrezzoRepository.findByAzione(azione).size();
    }

    // Metodo per prevedere il prezzo di un asset specifico
    public double prevediPrezzoPerAzione(Long azioneId) {
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata!"));

        List<PrevisionePrezzo> datiStorici = previsionePrezzoRepository.findByAzione(azione);

        if (datiStorici.isEmpty()) {
            throw new ConstraintViolationException("Errore: Nessun dato disponibile per questa azione!", null);
        }

        SimpleRegression regression = new SimpleRegression();
        double sommaPrezziRecenti = 0;
        int finestra = Math.min(datiStorici.size(), 5);

        for (PrevisionePrezzo dato : datiStorici) {
            regression.addData(dato.getGiorno(), dato.getPrezzoPrevisto());
        }

        for (int i = Math.max(0, datiStorici.size() - finestra); i < datiStorici.size(); i++) {
            sommaPrezziRecenti += datiStorici.get(i).getPrezzoPrevisto();
        }
        double mediaMobile = sommaPrezziRecenti / finestra;

        double previsioneRegressione = regression.predict(datiStorici.size() + 1);

        return Double.isNaN(previsioneRegressione) ? mediaMobile : (previsioneRegressione + mediaMobile) / 2;
    }

    // Controllo le variazioni per attivare un alert
    public void verificaPrevisione(Long azioneId, double nuovaPrevisione) {
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata!"));

        List<PrevisionePrezzo> datiStorici = previsionePrezzoRepository.findByAzione(azione);

        if (datiStorici.size() < 2) return; // Se ci sono pochi dati, non attiviamo un alert

        double ultimaPrevisione = datiStorici.get(datiStorici.size() - 2).getPrezzoPrevisto();
        double variazione = Math.abs((nuovaPrevisione - ultimaPrevisione) / ultimaPrevisione);

        if (variazione > SOGLIA_VARIAZIONE) {
            System.out.println("🚨 ALERT: La previsione di " + azione.getNome() + " è cambiata significativamente! Nuovo prezzo previsto: "
                    + String.format("%.2f", nuovaPrevisione) + "€ (variazione del " + String.format("%.2f", variazione * 100) + "%)");
        }
    }
}