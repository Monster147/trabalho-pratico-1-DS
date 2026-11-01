package pt.lisboapedala.estacao.disponibilidade.internal;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Ativa as tarefas periódicas deste componente sem ser preciso alterar a classe de arranque. */
@Configuration
@EnableScheduling
class AgendamentoDisponibilidade {
}
