-- =====================================================================
-- Inventy Stamp App - 1/3: criação do banco e do usuário de DEMONSTRAÇÃO
--
-- Executar como administrador do MySQL, por exemplo:
--   Linux:   sudo mysql < database/01-criar-banco-e-usuario.sql
--   Windows: mysql -u root -p < database\01-criar-banco-e-usuario.sql
--
-- O nome do banco, o usuário e a senha abaixo são os mesmos do arquivo
-- config.exemplo.properties. Se alterar aqui, altere também lá.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS inventy_stamp_demo
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'inventy_demo'@'localhost' IDENTIFIED BY 'demo1234';
GRANT ALL PRIVILEGES ON inventy_stamp_demo.* TO 'inventy_demo'@'localhost';
FLUSH PRIVILEGES;
