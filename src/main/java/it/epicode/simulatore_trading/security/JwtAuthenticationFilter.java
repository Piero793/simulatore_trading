package it.epicode.simulatore_trading.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Intercetta le richieste HTTP per estrarre e validare il token JWT.
     *
     * @param request La richiesta HTTP in ingresso.
     * @param response La risposta HTTP in uscita.
     * @param filterChain La catena di filtri.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Estrae il token JWT dall'header 'Authorization'
            String jwt = getJwtFromRequest(request);

            // 2. Se il token esiste ed è valido
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 3. Estrae lo username (email) dal token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // 4. Carica i dettagli dell'utente usando lo username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Se i dettagli dell'utente sono stati caricati con successo
                // e l'utente non è già autenticato nel contesto di sicurezza corrente
                if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 6. Crea un oggetto di autenticazione per Spring Security
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credenziali (non necessarie dopo la validazione del token)
                            userDetails.getAuthorities()); // Autorità (ruoli)

                    // 7. Imposta i dettagli della richiesta nell'oggetto di autenticazione
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 8. Imposta l'oggetto di autenticazione nel contesto di sicurezza di Spring
                    // Questo indica a Spring Security che l'utente è autenticato per questa richiesta
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // In caso di errori (token non valido, utente non trovato, ecc.)
            // Spring Security gestirà l'errore tramite l'AuthenticationEntryPoint configurato
            // Non impostiamo l'autenticazione nel contesto, lasciando che la catena di filtri
            // proceda e venga intercettata dall'AuthenticationEntryPoint se l'endpoint è protetto.
            logger.error("Could not set user authentication in security context", ex);
        }

        // 9. Continua la catena di filtri
        filterChain.doFilter(request, response);
    }

    /**
     * Estrae il token JWT dall'header 'Authorization' della richiesta.
     * Si aspetta il formato "Bearer TOKEN".
     *
     * @param request La richiesta HTTP.
     * @return Il token JWT o null se non presente o non nel formato corretto.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Controlla se l'header Authorization è presente e inizia con "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Estrae il token rimuovendo il prefisso "Bearer "
            return bearerToken.substring(7);
        }
        return null; // Nessun token trovato
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
}
