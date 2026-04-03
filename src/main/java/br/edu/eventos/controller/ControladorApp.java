package br.edu.eventos.controller;

import br.edu.eventos.model.CategoriaEvento;
import br.edu.eventos.model.Evento;
import br.edu.eventos.model.Usuario;
import br.edu.eventos.service.ServicoEventos;
import br.edu.eventos.view.VisaoConsole;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquestra menus e delega ao modelo/serviços (MVC — Controller).
 */
public class ControladorApp {

    private final VisaoConsole visao;
    private final ServicoEventos servico;
    private Usuario usuarioAtivo;

    public ControladorApp(VisaoConsole visao, ServicoEventos servico) {
        this.visao = visao;
        this.servico = servico;
    }

    public void executar() {
        try {
            servico.carregarDados();
        } catch (IOException e) {
            visao.mensagem("Erro ao carregar dados: " + e.getMessage());
            return;
        }

        visao.titulo("Bem-vindo");
        visao.mensagem("Eventos são carregados de " + br.edu.eventos.persistence.ArmazenamentoEventos.NOME_ARQUIVO);
        visao.mensagem("Usuários em " + br.edu.eventos.persistence.ArmazenamentoUsuarios.NOME_ARQUIVO);

        boolean sair = false;
        while (!sair) {
            LocalDateTime agora = LocalDateTime.now();
            visao.mostrarUsuarioAtual(usuarioAtivo);
            int op = visao.menuPrincipal();
            try {
                switch (op) {
                    case 0 -> sair = true;
                    case 1 -> cadastrarUsuario();
                    case 2 -> selecionarUsuario();
                    case 3 -> cadastrarEvento();
                    case 4 -> consultarEventos(agora);
                    case 5 -> participarEvento();
                    case 6 -> meusEventos();
                    case 7 -> apenasAgora(agora);
                    case 8 -> apenasPassados(agora);
                    case 9 -> notificacoes(agora);
                    default -> visao.mensagem("Opção inválida.");
                }
            } catch (IOException e) {
                visao.mensagem("Erro ao salvar: " + e.getMessage());
            }
        }
        visao.mensagem("Até logo.");
    }

    private void cadastrarUsuario() throws IOException {
        visao.titulo("Novo usuário");
        String nome = visao.lerLinha("Nome completo: ");
        String email = visao.lerLinha("E-mail: ");
        String cidade = visao.lerLinha("Cidade em que reside: ");
        String tel = visao.lerLinha("Telefone: ");
        int idade = visao.lerInteiro("Idade: ");
        if (nome.isBlank() || email.isBlank() || cidade.isBlank()) {
            visao.mensagem("Nome, e-mail e cidade são obrigatórios.");
            return;
        }
        Usuario u = servico.cadastrarUsuario(nome, email, cidade, tel, idade);
        usuarioAtivo = u;
        visao.mensagem("Usuário cadastrado e selecionado. Id: " + u.getId());
    }

    private void selecionarUsuario() {
        visao.titulo("Selecionar usuário");
        List<Usuario> lista = servico.listarUsuarios();
        visao.listarUsuarios(lista);
        if (lista.isEmpty()) {
            return;
        }
        String id = visao.lerLinha("Cole o ID do usuário (ou Enter para cancelar): ");
        if (id.isBlank()) {
            return;
        }
        servico.buscarUsuarioPorId(id).ifPresentOrElse(
                u -> {
                    usuarioAtivo = u;
                    visao.mensagem("Usuário ativo: " + u.getNomeCompleto());
                },
                () -> visao.mensagem("ID não encontrado.")
        );
    }

    private void cadastrarEvento() throws IOException {
        visao.titulo("Novo evento");
        String nome = visao.lerLinha("Nome do evento: ");
        String endereco = visao.lerLinha("Endereço: ");
        CategoriaEvento cat = visao.lerCategoria();
        LocalDateTime inicio = visao.lerDataHora("Horário de início");
        int duracao = visao.lerInteiro("Duração estimada (minutos, ex: 120): ");
        visao.mensagem("Descrição (uma linha): ");
        String desc = visao.lerLinha("");
        if (nome.isBlank() || endereco.isBlank() || desc.isBlank()) {
            visao.mensagem("Nome, endereço e descrição são obrigatórios.");
            return;
        }
        Evento e = servico.cadastrarEvento(nome, endereco, cat, inicio, duracao, desc);
        visao.mensagem("Evento cadastrado. Id: " + e.getId());
    }

