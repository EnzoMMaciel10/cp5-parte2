package br.com.fiap.cp5.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tds_sec_mvc_tb_mercado")
@Getter
@Setter
@NoArgsConstructor
public class Mercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório.")
    @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O tipo do produto é obrigatório.")
    @Size(max = 60, message = "O tipo deve ter até 60 caracteres.")
    @Column(nullable = false, length = 60)
    private String tipo;

    @NotBlank(message = "O setor é obrigatório.")
    @Size(max = 60, message = "O setor deve ter até 60 caracteres.")
    @Column(nullable = false, length = 60)
    private String setor;

    @NotBlank(message = "O tamanho é obrigatório.")
    @Size(max = 40, message = "O tamanho deve ter até 40 caracteres.")
    @Column(nullable = false, length = 40)
    private String tamanho;

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Informe um preço com até 10 dígitos inteiros e 2 decimais."
    )
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @NotNull(message = "O custo é obrigatório.")
    @DecimalMin(value = "0.00", message = "O custo não pode ser negativo.")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Informe um custo com até 10 dígitos inteiros e 2 decimais."
    )
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal custo;

    @NotNull(message = "O estoque é obrigatório.")
    @PositiveOrZero(message = "O estoque não pode ser negativo.")
    @Column(nullable = false)
    private Integer estoque;
}