package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrevisionePrezzoPrincipaleService {

    private static final Logger logger = LoggerFactory.getLogger(PrevisionePrezzoPrincipaleService.class);

    private final PrevisionePrezzoRepository previsionePrezzoRepository;
    private final AzioneRepository azioneRepository;
    private final SimulatorePrezzoService simulatorePrezzoService;
    private final PrevisionePrezzoService previsionePrezzoService;
    private final AlertPrevisioneService alertPrevisioneService;

    public PrevisionePrezzoPrincipaleService(PrevisionePrezzoRepository previsionePrezzoRepository,
                                             AzioneRepository azioneRepository,
                                             SimulatorePrezzoService simulatorePrezzoService,
                                             PrevisionePrezzoService previsionePrezzoService,
                                             AlertPrevisioneService alertPrevisioneService) {
        this.previsionePrezzoRepository = previsionePrezzoRepository;
        this.azioneRepository = azioneRepository;
        this.simulatorePrezzoService = simulatorePrezzoService;
        this.previsionePrezzoService = previsionePrezzoService;
        this.alertPrevisioneService = alertPrevisioneService;
    }

    @Scheduled(fixedDelayString = "${previsione.aggiornamento-delay-ms}")
    public void aggiornaDatiStorici() {
        List<Azione> azioni = azioneRepository.findAll();

        for (Azione azione : azioni) {
            double nuovoPrezzo = simulatorePrezzoService.simulaNuovoPrezzo(azione);
            int prossimoGiorno = simulatorePrezzoService.ottieniUltimoGiorno(azione) + 1;
            PrevisionePrezzo nuovoDato = new PrevisionePrezzo(null, azione, prossimoGiorno, nuovoPrezzo);
            previsionePrezzoRepository.save(nuovoDato);

            logger.info("Nuovo prezzo registrato per {}: {}", azione.getNome(), nuovoPrezzo);

            String messaggioAllerta = alertPrevisioneService.verificaPrevisione(azione.getId());
            if (messaggioAllerta.startsWith("🚨")) {
                logger.warn(messaggioAllerta);
            } else {
                logger.info(messaggioAllerta);
            }
        }
    }

    public double prevediPrezzoPerAzione(Long azioneId) {
        return previsionePrezzoService.prevediPrezzoPerAzione(azioneId);
    }
}