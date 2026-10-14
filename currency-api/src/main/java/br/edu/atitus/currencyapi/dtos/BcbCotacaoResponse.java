package br.edu.atitus.currencyapi.dtos;

import java.util.List;

public record BcbCotacaoResponse(
        List<BcbCotacao> value
) {}