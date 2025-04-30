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

    private static final double SOGLIA_VARIAZIONE = 0.003; // Alert per variazioni superiori al 3%

    //  Task automatico: aggiorna il database ogni 2 minuti con nuovi dati
    @Scheduled(cron = "0 */2 * * * ?")
    public void aggiornaDatiStorici() {
        List<Azione> azioni = azioneRepository.findAll();
        Random random = new Random();

        for (Azione azione : azioni) {
            double nuovoPrezzo = azione.getValoreAttuale() + (2 * random.nextDouble() - 1);
            PrevisionePrezzo nuovoDato = new PrevisionePrezzo(null, azione, ottieniUltimoGiorno(azione) + 1, nuovoPrezzo);
            previsionePrezzoRepository.save(nuovoDato);

            System.out.println("✅ Nuovo prezzo registrato per " + azione.getNome() + ": " + nuovoPrezzo);

            verificaPrevisione(azione.getId());
        }
    }

    // Metodo helper per determinare l'ultimo "giorno" registrato per una data azione
    private int ottieniUltimoGiorno(Azione azione) {
        return previsionePrezzoRepository.findByAzione(azione).size();
    }

    // Metodo per prevedere il prezzo futuro di un asset specifico
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

    // Controlla le variazioni significative nelle previsioni per attivare un alert
    public String verificaPrevisione(Long azioneId) {
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata!"));

        List<PrevisionePrezzo> datiStorici = previsionePrezzoRepository.findByAzione(azione);

        if (datiStorici.size() < 2) return "❌ Nessun dato sufficiente per verificare la variazione!";

        double ultimaPrevisione = datiStorici.get(datiStorici.size() - 2).getPrezzoPrevisto();
        double nuovaPrevisione = datiStorici.getLast().getPrezzoPrevisto();
        double variazione = Math.abs((nuovaPrevisione - ultimaPrevisione) / ultimaPrevisione);

        if (variazione > SOGLIA_VARIAZIONE) {
            return "🚨 ALERT: La previsione di " + azione.getNome() + " è cambiata significativamente! Nuovo prezzo previsto: "
                    + String.format("%.2f", nuovaPrevisione) + "€ (variazione del " + String.format("%.2f", variazione * 100) + "%)";
        }

        return "✅ Nessuna variazione significativa per l'azione " + azione.getNome() + ".";
    }
}

/*
 * SPIEGAZIONE DELLA CLASSE PrevisionePrezzoService:
 *
 * Questa classe Service gestisce la simulazione, la registrazione e la previsione
 * del prezzo delle azioni all'interno di un simulatore di trading.
 * Utilizza Spring per la gestione delle dipendenze e la schedulazione dei task.
 *
 * Dipendenze:
 * - PrevisionePrezzoRepository: Per l'accesso ai dati storici delle previsioni di prezzo.
 * - AzioneRepository: Per l'accesso ai dati delle azioni.
 *
 * Costanti:
 * - SOGLIA_VARIAZIONE: Definisce la soglia di variazione percentuale (0.3% = 0.003)
 *   al di sopra della quale viene generato un alert.
 *
 * Metodi:
 *
 * 1. aggiornaDatiStorici():
 * - Metodo schedulato per l'esecuzione automatica ogni 2 minuti.
 * - Simula e registra nuovi dati di prezzo per tutte le azioni presenti nel database.
 * - Per ogni azione:
 *   i. Genera un nuovo prezzo simulando una piccola variazione casuale (+/- 1).
 *   ii. Determina il giorno successivo.
 *   iii. Crea e salva un nuovo record PrevisionePrezzo.
 *   iv. Stampa un messaggio e controlla se serve un alert.
 *
 * 2. ottieniUltimoGiorno(Azione azione):
 * - Metodo helper privato per determinare il giorno successivo.
 * - Calcola il numero di dati storici esistenti per l’azione.
 *
 * 3. prevediPrezzoPerAzione(Long azioneId):
 * - Prevede il prezzo futuro per un’azione specifica.
 * - Utilizza:
 *   a. Regressione lineare semplice (Giorno vs Prezzo).
 *   b. Media mobile degli ultimi 5 prezzi (o meno se non disponibili).
 * - Combina i due risultati per una previsione più robusta.
 *
 * 4. verificaPrevisione(Long azioneId):
 * - Controlla se l'ultima previsione rappresenta una variazione significativa rispetto alla precedente.
 * - Se sì, genera un messaggio di alert formattato.
 * - Altrimenti, restituisce un messaggio di stato normale.
 *
 * In sintesi:
 * - La classe simula l’aggiornamento continuo dei prezzi.
 * - Utilizza metodi statistici per stimare le evoluzioni future.
 * - Notifica eventuali variazioni significative.
 */
