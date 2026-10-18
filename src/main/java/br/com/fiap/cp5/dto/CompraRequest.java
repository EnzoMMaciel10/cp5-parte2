package br.com.fiap.cp5.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompraRequest(
        @NotNull @Positive Long produtoId,
        @NotNull @Min(1) Integer quantidade,
        boolean simularRecusa) {
}
