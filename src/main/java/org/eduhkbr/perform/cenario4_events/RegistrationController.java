package org.eduhkbr.perform.cenario4_events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cenario4")
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);
    private final ApplicationEventPublisher publisher;
    private final WelcomeEmailListener listener; // Injeção direta apenas para demonstrar o jeito errado

    public RegistrationController(ApplicationEventPublisher publisher, WelcomeEmailListener listener) {
        this.publisher = publisher;
        this.listener = listener;
    }

    // =========================================================================
    // O PROBLEMA: Acoplado e Lento
    // =========================================================================
    @PostMapping("/register/sync")
    public String registerSync(@RequestParam String name) {
        long start = System.currentTimeMillis();
        log.info("Recebida requisição síncrona para: {}", name);

        // Chamada direta ao método (mesmo que tenha @Async, se chamar direto da classe, ignora o proxy em alguns casos,
        // mas aqui vamos assumir a lógica de "fazer na hora").
        // Para simular o erro comum, vamos "fingir" que o listener não é async ou chamar a lógica aqui:
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        long tempo = System.currentTimeMillis() - start;
        return String.format("Usuário %s registado. (Demorou: %d ms - UX Ruim)", name, tempo);
    }

    // =========================================================================
    // A SOLUÇÃO: Event-Driven e Rápido
    // =========================================================================
    @PostMapping("/register/async")
    public String registerAsync(@RequestParam String name) {
        long start = System.currentTimeMillis();
        log.info("Recebida requisição assíncrona para: {}", name);

        // 1. Salva no banco (rápido, ~10ms)
        // repo.save(...)

        // 2. Publica o evento e ESQUECE
        publisher.publishEvent(new UserRegisteredEvent(name, name + "@test.com"));

        long tempo = System.currentTimeMillis() - start;

        // O retorno acontece em milissegundos, o e-mail vai depois
        return String.format("Usuário %s registado! Verifique os logs. (Tempo de resposta: %d ms - UX Ótima)", name, tempo);
    }
}

