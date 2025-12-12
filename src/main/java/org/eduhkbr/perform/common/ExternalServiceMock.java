package org.eduhkbr.perform.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ExternalServiceMock {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceMock.class);

    /**
     * Simula uma chamada de rede bloqueante.
     * Imagine que isso e uma chamada HTTP para uma API de Pagamentos ou Clima.
     */
    public String fetchData(int id) {
        try {
            // Simula latencia de rede (200ms)
            // Durante esse tempo, uma Thread de Plataforma (OS) ficaria TRAVADA sem fazer nada.
            Thread.sleep(Duration.ofMillis(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Dados processados para ID: " + id;
    }

    // =========================================================================
    // Métodos para Cenário 3: Agregação Paralela
    // =========================================================================

    public String getUserInfo(long id) {
        simularLentidao(200); // 200ms
        return "Info do Usuário " + id;
    }

    public String getUserOrders(long id) {
        simularLentidao(400); // 400ms (O mais lento)
        return "Pedidos Recentes do Usuário " + id;
    }

    public String getUserRecommendations(long id) {
        simularLentidao(300); // 300ms
        return "Produtos Recomendados para " + id;
    }

    private void simularLentidao(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
