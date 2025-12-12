package org.eduhkbr.perform.cenario4_events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WelcomeEmailListener {

    private static final Logger log = LoggerFactory.getLogger(WelcomeEmailListener.class);

    @EventListener
    @Async // <--- Executa em outra thread
    public void handleUserRegistration(UserRegisteredEvent event) {
        log.info("--> [Background] Iniciando envio de e-mail para {}", event.email());

        try {
            // Simula delay de envio SMTP (3 segundos)
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("--> [Background] E-mail enviado com sucesso para {}!", event.username());
    }
}

