package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.EmailEnvio;
import br.com.fiap.cp5.model.StatusEmail;
import br.com.fiap.cp5.repository.EmailEnvioRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailRetryScheduler {
    private final EmailEnvioRepository repository;
    private final EmailService emailService;
    private final ObjectProvider<JavaMailSender> mailSender;

    public EmailRetryScheduler(EmailEnvioRepository repository, EmailService emailService,
                               ObjectProvider<JavaMailSender> mailSender) {
        this.repository = repository;
        this.emailService = emailService;
        this.mailSender = mailSender;
    }

    @Scheduled(fixedDelay = 60000)
    public void tentarPendentes() {
        if (mailSender.getIfAvailable() == null) {
            return;
        }
        for (EmailEnvio registro : repository.findTop20ByStatusOrderByCriadoEmAsc(StatusEmail.PENDENTE)) {
            emailService.enviarConfirmacao(registro.getPedido().getId());
        }
    }
}
