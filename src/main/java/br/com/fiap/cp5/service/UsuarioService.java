package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.Perfil;
import br.com.fiap.cp5.model.Usuario;
import br.com.fiap.cp5.repository.UsuarioRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Validated
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(
            @NotBlank(message = "O nome é obrigatório.")
            @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
            String nome,

            @NotBlank(message = "O e-mail é obrigatório.")
            @Email(message = "Informe um e-mail válido.")
            @Size(max = 150, message = "O e-mail deve ter até 150 caracteres.")
            String email,

            @NotBlank(message = "A senha é obrigatória.")
            @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
            String senha
    ) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado."
            );
        }

        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "A senha é muito longa. Use uma senha mais curta."
            );
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setPerfil(Perfil.CLIENTE);

        return usuarioRepository.save(usuario);
    }
}
