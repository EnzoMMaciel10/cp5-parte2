package br.com.fiap.cp5.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tds_emails_pedido_mercado")
@Getter
@Setter
@NoArgsConstructor
public class EmailEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmail status = StatusEmail.PENDENTE;

    @Column(nullable = false)
    private Instant criadoEm = Instant.now();

    private Instant enviadoEm;

    @Column(length = 500)
    private String erro;
}
