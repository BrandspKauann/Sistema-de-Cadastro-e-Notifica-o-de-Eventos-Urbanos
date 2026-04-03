# Diagrama de classes (UML — visão lógica)

O diagrama abaixo pode ser colado em qualquer visualizador Mermaid (GitHub, VS Code, etc.).

```mermaid
classDiagram
    direction TB

    class Main {
        +main(String[] args)$ void
    }

    class ControladorApp {
        -VisaoConsole visao
        -ServicoEventos servico
        -Usuario usuarioAtivo
        +executar() void
    }

    class VisaoConsole {
        +menuPrincipal() int
        +listarEventosComStatus(List~Evento~, LocalDateTime) void
        +lerDataHora(String) LocalDateTime
    }

    class ServicoEventos {
        -List~Evento~ eventos
        -List~Usuario~ usuarios
        +carregarDados() void
        +persistir() void
        +eventosOrdenadosPorProximidade(LocalDateTime) List~Evento~
        +confirmarParticipacao(String, String) boolean
        +cancelarParticipacao(String, String) boolean
    }

    class ArmazenamentoEventos {
        +carregar(Path) List~Evento~
        +salvar(Path, List~Evento~) void
    }

    class ArmazenamentoUsuarios {
        +carregar(Path) List~Usuario~
        +salvar(Path, List~Usuario~) void
    }

    class Usuario {
        -String id
        -String nomeCompleto
        -String email
        -String cidadeResidencia
        -String telefone
        -int idade
    }

    class Evento {
        -String id
        -String nome
        -String endereco
        -CategoriaEvento categoria
        -LocalDateTime horarioInicio
        -int duracaoMinutos
        -String descricao
        -Set~String~ idsParticipantes
        +statusEm(LocalDateTime) StatusTemporal
    }

    class CategoriaEvento {
        <<enumeration>>
        FESTA
        EVENTO_ESPORTIVO
        SHOW
        CULTURAL
        FEIRA
        ACADEMICO
        OUTRO
    }

    class StatusTemporal {
        <<enumeration>>
        FUTURO
        OCORRENDO_AGORA
        PASSADO
    }

    Main --> ControladorApp : inicia
    ControladorApp --> VisaoConsole
    ControladorApp --> ServicoEventos
    ServicoEventos --> ArmazenamentoEventos
    ServicoEventos --> ArmazenamentoUsuarios
    ServicoEventos o-- Evento
    ServicoEventos o-- Usuario
    Evento --> CategoriaEvento
    Evento +-- StatusTemporal
```
