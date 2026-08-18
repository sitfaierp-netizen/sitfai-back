-- =============================================================================
-- SITFAI ERP — Script de Inicialización de Bases de Datos Lógicas
-- Stack: MySQL 8.0+ / utf8mb4
-- =============================================================================
-- Crea las bases de datos independientes para los Bounded Contexts y Keycloak IAM.

CREATE DATABASE IF NOT EXISTS `sitfai_tienda` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `sitfai_inventory` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `sitfai_iam` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `tienda` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario de aplicación con permisos sobre todos los esquemas del ERP
CREATE USER IF NOT EXISTS 'sitfai_user'@'%' IDENTIFIED BY 'sitfai_secret_pwd';
GRANT ALL PRIVILEGES ON `sitfai_tienda`.* TO 'sitfai_user'@'%';
GRANT ALL PRIVILEGES ON `sitfai_inventory`.* TO 'sitfai_user'@'%';
GRANT ALL PRIVILEGES ON `sitfai_iam`.* TO 'sitfai_user'@'%';
GRANT ALL PRIVILEGES ON `tienda`.* TO 'sitfai_user'@'%';
FLUSH PRIVILEGES;
