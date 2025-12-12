package org.eduhkbr.perform.cenario3_async;

public record DashboardDTO(
    String userInfo,
    String orders,
    String recommendations,
    long tempoTotalMs
) {}

