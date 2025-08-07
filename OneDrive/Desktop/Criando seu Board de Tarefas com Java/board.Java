git  add. 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// ========== 1. ENUMS ==========

enum StatusTarefa {
    A_FAZER("A Fazer"),
    EM_PROGRESSO("Em Progresso"),
    CONCLUIDA("Concluída");
    
    private final String descricao;
    
    StatusTarefa(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}

enum PrioridadeTarefa {
    BAIXA("Baixa", 1),
    MEDIA("Média", 2),
    ALTA("Alta", 3),
    CRITICA("Crítica", 4);
    
    private final String descricao;
    private final int nivel;
    
    PrioridadeTarefa(String descricao, int nivel) {
        this.descricao = descricao;
        this.nivel = nivel;
    }
    
    public String getDescricao() { return descricao; }
    public int getNivel() { return nivel; }
}

// ========== 2. CLASSES DE MODELO ==========

class Tarefa {
    private static int proximoId = 1;
    
    private final int id;
    private String titulo;
    private String descricao;
    private StatusTarefa status;
    private PrioridadeTarefa prioridade;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataVencimento;
    private String responsavel;
    
    // Construtor
    public Tarefa(String titulo, String descricao, PrioridadeTarefa prioridade, 
                  LocalDateTime dataVencimento, String responsavel) {
        this.id = proximoId++;
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = StatusTarefa.A_FAZER;
        this.prioridade = prioridade;
        this.dataCriacao = LocalDateTime.now();
        this.dataVencimento = dataVencimento;
        this.responsavel = responsavel;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    
    public PrioridadeTarefa getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeTarefa prioridade) { this.prioridade = prioridade; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDateTime dataVencimento) { this.dataVencimento = dataVencimento; }
    
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    
    // Método para verificar se está vencida
    public boolean isVencida() {
        return LocalDateTime.now().isAfter(dataVencimento) && status != StatusTarefa.CONCLUIDA;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String vencida = isVencida() ? " [VENCIDA]" : "";
        
        return String.format("#%d - %s%s\n" +
                           "  Status: %s | Prioridade: %s\n" +
                           "  Responsável: %s\n" +
                           "  Vencimento: %s\n" +
                           "  Descrição: %s",
                           id, titulo, vencida,
                           status.getDescricao(), prioridade.getDescricao(),
                           responsavel,
                           dataVencimento.format(formatter),
                           descricao);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tarefa tarefa = (Tarefa) obj;
        return id == tarefa.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

// ========== 3. CLASSE DE SERVIÇO ==========

class BoardService {
    private final Map<StatusTarefa, List<Tarefa>> board;
    
    public BoardService() {
        this.board = new HashMap<>();
        // Inicializa as listas para cada status
        for (StatusTarefa status : StatusTarefa.values()) {
            board.put(status, new ArrayList<>());
        }
    }
    
    // Criar nova tarefa
    public Tarefa criarTarefa(String titulo, String descricao, PrioridadeTarefa prioridade,
                             LocalDateTime dataVencimento, String responsavel) {
        Tarefa novaTarefa = new Tarefa(titulo, descricao, prioridade, dataVencimento, responsavel);
        board.get(StatusTarefa.A_FAZER).add(novaTarefa);
        System.out.println("✅ Tarefa criada com sucesso: " + novaTarefa.getTitulo());
        return novaTarefa;
    }
    
    // Mover tarefa entre status
    public boolean moverTarefa(int idTarefa, StatusTarefa novoStatus) {
        for (List<Tarefa> tarefas : board.values()) {
            Optional<Tarefa> tarefaOpt = tarefas.stream()
                    .filter(t -> t.getId() == idTarefa)
                    .findFirst();
            
            if (tarefaOpt.isPresent()) {
                Tarefa tarefa = tarefaOpt.get();
                tarefas.remove(tarefa);
                tarefa.setStatus(novoStatus);
                board.get(novoStatus).add(tarefa);
                System.out.printf("✅ Tarefa #%d movida para: %s%n", idTarefa, novoStatus.getDescricao());
                return true;
            }
        }
        System.out.println("❌ Tarefa não encontrada: " + idTarefa);
        return false;
    }
    
    // Buscar tarefa por ID
    public Optional<Tarefa> buscarTarefaPorId(int id) {
        return board.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.getId() == id)
                .findFirst();
    }
    
    // Listar tarefas por status
    public List<Tarefa> listarTarefasPorStatus(StatusTarefa status) {
        return new ArrayList<>(board.get(status));
    }
    
    // Listar tarefas por responsável
    public List<Tarefa> listarTarefasPorResponsavel(String responsavel) {
        return board.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.getResponsavel().equalsIgnoreCase(responsavel))
                .collect(Collectors.toList());
    }
    
    // Listar tarefas vencidas
    public List<Tarefa> listarTarefasVencidas() {
        return board.values().stream()
                .flatMap(List::stream)
                .filter(Tarefa::isVencida)
                .collect(Collectors.toList());
    }
    
