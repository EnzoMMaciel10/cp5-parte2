package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.EmailEnvio;
import br.com.fiap.cp5.model.StatusEmail;
import br.com.fiap.cp5.repository.EmailEnvioRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;

@Service
public class EmailService {
    private final EmailEnvioRepository repository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;

    public EmailService(EmailEnvioRepository repository,
                        ObjectProvider<JavaMailSender> mailSender,
                        @Value("${banco-tranquilo.mail-from}") String remetente) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarConfirmacao(Long pedidoId) {
        EmailEnvio registro = repository.findByPedido_Id(pedidoId).orElseThrow();
        if (registro.getStatus() == StatusEmail.ENVIADO) {
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            registro.setStatus(StatusEmail.PENDENTE);
            registro.setErro("SMTP não configurado; envio pendente.");
            return;
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(registro.getDestinatario());
        mensagem.setSubject("Mercado Express — pedido #" + pedidoId + " confirmado");
        mensagem.setText("Seu pagamento foi aprovado e o pedido #" + pedidoId
                + " foi confirmado. Obrigado pela compra!");
        try {
            sender.send(mensagem);
            registro.setStatus(StatusEmail.ENVIADO);
            registro.setEnviadoEm(Instant.now());
            registro.setErro(null);
        } catch (RuntimeException exception) {
            registro.setStatus(StatusEmail.FALHA);
            registro.setErro(exception.getClass().getSimpleName() + ": "
                    + String.valueOf(exception.getMessage()).substring(0,
                    Math.min(420, String.valueOf(exception.getMessage()).length())));
        }
    }

}
