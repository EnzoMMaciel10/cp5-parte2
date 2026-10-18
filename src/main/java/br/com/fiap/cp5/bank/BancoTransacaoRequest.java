package br.com.fiap.cp5.bank;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BancoTransacaoRequest(
        @NotBlank String referencia,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        boolean simularRecusa) {
}
