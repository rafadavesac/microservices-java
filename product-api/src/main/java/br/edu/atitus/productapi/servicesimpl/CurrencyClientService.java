package br.edu.atitus.productapi.servicesimpl;

import org.springframework.stereotype.Service;
import br.edu.atitus.productapi.clients.CurrencyClient;
import br.edu.atitus.productapi.dtos.CurrencyResponse;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;

@Service
public class CurrencyClientService {
    private final CurrencyClient currencyClient;
    public CurrencyClientService(CurrencyClient currencyClient) {
        this.currencyClient = currencyClient;
    }
    @Cacheable(
            value = "currency-api",
            key = "#source + '-' + #target"
    )
    @Retry(name = "currency-api")
    @CircuitBreaker(name = "currency-api")
    public CurrencyResponse getCurrency(String source, String target) {
        return currencyClient.getCurrency(source, target);
    }
}
