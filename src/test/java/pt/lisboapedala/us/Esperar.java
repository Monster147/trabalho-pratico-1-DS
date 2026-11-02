package pt.lisboapedala.us;

import java.util.function.Supplier;

/** Espera por resultados de passos assíncronos (faturação e pagamentos correm em @Async). */
final class Esperar {

    private Esperar() {
    }

    static <T> T ate(Supplier<T> valor, java.util.function.Predicate<T> condicao) throws InterruptedException {
        long limite = System.currentTimeMillis() + 10_000;
        T atual = valor.get();
        while (!condicao.test(atual) && System.currentTimeMillis() < limite) {
            Thread.sleep(50);
            atual = valor.get();
        }
        return atual;
    }
}
