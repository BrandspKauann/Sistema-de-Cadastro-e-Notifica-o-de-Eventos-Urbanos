package br.edu.eventos.persistence;

import br.edu.eventos.model.Usuario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistência opcional de usuários (users.data) para não perder cadastros entre execuções.
 */
public class ArmazenamentoUsuarios {

    public static final String NOME_ARQUIVO = "users.data";
    private static final String SEP = "\u001E";

    public List<Usuario> carregar(Path baseDir) throws IOException {
        Path arquivo = baseDir.resolve(NOME_ARQUIVO);
        if (!Files.isRegularFile(arquivo)) {
            return new ArrayList<>();
        }
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<Usuario> lista = new ArrayList<>();
        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty() || linha.startsWith("#")) {
                continue;
            }
            if (!linha.startsWith("USUARIO|")) {
                continue;
            }
            String payload = linha.substring("USUARIO|".length());
            String[] p = payload.split("\\" + SEP, -1);
            if (p.length < 6) {
                continue;
            }
            lista.add(new Usuario(p[0], unescape(p[1]), unescape(p[2]), unescape(p[3]), unescape(p[4]), Integer.parseInt(p[5])));
        }
        return lista;
    }

    public void salvar(Path baseDir, List<Usuario> usuarios) throws IOException {
        Path arquivo = baseDir.resolve(NOME_ARQUIVO);
        List<String> linhas = new ArrayList<>();
        linhas.add("# users.data");
        for (Usuario u : usuarios) {
            linhas.add("USUARIO|" + String.join(SEP,
                    u.getId(),
                    escape(u.getNomeCompleto()),
                    escape(u.getEmail()),
                    escape(u.getCidadeResidencia()),
                    escape(u.getTelefone()),
                    String.valueOf(u.getIdade())
            ));
        }
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String unescape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == 'n') {
                    out.append('\n');
                    i++;
                } else if (n == 'r') {
                    out.append('\r');
                    i++;
                } else if (n == '\\') {
                    out.append('\\');
                    i++;
                } else {
                    out.append(c);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
