package org.eduhkbr.perform.cenario3_async;

import org.eduhkbr.perform.common.ExternalServiceMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/cenario3")
public class AggregationController {

    private static final Logger log = LoggerFactory.getLogger(AggregationController.class);
    private final ExternalServiceMock externalService;

    public AggregationController(ExternalServiceMock externalService) {
        this.externalService = externalService;
    }

    // =========================================================================
    // O PROBLEMA: Busca Sequencial (Soma dos tempos)
    // =========================================================================
    @GetMapping("/{id}/sync")
    public DashboardDTO getDashboardSync(@PathVariable long id) {
        long start = System.currentTimeMillis();

        // 1. Busca usuário (200ms)
        var user = externalService.getUserInfo(id);

        // 2. Busca pedidos (400ms) - Só começa depois que o usuário termina
        var orders = externalService.getUserOrders(id);

        // 3. Busca recomendações (300ms) - Só começa depois que pedidos termina
        var recs = externalService.getUserRecommendations(id);

        long tempoTotal = System.currentTimeMillis() - start;
        log.info("Dashboard Sincrono finalizado em: {}ms", tempoTotal);

        // Total esperado: 200 + 400 + 300 = ~900ms
        return new DashboardDTO(user, orders, recs, tempoTotal);
    }

    // =========================================================================
    // A SOLUÇÃO: Busca Paralela (Tempo do mais lento)
    // =========================================================================
    @GetMapping("/{id}/async")
    public DashboardDTO getDashboardAsync(@PathVariable long id) {
        long start = System.currentTimeMillis();

        // Usamos Virtual Threads para gerenciar a espera
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Dispara as 3 chamadas AO MESMO TEMPO
            var userFuture = CompletableFuture
                .supplyAsync(() -> externalService.getUserInfo(id), executor);

            var ordersFuture = CompletableFuture
                .supplyAsync(() -> externalService.getUserOrders(id), executor);

            var recsFuture = CompletableFuture
                .supplyAsync(() -> externalService.getUserRecommendations(id), executor);

            // O .join() aqui espera TODOS terminarem.
            // O tempo total será ditado pela tarefa mais lenta (orders: 400ms)
            CompletableFuture.allOf(userFuture, ordersFuture, recsFuture).join();

            long tempoTotal = System.currentTimeMillis() - start;
            log.info("Dashboard Assíncrono finalizado em: {}ms", tempoTotal);

            // .get() aqui é seguro pois o join() acima garantiu que tudo terminou
            return new DashboardDTO(
                userFuture.get(),
                ordersFuture.get(),
                recsFuture.get(),
                tempoTotal
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao montar dashboard", e);
        }
    }
}

