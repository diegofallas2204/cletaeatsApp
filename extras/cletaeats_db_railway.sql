-- ========================================
-- BASE DE DATOS CLETAEATS EN RAILWAY
-- ========================================
-- Este script está adaptado para ejecutarse en la base de datos `railway`
-- usando tu conexión MySQL de Railway.

USE railway;

-- ========================================
-- 1. USUARIOS (LOGIN)
-- ========================================
CREATE TABLE IF NOT EXISTS usuarios (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(50) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          rol ENUM('cliente','repartidor','admin') NOT NULL,
                          activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- ========================================
-- 2. CLIENTES
-- ========================================
CREATE TABLE IF NOT EXISTS clientes (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          usuario_id INT UNIQUE NOT NULL,
                          cedula VARCHAR(20) UNIQUE NOT NULL,
                          nombre VARCHAR(100) NOT NULL,
                          direccion TEXT NOT NULL,
                          telefono VARCHAR(20),
                          email VARCHAR(100),
                          tarjeta VARCHAR(50),
                          estado ENUM('activo','suspendido') DEFAULT 'activo',
                          FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ========================================
-- 3. REPARTIDORES
-- ========================================
CREATE TABLE IF NOT EXISTS repartidores (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              usuario_id INT UNIQUE NOT NULL,
                              cedula VARCHAR(20) UNIQUE NOT NULL,
                              nombre VARCHAR(100) NOT NULL,
                              email VARCHAR(100),
                              direccion TEXT,
                              telefono VARCHAR(20),
                              tarjeta VARCHAR(50),
                              estado ENUM('disponible','ocupado') DEFAULT 'disponible',
                              km_recorridos FLOAT DEFAULT 0,
                              amonestaciones INT DEFAULT 0,
                              FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ========================================
-- 4. ADMINS
-- ========================================
CREATE TABLE IF NOT EXISTS admins (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        usuario_id INT UNIQUE NOT NULL,
                        nombre VARCHAR(100),
                        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ========================================
-- 5. RESTAURANTES
-- ========================================
CREATE TABLE IF NOT EXISTS restaurantes (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL,
                              cedula_juridica VARCHAR(50) UNIQUE NOT NULL,
                              direccion TEXT,
                              tipo_comida VARCHAR(50)
) ENGINE=InnoDB;

-- ========================================
-- 6. COMBOS
-- ========================================
CREATE TABLE IF NOT EXISTS combos (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        restaurante_id INT NOT NULL,
                        numero_combo INT NOT NULL CHECK (numero_combo BETWEEN 1 AND 9),
                        nombre VARCHAR(100) NOT NULL,
                        precio FLOAT NOT NULL,
                        FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id) ON DELETE CASCADE,
                        UNIQUE(restaurante_id, numero_combo)
) ENGINE=InnoDB;

-- ========================================
-- 7. PEDIDOS
-- ========================================
CREATE TABLE IF NOT EXISTS pedidos (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         cliente_id INT NOT NULL,
                         restaurante_id INT NOT NULL,
                         repartidor_id INT,
                         estado ENUM('preparacion','camino','entregado','suspendido') DEFAULT 'preparacion',
                         distancia_km FLOAT DEFAULT 0,
                         subtotal FLOAT DEFAULT 0,
                         costo_envio FLOAT DEFAULT 0,
                         iva FLOAT DEFAULT 0,
                         total FLOAT DEFAULT 0,
                         fecha_pedido DATETIME DEFAULT CURRENT_TIMESTAMP,
                         fecha_entrega DATETIME NULL,
                         FOREIGN KEY (cliente_id) REFERENCES clientes(id),
                         FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
                         FOREIGN KEY (repartidor_id) REFERENCES repartidores(id)
) ENGINE=InnoDB;

-- ========================================
-- 8. DETALLE PEDIDO
-- ========================================
CREATE TABLE IF NOT EXISTS pedido_detalle (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                pedido_id INT NOT NULL,
                                combo_id INT NOT NULL,
                                cantidad INT DEFAULT 1,
                                precio_unitario FLOAT,
                                agrandado BOOLEAN DEFAULT FALSE,
                                notas TEXT,
                                FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
                                FOREIGN KEY (combo_id) REFERENCES combos(id)
) ENGINE=InnoDB;

-- ========================================
-- 9. QUEJAS
-- ========================================
CREATE TABLE IF NOT EXISTS quejas (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        pedido_id INT NOT NULL,
                        repartidor_id INT NOT NULL,
                        descripcion TEXT NOT NULL,
                        fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
                        FOREIGN KEY (repartidor_id) REFERENCES repartidores(id)
) ENGINE=InnoDB;

-- ========================================
-- 10. TARJETAS CLIENTE
-- ========================================
CREATE TABLE IF NOT EXISTS tarjetas_cliente (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  cliente_id INT NOT NULL,
                                  numero_tarjeta VARCHAR(20) NOT NULL,
                                  fecha_vencimiento VARCHAR(5) NOT NULL,
                                  cvv VARCHAR(4) NOT NULL,
                                  FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ========================================
-- DATOS DE PRUEBA
-- ========================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE quejas;
TRUNCATE TABLE pedido_detalle;
TRUNCATE TABLE pedidos;
TRUNCATE TABLE combos;
TRUNCATE TABLE restaurantes;
TRUNCATE TABLE admins;
SET FOREIGN_KEY_CHECKS = 1;

-- Restaurantes
INSERT INTO restaurantes (nombre, cedula_juridica, direccion, tipo_comida) VALUES
('Pizzeria La Nonna', '3-101-000001', 'Heredia, Centro', 'Pizza'),
('Burger Factory', '3-101-000002', 'San Jose, Escazu', 'Burger'),
('Pasta Italia', '3-101-000003', 'Alajuela, Centro', 'Pasta'),
('Green Garden', '3-101-000004', 'Cartago, Centro', 'Ensalada'),
('Sakura Sushi', '3-101-000005', 'Heredia, Belen', 'Sushi'),
('Aroma Cafe', '3-101-000006', 'San Jose, Curridabat', 'Cafe'),
('Sweet Delights', '3-101-000007', 'Alajuela, Guacima', 'Postres'),
('Taco Loco', '3-101-000008', 'Cartago, Paraiso', 'Tacos'),
('Crispy Chicken', '3-101-000009', 'Heredia, San Joaquin', 'Pollo'),
('Great Wall', '3-101-000010', 'San Jose, Barrio Chino', 'China'),
('Mariscos del Puerto', '3-101-000011', 'Puntarenas, Paseo de los Turistas', 'Mariscos'),
('Smoothie Bar', '3-101-000012', 'Guanacaste, Tamarindo', 'Bebidas');

-- Combos
INSERT INTO combos (restaurante_id, numero_combo, nombre, precio) VALUES
(1, 1, 'Margarita Personal', 4000), (1, 2, 'Pepperoni Mediana', 5000), (1, 3, 'Suprema Grande', 6000),
(1, 4, 'Hawaiana Familiar', 7000), (1, 5, 'Veggie Lover', 8000), (1, 6, 'Meat Lovers King', 9000),
(2, 1, 'Classic Burger', 4000), (2, 2, 'Double Cheese', 5000), (2, 3, 'Bacon Monster', 6000),
(2, 4, 'Crispy Chicken Sandwich', 7000), (2, 5, 'Fish Burger Meal', 8000), (2, 6, 'The Ultimate Factory', 9000),
(3, 1, 'Spaghetti Pomodoro', 4000), (3, 2, 'Fettuccine Alfredo', 5000), (3, 3, 'Lasagna de Carne', 6000),
(3, 4, 'Penne Arrabiata', 7000), (3, 5, 'Ravioli de Queso', 8000), (3, 6, 'Gnocchi al Pesto', 9000),
(4, 1, 'Ensalada Cesar', 4000), (4, 2, 'Greek Salad', 5000), (4, 3, 'Quinoa Bowl', 6000),
(4, 4, 'Santa Fe Chicken', 7000), (4, 5, 'Tuna Poke Salad', 8000), (4, 6, 'Superfood Mix', 9000),
(5, 1, 'California Roll', 4000), (5, 2, 'Philadelphia Roll', 5000), (5, 3, 'Spicy Tuna', 6000),
(5, 4, 'Tiger Roll', 7000), (5, 5, 'Dragon Roll Especial', 8000), (5, 6, 'Chef Platter 20pcs', 9000),
(6, 1, 'Expresso + Muffin', 4000), (6, 2, 'Capuccino + Croissant', 5000), (6, 3, 'Latte + Tarta', 6000),
(6, 4, 'Mocha + Cheesecake', 7000), (6, 5, 'Frappe + Brownie', 8000), (6, 6, 'Coffee Lovers Pack', 9000),
(7, 1, 'Copa de Helado', 4000), (7, 2, 'Porcion de Tiramisu', 5000), (7, 3, 'Tres Leches', 6000),
(7, 4, 'Pie de Limon', 7000), (7, 5, 'Volcan de Chocolate', 8000), (7, 6, 'Mega Dessert Platter', 9000),
(8, 1, 'Taco al Pastor (3)', 4000), (8, 2, 'Gringa de Pollo', 5000), (8, 3, 'Quesabirria', 6000),
(8, 4, 'Burrito Supremo', 7000), (8, 5, 'Enchiladas Suizas', 8000), (8, 6, 'Taco Loco Family Pack', 9000),
(9, 1, '2 Piezas + Papas', 4000), (9, 2, '3 Piezas + Acompanamiento', 5000), (9, 3, 'Burger de Pollo', 6000),
(9, 4, 'Bucket 8 Piezas', 7000), (9, 5, 'Bucket 12 Piezas', 8000), (9, 6, 'Bucket Familiar Completo', 9000),
(10, 1, 'Arroz Frito + Chop Suey', 4000), (10, 2, 'Pollo Agridulce', 5000), (10, 3, 'Cerdo al Jengibre', 6000),
(10, 4, 'Sopa de Wonton', 7000), (10, 5, 'Pato Pekin Porcion', 8000), (10, 6, 'Gran Buffet Imperial', 9000),
(11, 1, 'Ceviche Mixto', 4000), (11, 2, 'Filete de Pargo', 5000), (11, 3, 'Arroz con Calamares', 6000),
(11, 4, 'Langosta a la Mantequilla', 7000), (11, 5, 'Sopa de Mariscos', 8000), (11, 6, 'Gran Banquete Marino', 9000),
(12, 1, 'Smoothie Tropical', 4000), (12, 2, 'Jugo Natural Grande', 5000), (12, 3, 'Batido de Fresa', 6000),
(12, 4, 'Limonada Imperial', 7000), (12, 5, 'Te Frio Artesanal', 8000), (12, 6, 'Pack 4 Bebidas Premium', 9000);

-- Admins, pedidos y otros datos de ejemplo están comentados porque requieren datos de usuarios, clientes y repartidores existentes.
