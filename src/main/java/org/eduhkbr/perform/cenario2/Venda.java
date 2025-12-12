package org.eduhkbr.perform.cenario2;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venda_seq")
    @SequenceGenerator(name = "venda_seq", sequenceName = "venda_sequence", allocationSize = 50)
    private Long id;

    private String produto;
    private Double valor;

    public Venda(String produto, Double valor) {
        this.produto = produto;
        this.valor = valor;
    }
}

