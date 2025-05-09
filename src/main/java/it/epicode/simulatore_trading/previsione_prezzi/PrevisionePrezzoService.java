package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrevisionePrezzoService {

    private final PrevisionePrezzoRepository previsionePrezzoRepository;
    private final AzioneRepository azioneRepository;

    public PrevisionePrezzoService(PrevisionePrezzoRepository previsionePrezzoRepository,
                                   AzioneRepository azioneRepository) {
        this.previsionePrezzoRepository = previsionePrezzoRepository;
        this.azioneRepository = azioneRepository;
    }

    public double prevediPrezzoPerAzione(Long azioneId) {
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata!"));

        List<PrevisionePrezzo> datiStorici = previsionePrezzoRepository.findByAzioneOrderByGiornoAsc(azione);

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

    public Azione getAzioneById(Long azioneId) {
        return azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata con ID: " + azioneId));
    }

    public List<PrevisionePrezzo> getDatiStoriciPerAzione(Azione azione) {
        List<PrevisionePrezzo> datiStorici = previsionePrezzoRepository.findByAzioneOrderByGiornoAsc(azione);
        if (datiStorici.isEmpty()) {
            throw new ConstraintViolationException("Errore: Nessun dato storico disponibile per questa azione!", null);
        }
        return datiStorici;
    }

    public List<PrevisionePrezzo> calcolaDatiStoriciPrevisione(Long azioneId) {
        Azione azione = azioneRepository.findById(azioneId)
                .orElseThrow(() -> new EntityNotFoundException("Errore: Azione non trovata!"));

        List<PrevisionePrezzo> datiStoriciReali = previsionePrezzoRepository.findByAzioneOrderByGiornoAsc(azione);
        if (datiStoriciReali.isEmpty()) {
            throw new ConstraintViolationException("Errore: Nessun dato reale disponibile per questa azione!", null);
        }

        List<PrevisionePrezzo> datiStoriciPrevisione = new ArrayList<>();
        SimpleRegression regression = new SimpleRegression();
        int finestra = Math.min(datiStoriciReali.size(), 5);

        // Calcola la previsione per OGNI giorno storico
        for (PrevisionePrezzo datoReale : datiStoriciReali) {
            // Resetta la regressione per ogni punto
            regression.clear();

            // Aggiungi i dati storici FINO al giorno corrente per la regressione
            for (PrevisionePrezzo datoPrecedente : datiStoriciReali) {
                if (datoPrecedente.getGiorno() <= datoReale.getGiorno()) {
                    regression.addData(datoPrecedente.getGiorno(), datoPrecedente.getPrezzoPrevisto());
                }
            }

            double sommaPrezziRecenti = 0;
            int countRecenti = 0;
            for (int i = Math.max(0, datiStoriciReali.size() - finestra); i < datiStoriciReali.size(); i++) {
                if (datiStoriciReali.get(i).getGiorno() <= datoReale.getGiorno()) {
                    sommaPrezziRecenti += datiStoriciReali.get(i).getPrezzoPrevisto();
                    countRecenti++;
                }
            }
            double mediaMobile = countRecenti > 0 ? sommaPrezziRecenti / countRecenti : datoReale.getPrezzoPrevisto();
            double previsioneRegressione = regression.predict(datoReale.getGiorno());

            double prezzoPrevisione = Double.isNaN(previsioneRegressione) ? mediaMobile : (previsioneRegressione +
                    mediaMobile) / 2;

            datiStoriciPrevisione.add(new PrevisionePrezzo(null, azione, datoReale.getGiorno(), prezzoPrevisione));
        }

        return datiStoriciPrevisione;
    }
}

/**
 * MODELLO DI PREVISIONE UTILIZZATO:
 *
 * Questo metodo per la previsione del prezzo di un'azione utilizza una combinazione
 * di due tecniche statistiche semplici per analizzare i dati storici e stimare
 * i prezzi futuri. Il modello applica queste tecniche retroattivamente su ogni
 * punto dati storico per generare una serie di previsioni storiche, utili per
 * la visualizzazione del trend previsto nel tempo.
 *
 * 1. Regressione Lineare Semplice (applicata storicamente):
 * - Per ogni giorno storico disponibile, il modello tenta di modellare la relazione
 * lineare tra il "giorno" (considerato come una sequenza temporale) e il prezzo
 * previsto, utilizzando i dati storici *fino a quel giorno*. L'obiettivo è
 * trovare una linea retta che meglio si adatti a questi dati passati e quindi
 * utilizzare questa linea per "prevedere" il prezzo per il giorno corrente.
 * Questo processo viene ripetuto per ogni giorno nel set di dati storici,
 * generando una serie di previsioni basate sulla regressione.
 *
 * 2. Media Mobile Semplice (applicata storicamente):
 * - Similmente alla regressione, per ogni giorno storico, viene calcolata la media
 * aritmetica dei prezzi previsti degli ultimi 'finestra' giorni (attualmente
 * impostata a un massimo di 5), considerando solo i dati storici *fino a quel
 * giorno*. Questa tecnica attenua le fluttuazioni a breve termine osservate
 * nel passato e fornisce un'indicazione del trend recente, calcolata
 * storicamente.
 *
 * La "previsione storica" finale per ogni giorno nel set di dati viene ottenuta
 * come una media aritmetica tra il valore previsto dalla regressione lineare
 * (calcolata sui dati fino a quel giorno) e la media mobile (calcolata sui dati
 * fino a quel giorno). Questa combinazione mira a sfruttare sia il potenziale
 * trend a lungo termine catturato dalla regressione (in base ai dati passati)
 * che la tendenza a breve termine indicata dalla media mobile (sempre basata
 * sui dati passati). La visualizzazione di questa serie di previsioni storiche
 * offre un'idea di come il modello avrebbe previsto il prezzo nel tempo.
 *
 * LIMITAZIONI:
 * - Questo è un modello molto semplificato e non tiene conto di molti fattori
 * reali che influenzano i prezzi delle azioni (volatilità del mercato, notizie,
 * indicatori economici, ecc.).
 * - La scelta di una finestra fissa per la media mobile (5 giorni) è arbitraria
 * e potrebbe non essere ottimale per tutte le azioni o in tutte le condizioni
 * di mercato.
 * - La regressione lineare semplice assume una relazione lineare, che potrebbe
 * non essere sempre valida per i prezzi delle azioni. L'applicazione storica
 * non implica una capacità predittiva accurata per il futuro.
 */
