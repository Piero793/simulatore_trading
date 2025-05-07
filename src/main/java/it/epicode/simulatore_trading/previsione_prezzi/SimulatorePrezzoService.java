package it.epicode.simulatore_trading.previsione_prezzi;

import it.epicode.simulatore_trading.azioni.Azione;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SimulatorePrezzoService {

    private final PrevisionePrezzoRepository previsionePrezzoRepository;
    private final Random random = new Random();

    public SimulatorePrezzoService(PrevisionePrezzoRepository previsionePrezzoRepository) {
        this.previsionePrezzoRepository = previsionePrezzoRepository;
    }

    public double simulaNuovoPrezzo(Azione azione) {
        return azione.getValoreAttuale() + (2 * random.nextDouble() - 1);
    }

    public int ottieniUltimoGiorno(Azione azione) {
        return previsionePrezzoRepository.findByAzione(azione).size();
    }
}
