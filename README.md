# Sistema de Cadastro e Notificação de Eventos Urbanos

Eu desenvolvi esse sistema em **Java** para um trabalho da faculdade. A ideia é cadastrar **usuários** e **eventos** que acontecem na cidade (festas, shows, esportes, etc.), consultar tudo pelo **console** e marcar em quais eventos eu quero ir.

## O que o programa faz

- Cadastro de usuário com nome, e-mail, cidade onde moro, telefone e idade  
- Cadastro de eventos com nome, endereço, categoria, data/hora de início, duração em minutos e descrição  
- Listar eventos: mostra se já passou, se está rolando agora ou se ainda vai acontecer (uso `LocalDateTime` para isso)  
- Confirmar participação em um evento e depois ver só os meus, com opção de cancelar  
- Notificações simples no console para eventos em que confirmei presença (lembretes nas próximas 48h e aviso se está em andamento)  
- Os eventos são salvos no arquivo **`events.data`** e carregados de novo quando abro o programa  
- Os usuários ficam em **`users.data`** para não perder o cadastro entre uma execução e outra  

## Como eu rodo o projeto

Preciso ter **JDK 17** (ou superior) e **Maven** instalados.

Na pasta do projeto:

```bash
mvn compile exec:java -Dexec.mainClass="br.edu.eventos.Main"
```

Ou gerar o JAR e executar (os arquivos `.data` são criados na pasta de onde eu rodo o comando):

```bash
mvn package
java -jar target/sistema-eventos-cidade-1.0.0.jar
```

No Eclipse ou IntelliJ eu importo como **projeto Maven** e rodo a classe `br.edu.eventos.Main`.

## Organização do código

Separei em camadas no estilo **MVC**:

| Pacote | Função |
|--------|--------|
| `model` | Classes `Usuario`, `Evento`, enum de categorias |
| `view` | Leitura e impressão no console |
| `controller` | Menus e fluxo da aplicação |
| `service` | Regras de negócio (ordenar eventos, participação, etc.) |
| `persistence` | Ler e gravar os arquivos de texto |

O diagrama de classes está em **`docs/diagrama-classes.md`** (formato Mermaid).

## Observação

É um sistema **100% console**, sem interface gráfica ou web, como pediu a atividade.

---

*Repositório criado para entrega acadêmica.*
