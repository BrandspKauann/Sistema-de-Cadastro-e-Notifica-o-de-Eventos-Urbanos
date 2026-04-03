package br.edu.eventos.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Evento na cidade: nome, endereço, categoria, horário e descrição (obrigatórios).
 * Duração em minutos permite determinar se está ocorrendo "agora".
 */
public class Evento {
    private final String id;
    private String nome;
    private String endereco;
    private CategoriaEvento categoria;
    private LocalDateTime horarioInicio;
    private int duracaoMinutos;
    private String descricao;
    private final Set<String> idsParticipantes;

    public Evento(String nome, String endereco, CategoriaEvento categoria,
                  LocalDateTime horarioInicio, int duracaoMinutos, String descricao) {
        this.id = UUID.randomUUID().toString();
        this.nome = Objects.requireNonNull(nome, "nome");
        this.endereco = Objects.requireNonNull(endereco, "endereco");
        this.categoria = Objects.requireNonNull(categoria, "categoria");
        this.horarioInicio = Objects.requireNonNull(horarioInicio, "horario");
        this.duracaoMinutos = Math.max(1, duracaoMinutos);
        this.descricao = Objects.requireNonNull(descricao, "descricao");
        this.idsParticipantes = new LinkedHashSet<>();
    }

    public Evento(String id, String nome, String endereco, CategoriaEvento categoria,
                  LocalDateTime horarioInicio, int duracaoMinutos, String descricao,
                  Set<String> idsParticipantes) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.horarioInicio = horarioInicio;
        this.duracaoMinutos = duracaoMinutos;
        this.descricao = descricao;
        this.idsParticipantes = new LinkedHashSet<>(idsParticipantes != null ? idsParticipantes : Set.of());
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public CategoriaEvento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEvento categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalDateTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(int duracaoMinutos) {
        this.duracaoMinutos = Math.max(1, duracaoMinutos);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getHorarioFim() {
        return horarioInicio.plusMinutes(duracaoMinutos);
    }

    public Set<String> getIdsParticipantes() {
        return Collections.unmodifiableSet(idsParticipantes);
    }

    public boolean adicionarParticipante(String usuarioId) {
        return idsParticipantes.add(usuarioId);
    }

    public boolean removerParticipante(String usuarioId) {
        return idsParticipantes.remove(usuarioId);
    }

    public boolean usuarioParticipa(String usuarioId) {
        return idsParticipantes.contains(usuarioId);
    }

    public StatusTemporal statusEm(LocalDateTime agora) {
        if (agora.isBefore(horarioInicio)) {
            return StatusTemporal.FUTURO;
        }
        if (!agora.isBefore(getHorarioFim())) {
            return StatusTemporal.PASSADO;
        }
        return StatusTemporal.OCORRENDO_AGORA;
    }

    public enum StatusTemporal {
        FUTURO,
        OCORRENDO_AGORA,
        PASSADO
    }
}