    private void consultarEventos(LocalDateTime agora) {
        visao.titulo("Eventos cadastrados (ordenados)");
        List<Evento> lista = servico.eventosOrdenadosPorProximidade(agora);
        visao.listarEventosComStatus(lista, agora);
    }

    private void participarEvento() throws IOException {
        if (usuarioAtivo == null) {
            visao.mensagem("Selecione um usuário antes (menu 2).");
            return;
        }
        visao.titulo("Participar de evento");
        LocalDateTime agora = LocalDateTime.now();
        List<Evento> futuros = servico.eventosFuturos(agora);
        List<Evento> agoraList = servico.eventosOcorrendoAgora(agora);
        List<Evento> elegiveis = new java.util.ArrayList<>(agoraList);
        elegiveis.addAll(futuros);
        if (elegiveis.isEmpty()) {
            visao.mensagem("Não há eventos futuros ou em andamento para confirmar presença.");
            return;
        }
        visao.listarEventosComStatus(elegiveis, agora);
        String idEv = visao.lerLinha("ID do evento: ");
        if (idEv.isBlank()) {
            return;
        }
        if (servico.buscarEvento(idEv).isEmpty()) {
            visao.mensagem("Evento não encontrado.");
            return;
        }
        boolean ok = servico.confirmarParticipacao(usuarioAtivo.getId(), idEv);
        visao.mensagem(ok ? "Participação confirmada." : "Você já confirmou neste evento ou dados inválidos.");
    }

    private void meusEventos() throws IOException {
        if (usuarioAtivo == null) {
            visao.mensagem("Selecione um usuário antes (menu 2).");
            return;
        }
        visao.titulo("Meus eventos");
        LocalDateTime agora = LocalDateTime.now();
        List<Evento> meus = servico.eventosDoUsuario(usuarioAtivo.getId());
        if (meus.isEmpty()) {
            visao.mensagem("Você não confirmou presença em nenhum evento.");
            return;
        }
        visao.listarEventosComStatus(meus, agora);
        String op = visao.lerLinha("Cancelar participação? Digite o ID do evento ou Enter para voltar: ");
        if (op.isBlank()) {
            return;
        }
        boolean rem = servico.cancelarParticipacao(usuarioAtivo.getId(), op);
        visao.mensagem(rem ? "Participação cancelada." : "Não foi possível cancelar (id inválido ou não participava).");
    }

    private void apenasAgora(LocalDateTime agora) {
        visao.titulo("Eventos ocorrendo neste momento");
        List<Evento> lista = servico.eventosOcorrendoAgora(agora);
        visao.listarEventosComStatus(lista, agora);
        if (lista.isEmpty()) {
            visao.mensagem("Nenhum evento neste intervalo de horário.");
        }
    }

    private void apenasPassados(LocalDateTime agora) {
        visao.titulo("Eventos que já ocorreram");
        List<Evento> lista = servico.eventosPassados(agora);
        visao.listarEventosComStatus(lista, agora);
        if (lista.isEmpty()) {
            visao.mensagem("Nenhum evento passado registrado.");
        }
    }

    private void notificacoes(LocalDateTime agora) {
        if (usuarioAtivo == null) {
            visao.mensagem("Selecione um usuário (menu 2) para ver notificações.");
            return;
        }
        visao.mostrarNotificacoes(servico.notificacoesParaUsuario(usuarioAtivo.getId(), agora));
    }

    public static void mainBootstrap(String[] args) {
        Path dir = Path.of(System.getProperty("user.dir"));
        if (args.length > 0) {
            dir = Path.of(args[0]);
        }
        VisaoConsole v = new VisaoConsole();
        ServicoEventos s = new ServicoEventos(dir);
        new ControladorApp(v, s).executar();
    }
}
