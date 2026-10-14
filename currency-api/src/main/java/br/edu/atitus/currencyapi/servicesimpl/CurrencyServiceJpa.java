package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceJpa.class);

    // Data fixa com cotacao disponivel no BCB (formato MM-dd-yyyy)
    private static final String DATA_COTACAO = "10-13-2026";

    private final CurrencyRepository repository;
    private final BcbClientService bcbClientService;

    public CurrencyServiceJpa(CurrencyRepository repository,
                              BcbClientService bcbClientService) {
        this.repository = repository;
        this.bcbClientService = bcbClientService;
    }

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency,
            String targetCurrency
    ) throws Exception {
        String src = sourceCurrency.toUpperCase();
        String tgt = targetCurrency.toUpperCase();
        String environment = "Currency API running in Port: " + serverPort;

        // 1) Tenta o BCB. Falha de comunicacao ou circuito aberto cai no fallback.
        Double rate = null;
        try {
            rate = getRateFromBcb(src, tgt);
        } catch (Exception e) {
            log.warn("Falha ao consultar o BCB ({} -> {}): {}", src, tgt, e.toString());
        }
        if (rate != null) {
            return new CurrencyResponse(src, tgt, rate, environment + " - BCB");
        }

        // 2) Fallback: banco local. Sem cotacao aqui tambem, vira 404.
        log.info("Usando banco local como fallback ({} -> {})", src, tgt);
        var currency = repository
                .findBySourceCurrencyAndTargetCurrency(src, tgt)
                .orElseThrow(() -> new EntityNotFoundException("Cotação não encontrada"));

        return CurrencyResponse.fromEntity(currency, environment + " - Local database");
    }

    private Double getRateFromBcb(String source, String target) {
        if (source.equals(target)) {
            return 1.0;
        }
        Double sourceToBrl = toBrl(source);
        Double targetToBrl = toBrl(target);
        if (sourceToBrl == null || targetToBrl == null) {
            return null; // sem cotacao no BCB
        }
        return sourceToBrl / targetToBrl;
    }

    private Double toBrl(String moeda) {
        if ("BRL".equals(moeda)) {
            return 1.0;
        }
        return bcbClientService.getCotacaoVenda(moeda, DATA_COTACAO);
    }
}