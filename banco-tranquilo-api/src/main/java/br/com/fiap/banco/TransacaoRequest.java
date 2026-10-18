package br.com.fiap.banco;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransacaoRequest(
        @NotBlank String referencia,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        boolean simularRecusa) {
}
