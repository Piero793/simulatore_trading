package it.epicode.simulatore_trading.transazioni;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransazioneRequest {
    @NotBlank
    @Pattern(regexp = "Acquisto|Vendita", message = "Tipo transazione deve essere 'Acquisto' o 'Vendita'")
    private String tipoTransazione; //  "Acquisto" o "Vendita"
    @Min(1)
    private int quantita; //  Deve essere almeno 1
    @Positive
    private double prezzoUnitario; //  Valore positivo obbligatorio
    @NotNull
    private Long azioneId; //  ID dell’azione collegata
    @NotBlank
    private String nomeUtente;
}