package it.epicode.simulatore_trading.azioni;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AzioneRequest {
    @NotBlank
    private String nome;
    @Positive
    private double valoreAttuale;
    private double variazione;
    @NotNull
    private int quantita;
}
