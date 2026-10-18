package br.com.fiap.cp5.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitConfig {
    public static final String REQUISICOES_EXCHANGE = "mercado.pagamentos.requisicoes";
    public static final String RESPOSTAS_EXCHANGE = "mercado.pagamentos.respostas";
    public static final String ERROS_EXCHANGE = "mercado.pagamentos.erros";
    public static final String REQUISICOES_QUEUE = "mercado.pagamentos.requisicoes.q";
    public static final String RESPOSTAS_QUEUE = "mercado.pagamentos.respostas.q";
    public static final String ERROS_QUEUE = "mercado.pagamentos.erros.q";
    public static final String REQUISICAO_KEY = "pagamento.solicitado";
    public static final String RESPOSTA_KEY = "pagamento.respondido";

    @Bean
    DirectExchange requisicoesExchange() {
        return new DirectExchange(REQUISICOES_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange respostasExchange() {
        return new DirectExchange(RESPOSTAS_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange errosExchange() {
        return new DirectExchange(ERROS_EXCHANGE, true, false);
    }

    @Bean
    Queue requisicoesQueue() {
        return QueueBuilder.durable(REQUISICOES_QUEUE)
                .deadLetterExchange(ERROS_EXCHANGE)
                .deadLetterRoutingKey("erro")
                .build();
    }

    @Bean
    Queue respostasQueue() {
        return QueueBuilder.durable(RESPOSTAS_QUEUE)
                .deadLetterExchange(ERROS_EXCHANGE)
                .deadLetterRoutingKey("erro")
                .build();
    }

    @Bean
    Queue errosQueue() {
        return QueueBuilder.durable(ERROS_QUEUE).build();
    }

    @Bean
    Binding requisicoesBinding(@Qualifier("requisicoesQueue") Queue requisicoesQueue,
                              @Qualifier("requisicoesExchange") DirectExchange requisicoesExchange) {
        return BindingBuilder.bind(requisicoesQueue).to(requisicoesExchange).with(REQUISICAO_KEY);
    }

    @Bean
    Binding respostasBinding(@Qualifier("respostasQueue") Queue respostasQueue,
                            @Qualifier("respostasExchange") DirectExchange respostasExchange) {
        return BindingBuilder.bind(respostasQueue).to(respostasExchange).with(RESPOSTA_KEY);
    }

    @Bean
    Binding errosBinding(@Qualifier("errosQueue") Queue errosQueue,
                        @Qualifier("errosExchange") DirectExchange errosExchange) {
        return BindingBuilder.bind(errosQueue).to(errosExchange).with("erro");
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
