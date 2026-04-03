package br.edu.eventos.model;

/**
 * Categorias pré-definidas para cadastro de eventos na cidade.
 */
public enum CategoriaEvento {
    FESTA("Festas e comemorações"),
    EVENTO_ESPORTIVO("Eventos esportivos"),
    SHOW("Shows e apresentações musicais"),
    CULTURAL("Cultura, teatro e cinema"),
    FEIRA("Feiras e exposições"),
    ACADEMICO("Palestras e eventos acadêmicos"),
    OUTRO("Outros");

    private final String rotulo;

    CategoriaEvento(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static void listarOpcoes() {
        int i = 1;
        for (CategoriaEvento c : values()) {
            System.out.printf("%d) %s — %s%n", i++, c.name(), c.rotulo);
        }
    }

    public static CategoriaEvento porIndice(int indice) {
        CategoriaEvento[] vals = values();
        if (indice < 1 || indice > vals.length) {
            return null;
        }
        return vals[indice - 1];
    }
}
