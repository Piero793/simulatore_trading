package it.epicode.simulatore_trading.azioni;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AzioneService {

    @Autowired
    private AzioneRepository azioneRepository;

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
        if (request.getNome() == null || request.getNome().isEmpty()) {
            throw new ConstraintViolationException("Il nome dell'azione è obbligatorio!", null);
        }
        if (request.getQuantita() < 0) {
            throw new ConstraintViolationException("La quantità non può essere negativa!", null);
        }

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