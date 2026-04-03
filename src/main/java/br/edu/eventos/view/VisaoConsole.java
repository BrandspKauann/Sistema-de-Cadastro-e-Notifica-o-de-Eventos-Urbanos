package br.edu.eventos.view;

import br.edu.eventos.model.CategoriaEvento;
import br.edu.eventos.model.Evento;
import br.edu.eventos.model.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Entrada e saída no console (camada View do MVC).
 */
public class VisaoConsole {

    private static final DateTimeFormatter FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Scanner scanner = new Scanner(System.in);

    public void linha() {
        System.out.println("--------------------------------------------------------------------------------");
    }

    public void titulo(String s) {
        linha();
        System.out.println(s);
        linha();
    }

    public void mensagem(String s) {
        System.out.println(s);
    }

    public String lerLinha(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int lerInteiro(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(lerLinha(prompt));
            } catch (NumberFormatException e) {
                mensagem("Digite um número inteiro válido.");
            }
        }
    }

    public LocalDateTime lerDataHora(String prompt) {
        while (true) {
            String s = lerLinha(prompt + " (formato dd/MM/yyyy HH:mm, ex: 15/04/2026 19:30): ");
            try {
                return LocalDateTime.parse(s, FMT_BR);
            } catch (DateTimeParseException e) {
                mensagem("Data/hora inválida. Use dd/MM/yyyy HH:mm");
            }
        }
    }

    public CategoriaEvento lerCategoria() {
        mensagem("Categorias disponíveis:");
        CategoriaEvento.listarOpcoes();
        while (true) {
            int n = lerInteiro("Número da categoria: ");
            CategoriaEvento c = CategoriaEvento.porIndice(n);
            if (c != null) {
                return c;
            }
            mensagem("Opção inválida.");
        }
    }

    public void mostrarUsuarioAtual(Usuario u) {
        if (u == null) {
            mensagem("Nenhum usuário selecionado. Use o menu para cadastrar ou entrar.");
        } else {
            mensagem("Usuário ativo: " + u.getNomeCompleto() + " (" + u.getCidadeResidencia() + ")");
        }
    }

    public void listarUsuarios(List<Usuario> lista) {
        if (lista.isEmpty()) {
            mensagem("Nenhum usuário cadastrado.");
            return;
        }
        int i = 1;
        for (Usuario u : lista) {
            System.out.printf("%d) %s | id: %s%n", i++, u, u.getId());
        }
    }

    public void listarEventosComStatus(List<Evento> ordenados, LocalDateTime agora) {
        if (ordenados.isEmpty()) {
            mensagem("Nenhum evento cadastrado.");
            return;
        }
        int i = 1;
        for (Evento e : ordenados) {
            Evento.StatusTemporal st = e.statusEm(agora);
            String tag = switch (st) {
                case FUTURO -> "[FUTURO]";
                case OCORRENDO_AGORA -> "[OCORRENDO AGORA]";
                case PASSADO -> "[JÁ OCORREU]";
            };
            System.out.printf("%n%d) %s %s%n", i++, tag, e.getNome());
            System.out.printf("    Id: %s%n", e.getId());
            System.out.printf("    Onde: %s%n", e.getEndereco());
            System.out.printf("    Categoria: %s (%s)%n", e.getCategoria().name(), e.getCategoria().getRotulo());
            System.out.printf("    Início: %s | Fim: %s (duração %d min)%n",
                    e.getHorarioInicio().format(FMT_BR),
                    e.getHorarioFim().format(FMT_BR),
                    e.getDuracaoMinutos());
            System.out.printf("    Descrição: %s%n", e.getDescricao());
            System.out.printf("    Participantes confirmados: %d%n", e.getIdsParticipantes().size());
        }
    }

    public void mostrarNotificacoes(List<String> msgs) {
        if (msgs.isEmpty()) {
            return;
        }
        titulo("Notificações");
        for (String m : msgs) {
            mensagem(m);
        }
    }

    public int menuPrincipal() {
        titulo("Sistema de eventos na sua cidade");
        mensagem("1 — Cadastrar usuário");
        mensagem("2 — Entrar (selecionar usuário ativo)");
        mensagem("3 — Cadastrar evento");
        mensagem("4 — Consultar eventos (ordenados: em andamento, futuros por proximidade, depois passados)");
        mensagem("5 — Participar de um evento listado");
        mensagem("6 — Meus eventos confirmados / cancelar participação");
        mensagem("7 — Apenas eventos ocorrendo neste momento");
        mensagem("8 — Apenas eventos que já ocorreram");
        mensagem("9 — Ver notificações dos meus eventos (próximas 48h + em andamento)");
        mensagem("0 — Sair");
        return lerInteiro("Escolha: ");
    }
}
