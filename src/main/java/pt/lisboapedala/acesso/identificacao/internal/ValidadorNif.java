package pt.lisboapedala.acesso.identificacao.internal;

/** NIF português: 9 dígitos + dígito de controlo (módulo 11). */
final class ValidadorNif {

    private ValidadorNif() {
    }

    static boolean valido(String nif) {
        if (nif == null || !nif.matches("\\d{9}")) {
            return false;
        }
        int soma = 0;
        for (int i = 0; i < 8; i++) {
            soma += (nif.charAt(i) - '0') * (9 - i);
        }
        int controlo = 11 - (soma % 11);
        return (controlo >= 10 ? 0 : controlo) == nif.charAt(8) - '0';
    }
}
