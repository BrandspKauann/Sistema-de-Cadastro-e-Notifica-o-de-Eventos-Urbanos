package br.edu.eventos.persistence;

import br.edu.eventos.model.CategoriaEvento;
import br.edu.eventos.model.Evento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Leitura e gravação de eventos em arquivo texto events.data (na pasta de trabalho atual).
 */
public class ArmazenamentoEventos {

    public static final String NOME_ARQUIVO = "events.data";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String SEP = "\u001E"; // separador raro para campos na mesma linha

    public List<Evento> carregar(Path baseDir) throws IOException {
        Path arquivo = baseDir.resolve(NOME_ARQUIVO);
        if (!Files.isRegularFile(arquivo)) {
            return new ArrayList<>();
        }
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<Evento> lista = new ArrayList<>();
        int i = 0;
        while (i < linhas.size()) {
            String linha = linhas.get(i).trim();
            if (linha.isEmpty() || linha.startsWith("#")) {
                i++;
                continue;
            }
            if (!linha.startsWith("EVENTO")) {
                i++;
                continue;
            }
            // EVENTO|id|nome|endereco|categoria|horarioISO|duracao|descricao(escaped)|part1,part2
            String payload = linha.substring("EVENTO".length());
            if (payload.startsWith("|")) {
                payload = payload.substring(1);
            }
            String[] partes = payload.split("\\" + SEP, -1);
            if (partes.length < 8) {
                i++;
                continue;
            }
            String id = partes[0];
            String nome = unescape(partes[1]);
            String endereco = unescape(partes[2]);
            CategoriaEvento cat = CategoriaEvento.valueOf(partes[3]);
            LocalDateTime inicio = LocalDateTime.parse(partes[4], FMT);
            int duracao = Integer.parseInt(partes[5]);
            String descricao = unescape(partes[6]);
            Set<String> parts = new LinkedHashSet<>();
            if (!partes[7].isBlank()) {
                parts.addAll(Arrays.stream(partes[7].split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            }
            lista.add(new Evento(id, nome, endereco, cat, inicio, duracao, descricao, parts));
            i++;
        }
        return lista;
    }

    public void salvar(Path baseDir, List<Evento> eventos) throws IOException {
        Path arquivo = baseDir.resolve(NOME_ARQUIVO);
        List<String> linhas = new ArrayList<>();
        linhas.add("# events.data — formato: EVENTO|campos separados por \\u001E");
        for (Evento e : eventos) {
            String participantes = e.getIdsParticipantes().stream().sorted().collect(Collectors.joining(","));
            String linha = "EVENTO|" + String.join(SEP,
                    e.getId(),
                    escape(e.getNome()),
                    escape(e.getEndereco()),
                    e.getCategoria().name(),
                    e.getHorarioInicio().format(FMT),
                    String.valueOf(e.getDuracaoMinutos()),
                    escape(e.getDescricao()),
                    participantes
            );
            linhas.add(linha);
        }
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\u001E", "\\u001E");
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