    // Listar tarefas por prioridade
    public List<Tarefa> listarTarefasPorPrioridade(PrioridadeTarefa prioridade) {
        return board.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.getPrioridade() == prioridade)
                .sorted((t1, t2) -> t1.getDataVencimento().compareTo(t2.getDataVencimento()))
                .collect(Collectors.toList());
    }
    
    // Remover tarefa
    public boolean removerTarefa(int idTarefa) {
        for (List<Tarefa> tarefas : board.values()) {
            if (tarefas.removeIf(t -> t.getId() == idTarefa)) {
                System.out.println("✅ Tarefa removida com sucesso: " + idTarefa);
                return true;
            }
        }
        System.out.println("❌ Tarefa não encontrada: " + idTarefa);
        return false;
    }
    
    // Exibir board completo
    public void exibirBoard() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                         📋 BOARD DE TAREFAS                         ");
        System.out.println("=".repeat(80));
        
        for (StatusTarefa status : StatusTarefa.values()) {
            List<Tarefa> tarefas = board.get(status);
            System.out.printf("\n📌 %s (%d tarefas):%n", status.getDescricao().toUpperCase(), tarefas.size());
            System.out.println("-".repeat(50));
            
            if (tarefas.isEmpty()) {
                System.out.println("   Nenhuma tarefa");
            } else {
                tarefas.stream()
                        .sorted(Comparator.comparing((Tarefa t) -> t.getPrioridade().getNivel()).reversed()
                                .thenComparing(Tarefa::getDataVencimento))
                        .forEach(t -> {
                            System.out.println(t);
                            System.out.println();
                        });
            }
        }
    }
    
    // Estatísticas do board
    public void exibirEstatisticas() {
        int totalTarefas = board.values().stream().mapToInt(List::size).sum();
        int tarefasVencidas = (int) board.values().stream()
                .flatMap(List::stream)
                .filter(Tarefa::isVencida)
                .count();
        
        System.out.println("\n📊 ESTATÍSTICAS DO BOARD:");
        System.out.println("=".repeat(30));
        System.out.printf("Total de tarefas: %d%n", totalTarefas);
        System.out.printf("A fazer: %d%n", board.get(StatusTarefa.A_FAZER).size());
        System.out.printf("Em progresso: %d%n", board.get(StatusTarefa.EM_PROGRESSO).size());
        System.out.printf("Concluídas: %d%n", board.get(StatusTarefa.CONCLUIDA).size());
        System.out.printf("Vencidas: %d%n", tarefasVencidas);
    }
}

// ========== 4. CLASSE PRINCIPAL ==========

