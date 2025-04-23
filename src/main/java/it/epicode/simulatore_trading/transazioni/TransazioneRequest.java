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

    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private int quantita; //  Deve essere almeno 1

    @Positive(message = "Il prezzo unitario deve essere positivo")
    private double prezzoUnitario; //  Valore positivo obbligatorio

    @NotNull(message = "L'ID dell'azione non può essere nullo")
    private Long azioneId; //  ID dell’azione collegata

    @NotBlank(message = "Il nome utente non può essere vuoto")
    private String nomeUtente;
}