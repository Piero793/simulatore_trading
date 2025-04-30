package it.epicode.simulatore_trading.transazioni;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransazioneRequest {

    @NotBlank(message = "Il tipo transazione non può essere vuoto")
    @Pattern(regexp = "Acquisto|Vendita", message = "Tipo transazione deve essere 'Acquisto' o 'Vendita'")
    private String tipoTransazione; // "Acquisto" o "Vendita"

    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private int quantita;

    @Positive(message = "Il prezzo unitario deve essere positivo")
    private double prezzoUnitario;

    @NotNull(message = "L'ID dell'azione non può essere nullo")
    private Long azioneId;

    @NotNull(message = "L'ID del portfolio non può essere nullo")
    private Long portfolioId;
}
