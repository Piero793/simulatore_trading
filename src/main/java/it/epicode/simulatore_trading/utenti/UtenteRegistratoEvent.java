package it.epicode.simulatore_trading.utenti;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UtenteRegistratoEvent extends ApplicationEvent {

    private final Utente utente;

    public UtenteRegistratoEvent(Utente source) {
        super(source);
        this.utente = (Utente) source;
    }

}
