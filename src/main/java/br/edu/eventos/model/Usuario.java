package br.edu.eventos.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Usuário do sistema (mínimo 3 atributos além do identificador).
 */
public class Usuario {
    private final String id;
    private String nomeCompleto;
    private String email;
    private String cidadeResidencia;
    private String telefone;
    private int idade;

    public Usuario(String nomeCompleto, String email, String cidadeResidencia, String telefone, int idade) {
        this.id = UUID.randomUUID().toString();
        this.nomeCompleto = Objects.requireNonNull(nomeCompleto, "nome");
        this.email = Objects.requireNonNull(email, "email");
        this.cidadeResidencia = Objects.requireNonNull(cidadeResidencia, "cidade");
        this.telefone = telefone != null ? telefone : "";
        this.idade = idade;
    }

    /** Reconstrução a partir da persistência */
    public Usuario(String id, String nomeCompleto, String email, String cidadeResidencia, String telefone, int idade) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.cidadeResidencia = cidadeResidencia;
        this.telefone = telefone;
        this.idade = idade;
    }

    public String getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCidadeResidencia() {
        return cidadeResidencia;
    }

    public void setCidadeResidencia(String cidadeResidencia) {
        this.cidadeResidencia = cidadeResidencia;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | tel: %s | %d anos",
                nomeCompleto, email, cidadeResidencia, telefone, idade);
    }
}
