package pt.lisboapedala.estacao.ocupacao.internal;

import java.util.List;

/** Estação e as docas que lhe pertencem (a capacidade é o número de docas). */
public record Estacao(String id, String nome, String freguesia, double latitude, double longitude,
                      List<String> docas) {

    public Estacao {
        docas = List.copyOf(docas);
    }
}
