package br.edu.eventos.service;

import br.edu.eventos.model.CategoriaEvento;
import br.edu.eventos.model.Evento;
import br.edu.eventos.model.Usuario;
import br.edu.eventos.persistence.ArmazenamentoEventos;
import br.edu.eventos.persistence.ArmazenamentoUsuarios;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Regras de negócio: cadastro, participação, ordenação por proximidade temporal, notificações textuais.
 */
public class ServicoEventos {

    private final Path diretorioDados;
    private final ArmazenamentoEventos armazenamentoEventos;
    private final ArmazenamentoUsuarios armazenamentoUsuarios;
    private final List<Evento> eventos;
    private final List<Usuario> usuarios;

    public ServicoEventos(Path diretorioDados) {
        this.diretorioDados = diretorioDados;
        this.armazenamentoEventos = new ArmazenamentoEventos();
        this.armazenamentoUsuarios = new ArmazenamentoUsuarios();
        this.eventos = new ArrayList<>();
        this.usuarios = new ArrayList<>();
    }

    public void carregarDados() throws IOException {
        eventos.clear();
        eventos.addAll(armazenamentoEventos.carregar(diretorioDados));
        usuarios.clear();
        usuarios.addAll(armazenamentoUsuarios.carregar(diretorioDados));
    }

    public void persistir() throws IOException {
        armazenamentoEventos.salvar(diretorioDados, eventos);
        armazenamentoUsuarios.salvar(diretorioDados, usuarios);
    }

    public List<Usuario> listarUsuarios() {
        return List.copyOf(usuarios);
    }

    public Usuario cadastrarUsuario(String nome, String email, String cidade, String telefone, int idade) throws IOException {
        Usuario u = new Usuario(nome, email, cidade, telefone, idade);
        usuarios.add(u);
        persistir();
        return u;
    }

    public Optional<Usuario> buscarUsuarioPorId(String id) {
        return usuarios.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public Evento cadastrarEvento(String nome, String endereco, CategoriaEvento categoria,
                                  LocalDateTime horario, int duracaoMinutos, String descricao) throws IOException {
        Evento e = new Evento(nome, endereco, categoria, horario, duracaoMinutos, descricao);
        eventos.add(e);
        persistir();
        return e;
    }

    public List<Evento> eventosOrdenadosPorProximidade(LocalDateTime agora) {
        Map<Evento.StatusTemporal, List<Evento>> grupos = eventos.stream()
                .collect(Collectors.groupingBy(e -> e.statusEm(agora)));

        List<Evento> futuros = new ArrayList<>(grupos.getOrDefault(Evento.StatusTemporal.FUTURO, List.of()));
        futuros.sort(Comparator.comparing(Evento::getHorarioInicio));

        List<Evento> agoraList = new ArrayList<>(grupos.getOrDefault(Evento.StatusTemporal.OCORRENDO_AGORA, List.of()));
        agoraList.sort(Comparator.comparing(Evento::getHorarioInicio));

        List<Evento> passados = new ArrayList<>(grupos.getOrDefault(Evento.StatusTemporal.PASSADO, List.of()));
        passados.sort(Comparator.comparing(Evento::getHorarioInicio).reversed());

        List<Evento> resultado = new ArrayList<>();
        resultado.addAll(agoraList);
        resultado.addAll(futuros);
        resultado.addAll(passados);
        return resultado;
    }

    public List<Evento> eventosOcorrendoAgora(LocalDateTime agora) {
        return eventos.stream()
                .filter(e -> e.statusEm(agora) == Evento.StatusTemporal.OCORRENDO_AGORA)
                .sorted(Comparator.comparing(Evento::getHorarioInicio))
                .toList();
    }

    public List<Evento> eventosPassados(LocalDateTime agora) {
        return eventos.stream()
                .filter(e -> e.statusEm(agora) == Evento.StatusTemporal.PASSADO)
                .sorted(Comparator.comparing(Evento::getHorarioInicio).reversed())
                .toList();
    }

    public List<Evento> eventosFuturos(LocalDateTime agora) {
        return eventos.stream()
                .filter(e -> e.statusEm(agora) == Evento.StatusTemporal.FUTURO)
                .sorted(Comparator.comparing(Evento::getHorarioInicio))
                .toList();
    }

    public boolean confirmarParticipacao(String usuarioId, String eventoId) throws IOException {
        Evento e = buscarEvento(eventoId).orElse(null);
        if (e == null || buscarUsuarioPorId(usuarioId).isEmpty()) {
            return false;
        }
        boolean ok = e.adicionarParticipante(usuarioId);
        if (ok) {
            persistir();
        }
        return ok;
    }

    public boolean cancelarParticipacao(String usuarioId, String eventoId) throws IOException {
        Optional<Evento> opt = buscarEvento(eventoId);
        if (opt.isEmpty()) {
            return false;
        }
        Evento e = opt.get();
        boolean rem = e.removerParticipante(usuarioId);
        if (rem) {
            persistir();
        }
        return rem;
    }

    public List<Evento> eventosDoUsuario(String usuarioId) {
        return eventos.stream()
                .filter(e -> e.usuarioParticipa(usuarioId))
                .sorted(Comparator.comparing(Evento::getHorarioInicio))
                .toList();
    }

    public Optional<Evento> buscarEvento(String id) {
        return eventos.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public List<Evento> todosEventos() {
        return List.copyOf(eventos);
    }

    /**
     * Mensagens de “notificação” no console para eventos próximos (próximas 48h) ou em andamento.
     */
    public List<String> notificacoesParaUsuario(String usuarioId, LocalDateTime agora) {
        List<String> msgs = new ArrayList<>();
        for (Evento e : eventos) {
            if (!e.usuarioParticipa(usuarioId)) {
                continue;
            }
            Evento.StatusTemporal st = e.statusEm(agora);
            if (st == Evento.StatusTemporal.OCORRENDO_AGORA) {
                msgs.add("[AGORA] \"" + e.getNome() + "\" está em andamento até " + e.getHorarioFim() + ".");
            } else if (st == Evento.StatusTemporal.FUTURO) {
                long horas = java.time.Duration.between(agora, e.getHorarioInicio()).toHours();
                if (horas >= 0 && horas <= 48) {
                    msgs.add("[LEMBRETE] \"" + e.getNome() + "\" em " + e.getHorarioInicio() + " (" + horas + "h).");
                }
            }
        }
        return msgs;
    }
}
