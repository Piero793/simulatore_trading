package it.epicode.simulatore_trading.portfolio;


import org.springframework.data.jpa.repository.JpaRepository;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
