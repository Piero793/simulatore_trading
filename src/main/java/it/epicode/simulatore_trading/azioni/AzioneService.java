package it.epicode.simulatore_trading.azioni;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AzioneService {

    private final AzioneRepository azioneRepository;

    public AzioneService(AzioneRepository azioneRepository) {
        this.azioneRepository = azioneRepository;
    }

    public List<AzioneResponse> getAzioni() {
        List<Azione> azioni = azioneRepository.findAll();

        if (azioni.isEmpty()) {
            throw new EntityNotFoundException("Nessuna azione trovata!");
        }

        return azioni.stream()
                .map(azione -> new AzioneResponse(
                        azione.getId(),
                        azione.getNome(),
                        azione.getValoreAttuale(),
                        azione.getVariazione(),
                        azione.getQuantita()
                ))
                .collect(Collectors.toList());
    }

    public AzioneResponse salvaAzione(AzioneRequest request) {
        Azione nuovaAzione = new Azione(
                null,
                request.getNome(),
                request.getValoreAttuale(),
                request.getVariazione(),
                request.getQuantita()
        );

        Azione salvata = azioneRepository.save(nuovaAzione);
        return new AzioneResponse(
                salvata.getId(),
                salvata.getNome(),
                salvata.getValoreAttuale(),
                salvata.getVariazione(),
                salvata.getQuantita()
        );
    }
}