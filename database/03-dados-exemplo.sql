-- =====================================================================
-- Inventy Stamp App - 3/3: dados de EXEMPLO (fictícios)
--
-- Todos os clientes, telefones e valores abaixo são inventados, apenas
-- para demonstrar o sistema. Nenhum dado real da empresa está aqui.
--
-- As datas são calculadas a partir do dia em que o script é executado,
-- para que o painel mostre todas as cores de prazo (atrasado, entrega no
-- dia, próximos dois dias e com folga) e as estatísticas tenham o mês
-- atual e os três anteriores.
--
-- Executar depois do 02-schema.sql, por exemplo:
--   mysql -u inventy_demo -p inventy_stamp_demo < database/03-dados-exemplo.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Pedidos em aberto (aparecem no painel de Pedidos)
-- ---------------------------------------------------------------------
INSERT INTO pedidos (cliente_nome, cliente_telefone, anotacao, tipo_personalizacao,
                     data_confirmacao, data_entrega, valor, status_pagamento, situacao,
                     resta, precisa_nf, dtf_montado, atendente, precisa_cupomfiscal, valor_a_combinar)
VALUES
('Escola Aurora',          '(11) 90000-0101', '40 camisetas brancas com brasão no peito',              'DTF',        DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1200.00, 'PARCIAL',           'PREPARANDO', 600.00, 1, 0, 'Pedro',  1, 0),
('Clube Vila Serena',      '(11) 90000-0102', '15 bonés bordados com logo do clube',                   'BORDADO',    DATE_SUB(CURDATE(), INTERVAL 5 DAY), CURDATE(),                          675.00,  'PAGO',              'PREPARANDO', NULL,   0, 0, 'Kennia', 1, 0),
('Padaria Trigo Dourado',  '(11) 90000-0103', '6 aventais com nome da padaria',                        'SUBLIMACAO', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 330.00,  'PAGAR_NA_RETIRADA', 'PREPARANDO', NULL,   0, 0, 'Bia',    0, 0),
('Academia Movimento',     '(11) 90000-0104', '25 regatas pretas, estampa frente e costas',            'DTF',        DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 875.00,  'PAGO',              'PREPARANDO', NULL,   1, 1, 'Pedro',  1, 0),
('Igreja Esperança',       '(11) 90000-0105', '60 camisetas para o encontro de jovens',                'SILKSCREEN', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1800.00, 'PARCIAL',           'PREPARANDO', 900.00, 1, 0, 'Kennia', 1, 0),
('Ana Ribeiro',            '(11) 90000-0106', '2 canecas personalizadas com foto (aguardando arte)',   'BRINDES',    DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), NULL,    'PAGAR_NA_RETIRADA', 'PREPARANDO', NULL,   0, 0, 'Bia',    0, 1),
('Time Amigos da Quadra',  '(11) 90000-0107', '12 camisas de futebol com número e nome nas costas',    'DTF',        CURDATE(),                           DATE_ADD(CURDATE(), INTERVAL 9 DAY), 540.00,  'PAGAR_NA_RETIRADA', 'PREPARANDO', NULL,   0, 0, 'Marco',  0, 0),
('Consultório Bem-Estar',  '(11) 90000-0108', '8 jalecos com bordado do nome',                         'BORDADO',    CURDATE(),                           DATE_ADD(CURDATE(), INTERVAL 12 DAY), 520.00, 'PAGO',              'PREPARANDO', NULL,   1, 0, 'Pedro',  1, 0);

-- ---------------------------------------------------------------------
-- Pedidos já concluídos (não aparecem no painel; já contabilizados)
-- ---------------------------------------------------------------------
INSERT INTO pedidos (cliente_nome, cliente_telefone, anotacao, tipo_personalizacao,
                     data_confirmacao, data_entrega, valor, status_pagamento, situacao,
                     resta, precisa_nf, dtf_montado, atendente, precisa_cupomfiscal, valor_a_combinar)
VALUES
('Mercado Bom Preço',      '(11) 90000-0109', '20 camisetas de uniforme',                              'DTF',        DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 12 DAY), 700.00, 'PAGO', 'PRONTO', NULL, 1, 1, 'Kennia', 1, 0),
('Carlos Mendes',          '(11) 90000-0110', '1 jaqueta com bordado nas costas',                      'BORDADO',    DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 9 DAY),  180.00, 'PAGO', 'PRONTO', NULL, 0, 0, 'Bia',    0, 0);

