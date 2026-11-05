# Inventy Stamp App

Aplicativo desktop de gestão de pedidos, produção, demandas internas e indicadores financeiros da **Inventy Stamp**, microempresa de personalização de uniformes, brindes e roupas.

Projeto Integrador do curso de Tecnologia em Análise e Desenvolvimento de Sistemas — **IFSP, Câmpus Guarulhos (2026)**.

- **Autores:** Pedro de Castro Barba e Michael Miguel Silva Santos
- **Orientador:** Antonio Angelo de Souza Tartaglia

Este repositório contém todo o código-fonte do protótipo funcional, incluindo os scripts do banco de dados (pasta [`database/`](database)). Os requisitos, os casos de uso e os diagramas de classes e de sequência estão no relatório técnico (Apêndices A e B), e os nomes de classes, pacotes e tabelas usados aqui são os mesmos de lá.

---

## Sumário

1. [Visão geral](#visão-geral)
2. [Funcionalidades](#funcionalidades)
3. [Casos de uso demonstrados pelo protótipo](#casos-de-uso-demonstrados-pelo-protótipo)
4. [Arquitetura](#arquitetura)
5. [Tecnologias](#tecnologias)
6. [Estrutura do repositório](#estrutura-do-repositório)
7. [Como executar o protótipo](#como-executar-o-protótipo)
8. [Gerar o executável (.jar)](#gerar-o-executável-jar)
9. [Implantação nas estações da empresa](#implantação-nas-estações-da-empresa)
10. [Limitações fora da empresa](#limitações-fora-da-empresa)

---

## Visão geral

O sistema é um aplicativo **desktop monolítico**: cada computador da empresa executa o aplicativo, e todos se conectam a um **único banco de dados MySQL** hospedado em um computador da rede local. O usuário trabalha em três áreas, acessadas pela tela inicial ou pelo menu lateral:

| Área | Para que serve |
|---|---|
| **Atendimento** | Registrar novos pedidos e gerenciar rascunhos |
| **Pedidos** | Painel de pedidos em aberto e, em uma segunda aba, as demandas internas vinculadas aos pedidos |
| **Estatísticas** | Indicadores financeiros mensais (acesso protegido por senha) |

## Funcionalidades

Os códigos entre parênteses são os requisitos funcionais do relatório (Seção 2.2 e Apêndice A).

**Atendimento**
- Registro de pedido com cliente, telefone (formatado automaticamente), anotação, tipo de personalização (bordado, DTF, sublimação, silkscreen ou brindes), datas, valor, forma de pagamento, nota fiscal, cupom fiscal e atendente (RF01).
- Valor **"a combinar"**, que fixa o pagamento em "pagar na retirada" e bloqueia a conclusão e o cupom enquanto não houver valor (RF02).
- Demanda opcional para recursos ou costura, registrada junto com o pedido (RF06).
- Rascunhos: salvos pelo botão ou automaticamente ao sair da tela, com aviso de pendentes, para carregar ou excluir depois (RF08).
- Abertura da conversa com o cliente no WhatsApp Desktop (RF13).

**Pedidos**
- Cartões ordenados pela entrega, com barra de prazo: preto (atrasado), vermelho (hoje), laranja (próximos dois dias) e verde (com folga) (RF03).
- Filtro por um ou mais tipos de personalização e pesquisa por cliente, código, telefone, anotação, datas, atendente, NF ou cupom (RF04).
- Conclusão do pedido com a quantidade de peças, que alimenta os indicadores do mês (RF05).
- Edição e cancelamento de pedidos (RF11), etiqueta de produção e cupom fiscal simplificado copiado como imagem (RF07).
- Sinalização dos DTFs ainda não montados (RF12).
- Aba **Demandas**: registro, filtro por destinatário, pesquisa, edição e cancelamento (RF06).
- Atualização automática das listas a cada minuto, para uso simultâneo em várias estações (RNF02, RNF03).

**Estatísticas**
- Acesso mediante senha (RF10).
- Por mês: faturamento ("lucro bruto"), gastos, lucro líquido e gráficos de evolução do faturamento, faturamento por tipo e peças por pedido (RF09).
- Registro e remoção de gastos operacionais (RF14).

## Casos de uso demonstrados pelo protótipo

O protótipo implementa todos os casos de uso do Apêndice B. Os de maior complexidade, que exercitam todas as camadas da arquitetura e reduzem os riscos técnicos do projeto, são:

| Caso de uso | Por que é crítico | Onde está no código |
|---|---|---|
| **UC01 – Registrar pedido** | Validação, conversão de valores e gravação do pedido seguida da demanda vinculada, usando o identificador gerado pelo banco | `AtendimentoView` → `PedidoController.salvarPedido` → `PedidoDAO`, `DemandaPedidoDAO` |
| **UC06 – Concluir pedido** | Uma única ação atualiza três tabelas: situação do pedido, peças do mês e indicadores mensais (somados com `ON DUPLICATE KEY UPDATE`) | `PedidosView` → `PedidoController.concluirPedido` → `PedidoDAO`, `PecasPedidoDAO`, `EstatisticaDAO` |
| **UC11 – Registrar demanda** | Busca de pedidos em aberto e vínculo entre tabelas (consulta com `JOIN`) | `PedidosView` → `DemandaController` → `DemandaPedidoDAO`, `PedidoDAO` |
| **UC14/UC16 – Consultar indicadores** | Autenticação e consolidação de faturamento, gastos e lucro líquido | `MainApp` → `EstatisticasView` → `EstatisticaController` → `EstatisticaDAO`, `PecasPedidoDAO` |
| **Uso simultâneo (RNF02/RNF03)** | Várias estações sobre uma única base, com recarga automática das listas | `ConexaoDB` e atualização periódica em `PedidosView` |

Os diagramas de sequência desses cenários estão no Apêndice B do relatório (Figuras 8 a 11).

## Arquitetura

Padrão **MVC** com camada de acesso a dados (**DAO**), no pacote `com.inventystamp`:

```
com.inventystamp
├── MainApp                 tela inicial, menu lateral e senha das estatísticas
├── Launcher                ponto de entrada para executar pela IDE ou com java -jar
├── model/                  Pedido, RascunhoPedido, DemandaPedido, EstatisticasMes, Gasto
│                           e os enums TipoPersonalizacao, StatusPagamento, SituacaoPedido
├── view/                   AtendimentoView, PedidosView, EstatisticasView (telas JavaFX)
├── controller/             PedidoController, DemandaController, EstatisticaController (regras de negócio)
├── dao/                    PedidoDAO, RascunhoDAO, DemandaPedidoDAO, EstatisticaDAO, PecasPedidoDAO (SQL)
└── util/                   ConexaoDB (conexões) e Configuracao (leitura do config.properties)
```

Todas as consultas usam `PreparedStatement`, ou seja, consultas parametrizadas (RNF07). Como registrado no relatório (Seção 2.1), a separação em camadas não é integral: a `AtendimentoView` usa o `RascunhoDAO` diretamente, e algumas verificações de apresentação ficam nas telas.

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java (JDK) | 21 | Linguagem |
| JavaFX | 21.0.11 | Interface gráfica (baixado pelo Maven) |
| MySQL | 8.x | Banco de dados |
| MySQL Connector/J | 8.3.0 | Driver JDBC (baixado pelo Maven) |
| Apache Maven | 3.9+ | Dependências e build |

## Estrutura do repositório

```
inventy-stamp/
├── README.md
├── pom.xml                         dependências e build (Maven)
├── config.exemplo.properties       modelo de configuração (copiar para config.properties)
├── database/
│   ├── 01-criar-banco-e-usuario.sql   cria o banco e o usuário de demonstração
│   ├── 02-schema.sql                  cria as 6 tabelas
│   └── 03-dados-exemplo.sql           dados fictícios para demonstração
└── src/
    ├── main/java/com/inventystamp/    código-fonte (ver Arquitetura)
    ├── main/resources/                ícone e imagens
    └── distribuicao/                  scripts das estações Windows (iniciar.vbs, criar-atalho.vbs, logo.ico)
```

## Como executar o protótipo

### 1. Pré-requisitos

- **JDK 21** ([Eclipse Temurin](https://adoptium.net/) ou OpenJDK 21)
- **Apache Maven** ([download](https://maven.apache.org/download.cgi)); o IntelliJ IDEA já traz um Maven embutido
- **MySQL Server 8** ([download](https://dev.mysql.com/downloads/mysql/))

No Ubuntu, tudo de uma vez:

```bash
sudo apt install openjdk-21-jdk maven mysql-server
```

Não é preciso instalar o JavaFX separadamente: o Maven baixa as bibliotecas.

### 2. Criar o banco de demonstração

Na pasta do projeto, execute os três scripts, na ordem.

**Linux:**

```bash
sudo mysql < database/01-criar-banco-e-usuario.sql
mysql -u inventy_demo -pdemo1234 inventy_stamp_demo < database/02-schema.sql
mysql -u inventy_demo -pdemo1234 inventy_stamp_demo < database/03-dados-exemplo.sql
```

**Windows** (Prompt de Comando, na pasta do projeto; se o comando `mysql` não for reconhecido, use o caminho completo, por exemplo `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"`):

```bat
mysql -u root -p < database\01-criar-banco-e-usuario.sql
mysql -u inventy_demo -pdemo1234 inventy_stamp_demo < database\02-schema.sql
mysql -u inventy_demo -pdemo1234 inventy_stamp_demo < database\03-dados-exemplo.sql
```

Também é possível abrir e executar os três arquivos, na mesma ordem, pelo **MySQL Workbench**.

Os dados de exemplo são **fictícios**. As datas são calculadas a partir do dia da execução, para que o painel mostre todas as cores de prazo e as estatísticas tenham quatro meses.

### 3. Criar o arquivo de configuração

Copie o modelo para `config.properties`, na raiz do projeto:

```bash
cp config.exemplo.properties config.properties      # Linux
copy config.exemplo.properties config.properties    # Windows
```

Os valores do modelo já apontam para o banco de demonstração. O `config.properties` está no `.gitignore` e nunca deve ser enviado ao repositório.

### 4. Executar

**Pelo terminal**, na raiz do projeto:

```bash
mvn javafx:run
```

**Pelo IntelliJ IDEA:** abra a pasta do projeto, aguarde o Maven baixar as dependências e execute a classe `com.inventystamp.Launcher` (botão direito → *Run 'Launcher.main()'*). Executar a `MainApp` diretamente gera o erro "componentes de runtime do JavaFX não encontrados"; a classe `Launcher` existe para evitar isso.

**Senha da área de Estatísticas (demonstração):** `demo`

## Gerar o executável (.jar)

```bash
mvn clean package -DskipTests
```

O arquivo gerado é **`target/inventy-stamp.jar`**, que já inclui as bibliotecas do projeto. Ele pode ser executado com:

```bash
java -jar target/inventy-stamp.jar
```

O `config.properties` precisa estar na pasta de onde o comando é executado ou na mesma pasta do `.jar`. As bibliotecas do JavaFX incluídas no `.jar` são as do sistema operacional em que o build foi feito: gere o `.jar` no Windows para usar no Windows.

As versões prontas do executável ficam na página **[Releases](../../releases)** deste repositório, e não no código-fonte.

## Implantação nas estações da empresa

Em cada estação Windows da empresa:

1. Instale o JDK 21 e o JavaFX SDK 21.0.11 em `C:\javafx-sdk\javafx-sdk-21.0.11`.
2. Em uma pasta do aplicativo, coloque:
   - `inventy-stamp.jar` (das Releases ou gerado pelo Maven);
   - `iniciar.vbs`, `criar-atalho.vbs` e `logo.ico` (de `src/distribuicao/`);
   - um `config.properties` com os dados do banco da empresa (servidor da rede local) e a senha das estatísticas.
3. Execute `criar-atalho.vbs` uma vez, para criar o atalho na área de trabalho.

Para atualizar uma estação, basta substituir o `inventy-stamp.jar`. Os dados reais de acesso ao banco da empresa ficam somente no `config.properties` de cada estação, e não no repositório.

## Limitações fora da empresa

Ao executar o protótipo em outro computador:

- **Impressão de etiquetas:** procura a impressora térmica da empresa (`LABEL`). Se ela não existir, o sistema exibe um aviso e continua funcionando.
- **WhatsApp:** abre a conversa no aplicativo WhatsApp Desktop do Windows. Sem o aplicativo instalado, ou em outros sistemas operacionais, o botão não tem efeito.
- **Cupom fiscal:** é gerado como imagem e copiado para a área de transferência. Pode ser colado em qualquer programa (Paint, Word, WhatsApp etc.).

Todas as demais funcionalidades funcionam integralmente com o banco de demonstração.

---

Projeto acadêmico desenvolvido para uso interno da Inventy Stamp.
