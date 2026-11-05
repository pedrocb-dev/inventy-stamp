-- =====================================================================
-- Inventy Stamp App - 2/3: estrutura das tabelas
--
-- Executar no banco criado no passo 1, por exemplo:
--   mysql -u inventy_demo -p inventy_stamp_demo < database/02-schema.sql
--
-- Correspondência com as classes do sistema (Apêndice B do relatório):
--   pedidos           -> Pedido          (PedidoDAO)
--   demandas_pedido   -> DemandaPedido   (DemandaPedidoDAO)
--   rascunhos_pedido  -> RascunhoPedido  (RascunhoDAO)
--   estatisticas_mes  -> EstatisticasMes (EstatisticaDAO)
--   gastos            -> Gasto           (EstatisticaDAO)
--   pecas_por_pedido  -> valores inteiros (PecasPedidoDAO)
--
-- Os campos de enumeração guardam o nome da constante Java:
--   tipo_personalizacao: BORDADO, DTF, SUBLIMACAO, SILKSCREEN, BRINDES
--   status_pagamento:    PAGO, PAGAR_NA_RETIRADA, PARCIAL
--   situacao:            PREPARANDO, PRONTO
--   destinatario:        MARCO (recursos), SIMONE (costura)
-- =====================================================================

-- Pedidos registrados no atendimento
CREATE TABLE IF NOT EXISTS pedidos (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    cliente_nome        VARCHAR(150) NOT NULL,
    cliente_telefone    VARCHAR(30),
    anotacao            TEXT,
    tipo_personalizacao VARCHAR(30)  NOT NULL,
    data_confirmacao    DATE         NOT NULL,
    data_entrega        DATE         NOT NULL,
    valor               DECIMAL(10,2) NULL,              -- nulo quando "a combinar"
    status_pagamento    VARCHAR(30)  NOT NULL,
    situacao            VARCHAR(30)  NOT NULL DEFAULT 'PREPARANDO',
    resta               DECIMAL(10,2) NULL,              -- só no pagamento PARCIAL
    precisa_nf          TINYINT(1)   NOT NULL DEFAULT 0,
    dtf_montado         TINYINT(1)   NOT NULL DEFAULT 0,
    atendente           VARCHAR(100),
    precisa_cupomfiscal TINYINT(1)   NOT NULL DEFAULT 0,
    valor_a_combinar    TINYINT(1)   NOT NULL DEFAULT 0,
    criado_em           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Demandas internas vinculadas a um pedido (recursos ou costura).
-- Ao cancelar (excluir) um pedido, as suas demandas são excluídas junto.
CREATE TABLE IF NOT EXISTS demandas_pedido (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id    INT          NOT NULL,
    destinatario VARCHAR(50)  NOT NULL,
    texto        TEXT         NOT NULL,
    criado_em    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_demanda_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rascunhos do formulário de atendimento (valores guardados como texto,
-- pois representam um preenchimento ainda não validado)
CREATE TABLE IF NOT EXISTS rascunhos_pedido (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    cliente_nome         VARCHAR(150),
    cliente_telefone     VARCHAR(30),
    anotacao             TEXT,
    tipo_personalizacao  VARCHAR(30),
    data_confirmacao     VARCHAR(20),
    data_entrega         VARCHAR(20),
    valor                VARCHAR(30),
    status_pagamento     VARCHAR(30),
    resta                VARCHAR(30),
    precisa_nf           TINYINT(1)   NOT NULL DEFAULT 0,
    atendente            VARCHAR(100),
    demanda_texto        TEXT,
    demanda_destinatario VARCHAR(50),
    precisa_cupomfiscal  TINYINT(1)   NOT NULL DEFAULT 0,
    criado_em            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indicadores de cada mês. A chave única (ano, mes) permite que a conclusão
-- de um pedido some os valores ao mês corrente (INSERT ... ON DUPLICATE KEY UPDATE).
CREATE TABLE IF NOT EXISTS estatisticas_mes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    ano              INT           NOT NULL,
    mes              INT           NOT NULL,
    lucro_bruto      DECIMAL(12,2) NOT NULL DEFAULT 0,
    lucro_bordado    DECIMAL(12,2) NOT NULL DEFAULT 0,
    lucro_dtf        DECIMAL(12,2) NOT NULL DEFAULT 0,
    lucro_sublimacao DECIMAL(12,2) NOT NULL DEFAULT 0,
    lucro_silkscreen DECIMAL(12,2) NOT NULL DEFAULT 0,
    lucro_brindes    DECIMAL(12,2) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_estatisticas_ano_mes (ano, mes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Gastos operacionais de cada mês
CREATE TABLE IF NOT EXISTS gastos (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    estatistica_id INT           NOT NULL,
    categoria      VARCHAR(100)  NOT NULL,
    valor          DECIMAL(10,2) NOT NULL,
    criado_em      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gasto_estatistica FOREIGN KEY (estatistica_id)
        REFERENCES estatisticas_mes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Quantidade de peças de cada pedido concluído (alimenta o histograma mensal)
CREATE TABLE IF NOT EXISTS pecas_por_pedido (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    ano              INT NOT NULL,
    mes              INT NOT NULL,
    quantidade_pecas INT NOT NULL,
    KEY idx_pecas_ano_mes (ano, mes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
