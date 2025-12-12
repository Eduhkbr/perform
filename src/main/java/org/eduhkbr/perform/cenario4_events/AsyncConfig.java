package org.eduhkbr.perform.cenario4_events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;

@Configuration
@EnableAsync // Habilita a deteção da anotação @Async
public class AsyncConfig {

    // Opcional: Forçar o @Async a usar Virtual Threads também!
    // Assim, o envio de e-mail não bloqueia uma thread de plataforma.
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .build();
    }
}

