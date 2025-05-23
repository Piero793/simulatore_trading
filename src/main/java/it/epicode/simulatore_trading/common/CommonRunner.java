package it.epicode.simulatore_trading.common;

import it.epicode.simulatore_trading.azioni.Azione;
import it.epicode.simulatore_trading.azioni.AzioneRepository;
import it.epicode.simulatore_trading.transazioni.TransazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import java.util.Random;

@Component
public class CommonRunner implements CommandLineRunner {

    @Autowired
    private AzioneRepository azioneRepository;

    @Autowired
    private TransazioneRepository transazioneRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        generaAzioniCasuali();
    }

    private void generaAzioniCasuali() {
        for (int i = 0; i < 20; i++) {
            Azione azione = new Azione();
            azione.setNome(faker.company().name());
            azione.setValoreAttuale(50 + (random.nextDouble() * 450)); // Prezzo casuale tra 50 e 500
            azione.setVariazione((random.nextDouble() * 6) - 3); // Variazione tra -3% e +3%
            azioneRepository.save(azione);
        }
        System.out.println("✅ Azioni casuali generate!");
    }
}