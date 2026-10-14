package br.edu.atitus.currencyapi.dtos;

public record BcbCotacao(
        Double cotacaoCompra,
        Double cotacaoVenda,
        String dataHoraCotacao
) {}