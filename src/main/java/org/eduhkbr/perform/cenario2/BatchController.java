package org.eduhkbr.perform.cenario2;

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

        List<Venda> vendas = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            vendas.add(new Venda("Produto Batch " + i, 10.0 + i));
        }

        // O Hibernate vai agrupar isso em blocos de 50 (conforme config)
        // SQL Gerado: insert into venda (...) values (...), (...), (...)
        repository.saveAll(vendas);

        long time = System.currentTimeMillis() - start;
        return "Fast insert (5000 registros com Batch) levou: " + time + "ms";
    }
}

