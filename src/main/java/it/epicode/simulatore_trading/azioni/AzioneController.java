package it.epicode.simulatore_trading.azioni;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/azioni")
public class AzioneController {

    private final AzioneService azioneService;

    public AzioneController(AzioneService azioneService) {
        this.azioneService = azioneService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AzioneResponse> getAzioni() {
        return azioneService.getAzioni();
    }

    @PostMapping
    public ResponseEntity<AzioneResponse> salvaAzione(@Valid @RequestBody AzioneRequest request) {
        AzioneResponse savedAzione = azioneService.salvaAzione(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedAzione.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedAzione);
    }
}