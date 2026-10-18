package br.com.fiap.cp5.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PedidoForm {

    @NotNull(message = "Informe o produto.")
    @Positive(message = "Produto inválido.")
    private Long produtoId;

    @NotNull(message = "Informe a quantidade.")
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1.")
    private Integer quantidade = 1;

    private boolean simularRecusa;
}