public class BoardTarefasApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BoardService boardService = new BoardService();
    
    public static void main(String[] args) {
        System.out.println("🚀 Bem-vindo ao Board de Tarefas - DIO GFT Start #7 Java!");
        
        // Criar algumas tarefas de exemplo
        criarTarefasExemplo();
        
        boolean executando = true;
        while (executando) {
            exibirMenu();
            int opcao = lerOpcao();
            
            switch (opcao) {
                case 1 -> criarNovaTarefa();
                case 2 -> boardService.exibirBoard();
                case 3 -> moverTarefa();
                case 4 -> listarTarefasPorFiltro();
                case 5 -> removerTarefa();
                case 6 -> boardService.exibirEstatisticas();
                case 0 -> {
                    System.out.println("👋 Obrigado por usar o Board de Tarefas!");
                    executando = false;
                }
                default -> System.out.println("❌ Opção inválida!");
            }
        }
        scanner.close();
    }
    
    private static void exibirMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           📋 MENU PRINCIPAL");
        System.out.println("=".repeat(50));
        System.out.println("1. 📝 Criar nova tarefa");
        System.out.println("2. 👀 Visualizar board");
        System.out.println("3. ➡️  Mover tarefa");
        System.out.println("4. 🔍 Listar tarefas (filtros)");
        System.out.println("5. 🗑️  Remover tarefa");
        System.out.println("6. 📊 Estatísticas");
        System.out.println("0. 🚪 Sair");
        System.out.print("Escolha uma opção: ");
    }
    
    private static int lerOpcao() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void criarNovaTarefa() {
        System.out.println("\n📝 CRIAR NOVA TAREFA");
        System.out.println("-".repeat(30));
        
        System.out.print("Título: ");
        String titulo = scanner.nextLine();
        
        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();
        
        System.out.print("Responsável: ");
        String responsavel = scanner.nextLine();
        
        // Selecionar prioridade
        System.out.println("\nPrioridades:");
        PrioridadeTarefa[] prioridades = PrioridadeTarefa.values();
        for (int i = 0; i < prioridades.length; i++) {
            System.out.printf("%d. %s%n", i + 1, prioridades[i].getDescricao());
        }
        System.out.print("Escolha a prioridade (1-4): ");
        int indicePrioridade = lerOpcao() - 1;
        
        if (indicePrioridade < 0 || indicePrioridade >= prioridades.length) {
            System.out.println("❌ Prioridade inválida!");
            return;
        }
        
        // Data de vencimento (simplificado - adiciona dias à data atual)
        System.out.print("Vencimento em quantos dias? ");
        int dias = lerOpcao();
        if (dias <= 0) {
            System.out.println("❌ Número de dias inválido!");
            return;
        }
        
        LocalDateTime dataVencimento = LocalDateTime.now().plusDays(dias);
        
        boardService.criarTarefa(titulo, descricao, prioridades[indicePrioridade], 
                                dataVencimento, responsavel);
    }
    
    private static void moverTarefa() {
        System.out.println("\n➡️ MOVER TAREFA");
        System.out.println("-".repeat(30));
        
        System.out.print("ID da tarefa: ");
        int id = lerOpcao();
        
        System.out.println("\nStatus disponíveis:");
        StatusTarefa[] status = StatusTarefa.values();
        for (int i = 0; i < status.length; i++) {
            System.out.printf("%d. %s%n", i + 1, status[i].getDescricao());
        }
        System.out.print("Escolha o novo status (1-3): ");
        int indiceStatus = lerOpcao() - 1;
        
        if (indiceStatus < 0 || indiceStatus >= status.length) {
            System.out.println("❌ Status inválido!");
            return;
        }
        
        boardService.moverTarefa(id, status[indiceStatus]);
    }
    
    private static void listarTarefasPorFiltro() {
        System.out.println("\n🔍 FILTROS DE LISTAGEM");
        System.out.println("-".repeat(30));
        System.out.println("1. Por responsável");
        System.out.println("2. Por prioridade");
        System.out.println("3. Tarefas vencidas");
        System.out.print("Escolha o filtro: ");
        
        int opcao = lerOpcao();
        switch (opcao) {
            case 1 -> {
                System.out.print("Nome do responsável: ");
                String responsavel = scanner.nextLine();
                List<Tarefa> tarefas = boardService.listarTarefasPorResponsavel(responsavel);
                exibirListaTarefas(tarefas, "Responsável: " + responsavel);
            }
            case 2 -> {
                System.out.println("Prioridades:");
                PrioridadeTarefa[] prioridades = PrioridadeTarefa.values();
                for (int i = 0; i < prioridades.length; i++) {
                    System.out.printf("%d. %s%n", i + 1, prioridades[i].getDescricao());
                }
                System.out.print("Escolha a prioridade: ");
                int indice = lerOpcao() - 1;
                if (indice >= 0 && indice < prioridades.length) {
                    List<Tarefa> tarefas = boardService.listarTarefasPorPrioridade(prioridades[indice]);
                    exibirListaTarefas(tarefas, "Prioridade: " + prioridades[indice].getDescricao());
                }
            }
            case 3 -> {
                List<Tarefa> tarefas = boardService.listarTarefasVencidas();
                exibirListaTarefas(tarefas, "Tarefas Vencidas");
            }
            default -> System.out.println("❌ Opção inválida!");
        }
    }
    
    private static void exibirListaTarefas(List<Tarefa> tarefas, String titulo) {
        System.out.printf("%n📋 %s (%d tarefas):%n", titulo.toUpperCase(), tarefas.size());
        System.out.println("=".repeat(50));
        
        if (tarefas.isEmpty()) {
            System.out.println("Nenhuma tarefa encontrada.");
        } else {
            tarefas.forEach(t -> {
                System.out.println(t);
                System.out.println();
            });
        }
    }
    
    private static void removerTarefa() {
        System.out.println("\n🗑️ REMOVER TAREFA");
        System.out.println("-".repeat(30));
        
        System.out.print("ID da tarefa: ");
        int id = lerOpcao();
        
        boardService.removerTarefa(id);
    }
    
    private static void criarTarefasExemplo() {
        // Criando tarefas de exemplo
        boardService.criarTarefa(
            "Implementar autenticação",
            "Desenvolver sistema de login e logout",
            PrioridadeTarefa.ALTA,
            LocalDateTime.now().plusDays(7),
            "João Silva"
        );
        
        boardService.criarTarefa(
            "Criar documentação API",
            "Documentar endpoints da API REST",
            PrioridadeTarefa.MEDIA,
            LocalDateTime.now().plusDays(10),
            "Maria Santos"
        );
        
        boardService.criarTarefa(
            "Corrigir bug crítico",
            "Resolver problema de performance na consulta",
            PrioridadeTarefa.CRITICA,
            LocalDateTime.now().plusDays(2),
            "Pedro Costa"
        );
        
        // Mover algumas tarefas para simular progresso
        boardService.moverTarefa(2, StatusTarefa.EM_PROGRESSO);
        boardService.moverTarefa(3, StatusTarefa.CONCLUIDA);
    }
}

/*
 CONCEITOS DE POO DEMONSTRADOS:

1. ENCAPSULAMENTO:
   - Atributos privados com getters/setters
   - Métodos de acesso controlado
   - Validações internas

2. ABSTRAÇÃO:
   - Classes representam conceitos reais (Tarefa, Board)
   - Interfaces limpas para operações complexas
   - Ocultação de detalhes de implementação

3. HERANÇA:
   - Enums com comportamentos específicos
   - Estruturas hierárquicas organizadas

4. POLIMORFISMO:
   - Sobrescrita de métodos (toString, equals, hashCode)
   - Interfaces funcionais com streams
   - Comportamentos diferentes para tipos similares


