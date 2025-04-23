package it.epicode.simulatore_trading.azioni;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/azioni")
@CrossOrigin(origins = "*")
public class AzioneController {
    @Autowired
    private AzioneService azioneService;

    @GetMapping
    public List<AzioneResponse> getAzioni() {
        return azioneService.getAzioni();
    }

    @PostMapping
    public ResponseEntity<AzioneResponse> salvaAzione(@Valid @RequestBody AzioneRequest request) {
        return ResponseEntity.ok(azioneService.salvaAzione(request));
    }
}
