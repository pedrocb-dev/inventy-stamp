# Inventy Stamp — Sistema de Gestão

Sistema desktop de gestão interna para a estamparia **Inventy Stamp**.  
Permite comunicação entre os diferentes responsáveis da empresa, gerenciamento de pedidos, tarefas e demandas.

---

## Responsáveis e Acessos

| Responsável | Pedidos | Tarefas | Atendimento | Demandas |
|---|---|---|---|---|
| Atendimento | ✅ | ✅ | ✅ | ✅ |
| Produção | ✅ | ✅ | ❌ | ✅ |
| Bordado | ✅ | ❌ | ❌ | ✅ |
| Costura | ❌ | ❌ | ❌ | ✅ |
| Recursos | ❌ | ❌ | ❌ | ✅ |

---

## Funcionalidades

### Pedidos
- Resumo dos pedidos em cards organizados por prazo de entrega
- Cores de sinalização: 🔴 vermelho (entrega hoje), 🟡 amarelo (entrega amanhã), 🟢 verde (entrega futura)
- Filtro por tipo: bordado ou demais
- Exibe valor, valor restante (pagamento parcial) e prazo em dias
- Tarefas vinculadas ao pedido dentro do card
- Edição e remoção de pedidos

### Atendimento
- Formulário completo de cadastro de pedidos
- Campos: cliente, telefone, anotações, tipo de personalização, datas, valor e pagamento
- Campo "Resta" aparece automaticamente ao selecionar pagamento parcial
- Tarefas automáticas criadas ao salvar: "Fazer layout", "Fazer arquivo", "Dobrar"

### Tarefas
- Visível para Atendimento e Produção
- Exibe tarefas vinculadas a pedidos
- Marcar como concluída, editar e remover

### Demandas
- Acessível por todos os responsáveis
- Envio de demandas para Costura ou Recursos (conforme perfil)
- Lista de demandas recebidas com status de conclusão

---

## Tecnologias

- **Java 21**
- **JavaFX 21.0.11** — interface gráfica
- **MySQL** — banco de dados compartilhado
- **Maven** — gerenciamento de dependências

---

## Estrutura do Projeto

src/main/java/com/inventystamp/
├── model/ → Classes de dados (Pedido, Tarefa, Demanda, Usuario...)
├── dao/ → Acesso ao banco de dados
├── view/ → Telas da aplicação (JavaFX)
├── util/ → Utilitários (conexão com banco)
└── MainApp.java → Ponto de entrada

---

## Configuração do Banco de Dados

1. Instale o MySQL e crie o banco:
```sql
CREATE DATABASE inventy_stamp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Execute o script de criação das tabelas (arquivo `banco.sql` na raiz do projeto)

3. Configure a conexão em `src/main/java/com/inventystamp/util/ConexaoDB.java`:
```java
private static final String URL = "jdbc:mysql://IP_DO_SERVIDOR:3306/inventy_stamp...";
private static final String USUARIO = "seu_usuario";
private static final String SENHA = "sua_senha";
```

---

## Como Rodar

### Desenvolvimento (IntelliJ)
1. Abra o projeto no IntelliJ IDEA
2. Configure o JDK 21
3. Adicione o JavaFX SDK em `C:\javafx-sdk\javafx-sdk-21.0.11\lib`
4. Configure as VM options: --module-path "C:\javafx-sdk\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml
5. Rode a classe `MainApp`

### Produção (outros computadores)
1. Instale o JDK 21 e o JavaFX SDK 21.0.11
2. Copie o arquivo `inventy-stamp-1.0-SNAPSHOT.jar`
3. Execute com o arquivo `iniciar.bat`:
```bat
@echo off
java --module-path "C:\javafx-sdk\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml -jar inventy-stamp-1.0-SNAPSHOT.jar
pause
```

---

## Status do Projeto

🚧 Em desenvolvimento

---

*Desenvolvido para uso interno da Inventy Stamp.*