package org.eduhkbr.perform.cenario2_batching;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cenario2")
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);
    private final VendaRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public BatchController(VendaRepository repository) {
        this.repository = repository;
    }

    // =========================================================================
    // O PROBLEMA: Insert Um por Um (Anti-pattern)
    // =========================================================================
    @PostMapping("/slow")
    public String insertSlow() {
        long start = System.currentTimeMillis();

        // Simula 5000 vendas chegando
        for (int i = 0; i < 5000; i++) {
            Venda v = new Venda("Produto " + i, 10.0 + i);
            // Cada save vai disparar uma ida ao banco (se não houver transação otimizada)
            // Mesmo com @Transactional, o Hibernate pode flushar um por um dependendo da config.
            repository.save(v);
        }

        long time = System.currentTimeMillis() - start;
        return "Slow insert (5000 registros) levou: " + time + "ms";
    }

    // =========================================================================
    // A SOLUÇÃO: Batch Insert (JPA Otimizado)
    // =========================================================================
    @PostMapping("/fast")
    @Transactional // Garante que tudo ocorra numa única sessão do Hibernate
    public String insertFast() {
        long start = System.currentTimeMillis();
        int total = 5000;
        int batchSize = 1000; // Tamanho do lote

        List<Venda> batch = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            batch.add(new Venda("Produto Batch " + i, 10.0 + i));

            if (batch.size() >= batchSize) {
                // 1. Salva o lote no contexto de persistência
                repository.saveAll(batch);

                // 2. Força o envio para o banco de dados (SQL INSERTs)
                entityManager.flush();

                // 3. Limpa o contexto de persistência (First Level Cache)
                // Isso libera memória, evitando OutOfMemoryError em grandes cargas de dados
                entityManager.clear();

                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            repository.saveAll(batch);
            entityManager.flush();
            entityManager.clear(); // Garante que o último lote também libere memória
        }

        long time = System.currentTimeMillis() - start;
        return "Fast insert (" + total + " registros com Batch e Clear) levou: " + time + "ms";
    }
}
