package org.eduhkbr.perform.cenario1_virtualthreads;

import org.eduhkbr.perform.common.ExternalServiceMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/cenario1")
public class IoHeavyController {

    private static final Logger log = LoggerFactory.getLogger(IoHeavyController.class);
    private final ExternalServiceMock externalService;

    public IoHeavyController(ExternalServiceMock externalService) {
        this.externalService = externalService;
    }

    // =========================================================================
    // O PROBLEMA: Processamento Sequencial (Bloqueante)
    // =========================================================================
    @GetMapping("/blocking")
    public List<String> processBlocking() {
        long start = System.currentTimeMillis();
        log.info("Iniciando processamento bloqueante...");

        // Queremos processar 50 requisicoes
        // Se cada uma leva 200ms -> 50 * 200ms = ~10 segundos de espera!
        List<String> results = IntStream.range(0, 50)
                .mapToObj(i -> {
                    // A thread principal fica presa aqui esperando cada um terminar
                    return externalService.fetchData(i);
                })
                .toList();

        long time = System.currentTimeMillis() - start;
        log.info("Processamento Bloqueante finalizado em: {}ms", time);
        return results;
    }

    // =========================================================================
    // A SOLUCAO: Virtual Threads (Java 21)
    // =========================================================================
    @GetMapping("/virtual")
    public List<String> processVirtual() throws Exception {
        long start = System.currentTimeMillis();
        log.info("Iniciando processamento com Virtual Threads...");

        // Cria um executor que lanca uma NOVA Virtual Thread para CADA tarefa.
        // Virtual threads sao muito leves, podemos criar milhoes delas.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Submetemos 50 tarefas simultaneamente
            List<Future<String>> futures = IntStream.range(0, 50)
                    .mapToObj(i -> executor.submit(() -> {
                        // Quando entra no sleep do mock, a Virtual Thread "desmonta"
                        // e libera a Thread do SO para fazer outra coisa.
                        return externalService.fetchData(i);
                    }))
                    .toList();

            // Coletamos os resultados (Future.get() aguarda a conclusao)
            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) {
                results.add(future.get());
            }

            long time = System.currentTimeMillis() - start;
            // O tempo total deve ser muito proximo de APENAS 200ms (o tempo da tarefa mais lenta),
            // pois todas rodaram em paralelo.
            log.info("Processamento Virtual finalizado em: {}ms", time);
            return results;
        }
    }
}
