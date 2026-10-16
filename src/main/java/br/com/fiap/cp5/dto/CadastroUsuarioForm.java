package br.com.fiap.cp5.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastroUsuarioForm {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter até 150 caracteres.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(
            min = 8,
            max = 72,
            message = "A senha deve ter entre 8 e 72 caracteres."
    )
    private String senha;
}