-- ---------------------------------------------------------------------
-- Demandas internas para recursos (MARCO) e costura (SIMONE)
-- ---------------------------------------------------------------------
INSERT INTO demandas_pedido (pedido_id, destinatario, texto)
SELECT id, 'MARCO',  'Comprar 40 camisetas brancas tamanhos P a GG' FROM pedidos WHERE cliente_nome = 'Escola Aurora';
INSERT INTO demandas_pedido (pedido_id, destinatario, texto)
SELECT id, 'SIMONE', 'Ajustar a barra de 6 aventais antes da sublimação' FROM pedidos WHERE cliente_nome = 'Padaria Trigo Dourado';
INSERT INTO demandas_pedido (pedido_id, destinatario, texto)
SELECT id, 'MARCO',  'Separar 60 camisetas azul-marinho e tinta branca para o silk' FROM pedidos WHERE cliente_nome = 'Igreja Esperança';
INSERT INTO demandas_pedido (pedido_id, destinatario, texto)
SELECT id, 'SIMONE', 'Fazer bainha nos 8 jalecos' FROM pedidos WHERE cliente_nome = 'Consultório Bem-Estar';

-- ---------------------------------------------------------------------
-- Um rascunho pendente no atendimento
-- ---------------------------------------------------------------------
INSERT INTO rascunhos_pedido (cliente_nome, cliente_telefone, anotacao, tipo_personalizacao,
                              data_confirmacao, data_entrega, valor, status_pagamento, resta,
                              precisa_nf, atendente, demanda_texto, demanda_destinatario, precisa_cupomfiscal)
VALUES ('Loja Estilo Urbano', '(11) 90000-0111', 'Orçamento de 30 moletons, cliente vai confirmar cores',
        'SILKSCREEN', DATE_FORMAT(CURDATE(), '%Y-%m-%d'), DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 15 DAY), '%Y-%m-%d'),
        '', 'PAGAR_NA_RETIRADA', '', 0, 'Kennia', NULL, NULL, 0);

-- ---------------------------------------------------------------------
-- Estatísticas do mês atual e dos três meses anteriores
-- (lucro_bruto = soma dos valores por tipo de personalização)
-- ---------------------------------------------------------------------
INSERT INTO estatisticas_mes (ano, mes, lucro_bruto, lucro_bordado, lucro_dtf, lucro_sublimacao, lucro_silkscreen, lucro_brindes)
VALUES
(YEAR(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), MONTH(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), 7450.00, 1850.00, 3200.00,  900.00, 1100.00, 400.00),
(YEAR(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), MONTH(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), 8930.00, 2100.00, 3900.00,  780.00, 1650.00, 500.00),
(YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 9820.00, 2400.00, 4300.00, 1020.00, 1500.00, 600.00),
(YEAR(CURDATE()),                             MONTH(CURDATE()),                             3880.00,  980.00, 1900.00,  300.00,  500.00, 200.00);

-- Gastos operacionais de cada mês
INSERT INTO gastos (estatistica_id, categoria, valor)
SELECT e.id, g.categoria, g.valor
FROM estatisticas_mes e
JOIN (
    SELECT 3 AS meses_atras, 'Aluguel'           AS categoria, 1800.00 AS valor UNION ALL
    SELECT 3, 'Filme e tinta DTF',      950.00 UNION ALL
    SELECT 3, 'Energia elétrica',       420.00 UNION ALL
    SELECT 2, 'Aluguel',               1800.00 UNION ALL
    SELECT 2, 'Camisetas lisas',       1350.00 UNION ALL
    SELECT 2, 'Linhas de bordado',      260.00 UNION ALL
    SELECT 1, 'Aluguel',               1800.00 UNION ALL
    SELECT 1, 'Filme e tinta DTF',     1100.00 UNION ALL
    SELECT 1, 'Manutenção da prensa',   380.00 UNION ALL
    SELECT 0, 'Aluguel',               1800.00 UNION ALL
    SELECT 0, 'Energia elétrica',       410.00
) g ON e.ano = YEAR(DATE_SUB(CURDATE(), INTERVAL g.meses_atras MONTH))
   AND e.mes = MONTH(DATE_SUB(CURDATE(), INTERVAL g.meses_atras MONTH));

-- Quantidade de peças dos pedidos concluídos em cada mês (histograma)
INSERT INTO pecas_por_pedido (ano, mes, quantidade_pecas)
SELECT YEAR(DATE_SUB(CURDATE(), INTERVAL p.meses_atras MONTH)),
       MONTH(DATE_SUB(CURDATE(), INTERVAL p.meses_atras MONTH)),
       p.qtd
FROM (
    SELECT 3 AS meses_atras, 1 AS qtd UNION ALL SELECT 3, 4 UNION ALL SELECT 3, 12 UNION ALL SELECT 3, 30 UNION ALL SELECT 3, 8 UNION ALL
    SELECT 2, 2 UNION ALL SELECT 2, 6 UNION ALL SELECT 2, 15 UNION ALL SELECT 2, 50 UNION ALL SELECT 2, 20 UNION ALL SELECT 2, 3 UNION ALL
    SELECT 1, 1 UNION ALL SELECT 1, 10 UNION ALL SELECT 1, 25 UNION ALL SELECT 1, 40 UNION ALL SELECT 1, 7 UNION ALL SELECT 1, 18 UNION ALL SELECT 1, 2 UNION ALL
    SELECT 0, 20 UNION ALL SELECT 0, 1
) p;
