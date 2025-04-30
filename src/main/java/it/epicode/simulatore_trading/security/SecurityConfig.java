package it.epicode.simulatore_trading.security;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // per usare @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Opzionale: abilita la sicurezza a livello di metodo (@PreAuthorize, @PostAuthorize, etc.)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;


    // Bean per l'encoder delle password (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean per l'AuthenticationManager (necessario per il processo di login)
    // Richiede il UserDetailsService e il PasswordEncoder
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    // --- CONFIGURAZIONE CORS GLOBALE ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permette richieste da qualsiasi origine. NON OK PER LA PRODUZIONE!
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // Metodi permessi
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type")); // Header permessi
        configuration.setAllowCredentials(false); // Di solito false con JWT
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    // --- CATENA DEI FILTRI DI SICUREZZA (SecurityFilterChain) ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disabilita CSRF per le API REST (stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Abilita CORS con la configurazione definita sopra
                .cors(Customizer.withDefaults())

                // Configura la gestione delle sessioni come stateless (no sessioni HTTP lato server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configura come gestire gli errori di autenticazione (es. utente non autenticato)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorizedHandler)
                )

                // Configura le regole di autorizzazione per le richieste HTTP
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.POST, "/api/utenti/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/utenti/login").permitAll()
                        // Permetti GET per gli endpoint pubblici che non richiedono POST
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // Endpoint per Swagger/OpenAPI

                        .requestMatchers(HttpMethod.GET, "/api/portfolio").permitAll()
                        // ---------------------------------------------------------------------------------

                        .requestMatchers("/api/utenti/saldo/**").hasRole("USER")
                        .requestMatchers("/api/**").hasRole("USER") // Tutti gli endpoint sotto /api/ richiedono il ruolo USER

                        // Qualsiasi altra richiesta richiede autenticazione
                        .anyRequest().authenticated()
                )

                // Aggiungi il filtro JWT PRIMA del filtro di autenticazione standard di Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
}
