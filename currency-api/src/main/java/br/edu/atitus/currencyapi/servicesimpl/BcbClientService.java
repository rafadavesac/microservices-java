package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BcbClientService {

    private final BcbClient bcbClient;

    public BcbClientService(BcbClient bcbClient) {
        this.bcbClient = bcbClient;
    }

    @Cacheable(
            value = "bcb-api",
            key = "#moeda + '-' + #dataCotacao",
            unless = "#result == null"
    )
    @Retry(name = "bcb-api")
    @CircuitBreaker(name = "bcb-api")
    public Double getCotacaoVenda(String moeda, String dataCotacao) {
        var response = bcbClient.getCotacao(
                "'" + moeda + "'",
                "'" + dataCotacao + "'",
                "json"
        );

        if (response == null || response.value() == null || response.value().isEmpty()) {
            return null;
        }
        return response.value().get(0).cotacaoVenda();
    }
}