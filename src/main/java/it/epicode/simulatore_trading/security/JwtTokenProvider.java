package it.epicode.simulatore_trading.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String jwtSecret;
    private final int jwtExpirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") int jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        // Ottiene i dettagli dell'utente autenticato
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Data di scadenza del token
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Costruisce il token JWT
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Il subject del token è lo username (email)
                .setIssuedAt(new Date()) // Data di emissione
                .setExpiration(expiryDate) // Data di scadenza
                .signWith(key(), SignatureAlgorithm.HS256) // Firma il token con la chiave segreta usando HS256
                .compact(); // Compatta il token in una stringa
    }


    private Key key() {
        // Decodifica la chiave segreta dalla stringa base64
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    public String getUsernameFromToken(String token) {
        // Parsa il token e ottiene i claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key()) // Usa la chiave segreta per verificare la firma
                .build()
                .parseClaimsJws(token)
                .getBody(); // Ottiene il corpo del token (i claims)

        // Restituisce il subject (che è lo username/email)
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            // Parsa e verifica la firma del token
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true; // Token valido
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        }
        return false; // Token non valido
    }
}
