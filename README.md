En teoria deberia haber tanto el programa como la base de datos
y aqui esta la base de datos:
-- Tabla de productos
CREATE TABLE IF NOT EXISTS productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    precio DECIMAL(10,2) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de ventas
CREATE TABLE IF NOT EXISTS ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'COMPLETADA'
);

-- Tabla de detalle de ventas
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES ventas(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Tabla de transacciones (para registro de operaciones con niveles de aislamiento)
CREATE TABLE IF NOT EXISTS transacciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL, -- VENTA, ACTUALIZACIÓN_STOCK, etc.
    producto_id INT,
    referencia_id INT, -- ID de venta u otra entidad relacionada
    monto DECIMAL(10,2),
    cantidad_anterior INT, -- Para cambios de stock
    cantidad_nueva INT, -- Para cambios de stock
    fecha DATETIME NOT NULL,
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Datos de ejemplo para productos
INSERT INTO productos (nombre, descripcion, categoria, stock, precio) VALUES
('Paracetamol 500mg', 'Analgésico y antipirético', 'Analgésicos', 100, 5.50),
('Ibuprofeno 400mg', 'Antiinflamatorio no esteroideo', 'Antiinflamatorios', 80, 7.25),
('Amoxicilina 500mg', 'Antibiótico de amplio espectro', 'Antibióticos', 50, 12.75),
('Loratadina 10mg', 'Antihistamínico', 'Alergias', 60, 8.90),
('Omeprazol 20mg', 'Inhibidor de la bomba de protones', 'Digestivo', 70, 9.50),
('Vitamina C 1000mg', 'Suplemento vitamínico', 'Vitaminas', 120, 15.00),
('Alcohol 96°', 'Antiséptico', 'Primeros auxilios', 40, 4.25),
('Vendas elásticas', 'Para inmovilización', 'Primeros auxilios', 30, 6.50),
('Aspirina 100mg', 'Antiagregante plaquetario', 'Analgésicos', 90, 4.75),
('Enalapril 10mg', 'Antihipertensivo', 'Cardiovascular', 45, 14.25);
