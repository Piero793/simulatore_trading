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
    @NotBlank(message = "Il nome non può essere vuoto")
    private String nome;
    @Positive (message = "Il valore attuale deve essere positivo")
    private double valoreAttuale;
    private double variazione;
    @NotNull(message = "La quantità non può essere vuota")
    private int quantita;
}
