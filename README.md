# SimuTrade X - Backend API

Il backend di SimuTrade X è sviluppato con Java Spring Boot e fornisce un'API RESTful per la gestione della simulazione di trading. Gestisce l'autenticazione degli utenti, l'accesso ai dati di mercato (azioni), la logica delle transazioni simulate, la gestione del portfolio degli utenti e il calcolo delle previsioni dei prezzi basato su modelli matematici. I dati sono persistiti utilizzando un database PostgreSQL.

## Prerequisiti

Assicurati di avere installato i seguenti strumenti sul tuo sistema:

* **Java Development Kit (JDK):** Versione 21 o superiore.
* **Maven:** Versione 3.x.
* **PostgreSQL:** Un'istanza del database PostgreSQL deve essere in esecuzione e configurata.

## Configurazione del Database

L'applicazione si connette a un database PostgreSQL. È necessario configurare le seguenti variabili d'ambiente per specificare i dettagli della connessione. **Queste variabili devono essere impostate nell'ambiente in cui l'applicazione viene eseguita (sistema operativo, IDE, server di produzione).**

DB_URL=jdbc:postgresql://localhost:5432/simutrade  # Esempio
DB_USER=il_tuo_username_db
DB_PWD=la_tua_password_db

## Configurazione delle Variabili d'Ambiente

Oltre alle configurazioni del database, è necessario impostare la seguente variabile d'ambiente:

JWT_SECRET=la_tua_chiave_segreta_jwt

Questa variabile d'ambiente può essere impostata direttamente nel sistema operativo, tramite il tuo ambiente di sviluppo (come IntelliJ IDEA nella sezione "Environment variables" della configurazione di esecuzione), o tramite il sistema di gestione dei container in produzione.

## Build del Progetto

Per buildare il backend utilizzando Maven, esegui il seguente comando nella root del progetto (dove si trova il file `pom.xml`):

```bash
mvn clean install
```
## Esecuzione del Backend
Puoi eseguire l'applicazione backend in diversi modi:

Tramite IntelliJ IDEA: Apri il progetto in IntelliJ IDEA ed esegui la classe principale it.epicode.simulatore_trading.SimulatoreTradingApplication. Assicurati di aver configurato le variabili d'ambiente nella configurazione di esecuzione/debug dell'IDE.

Utilizzando il JAR (dopo la build con Maven):

Apri un terminale o prompt dei comandi.

Naviga fino alla directory target all'interno della root del tuo progetto backend.

Esegui il seguente comando:

java -jar simulatore_trading-0.0.1-SNAPSHOT.jar

Assicurati che le variabili d'ambiente necessarie (DB_URL, DB_USER, DB_PWD, JWT_SECRET) siano impostate nel tuo sistema prima di eseguire il comando Java.

Documentazione delle API (Swagger UI)
Questo backend espone una documentazione interattiva delle API utilizzando Swagger UI. Una volta che l'applicazione è in esecuzione, puoi accedere alla documentazione visitando il seguente URL nel tuo browser:

http://localhost:8080/swagger-ui.html

(Nota: la porta predefinita di Spring Boot è 8080. Se hai configurato una porta diversa nel tuo application.properties o application.yml, usa quella porta).

Note Aggiuntive
Il backend implementa l'autenticazione degli utenti tramite token JWT.
La persistenza dei dati è gestita tramite Spring Data JPA e interagisce con il database PostgreSQL.
Sono implementati endpoint RESTful per le seguenti funzionalità principali:
Gestione delle azioni (/api/azioni).
Gestione del portfolio degli utenti (/api/portfolio).
Gestione delle previsioni dei prezzi (/api/previsione).
Gestione delle transazioni (/api/transazioni).
Autenticazione e registrazione degli utenti (/api/auth, /api/utenti).
Recupero del saldo degli utenti (/api/utenti/saldo).
Il backend è progettato per comunicare con il frontend React tramite queste API RESTful, scambiando dati in formato JSON.
Per il calcolo delle previsioni dei prezzi, è utilizzata la libreria Apache Commons Math3, implementando modelli basati su regressione lineare e media mobile.
