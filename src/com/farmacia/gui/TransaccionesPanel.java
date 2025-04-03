package com.farmacia.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;

public class TransaccionesPanel extends JPanel {
    
    private JTable tblTransacciones;
    private JComboBox<String> cmbNivelAislamiento;
    private JButton btnMostrarTransacciones;
    private JButton btnEjecutarTransaccion;
    
    private Connection conexion;
    
    public void setConnection(Connection conexion) {
        this.conexion = conexion;
    }
    
    public TransaccionesPanel() {
        initComponents();
        setupPanel();
        setupDatabase();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Modelo de tabla para transacciones
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Tipo", "Producto ID", "Referencia", "Monto", "Cant. Ant.", "Cant. Nueva", "Fecha"}, 0);
        tblTransacciones = new JTable(model);
        
        // Panel superior con controles
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        cmbNivelAislamiento = new JComboBox<>(new String[]{
            "READ UNCOMMITTED", 
            "READ COMMITTED", 
            "REPEATABLE READ", 
            "SERIALIZABLE"
        });
        
        btnMostrarTransacciones = new JButton("Mostrar Transacciones");
        btnEjecutarTransaccion = new JButton("Ejecutar Transacción Demo");
        
        pnlControles.add(new JLabel("Nivel de Aislamiento:"));
        pnlControles.add(cmbNivelAislamiento);
        pnlControles.add(btnMostrarTransacciones);
        pnlControles.add(btnEjecutarTransaccion);
        
        add(pnlControles, BorderLayout.NORTH);
        add(new JScrollPane(tblTransacciones), BorderLayout.CENTER);
        
        // Listeners
        btnMostrarTransacciones.addActionListener(this::mostrarTransacciones);
        btnEjecutarTransaccion.addActionListener(this::ejecutarTransaccionDemo);
    }
    
    private void setupPanel() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/localdb", 
                "root", 
                "");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al conectar con la base de datos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarTransacciones(ActionEvent e) {
        try {
            DefaultTableModel model = (DefaultTableModel) tblTransacciones.getModel();
            model.setRowCount(0); // Limpiar tabla
            
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transacciones ORDER BY fecha DESC");
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("tipo"));
                row.add(rs.getInt("producto_id"));
                row.add(rs.getInt("referencia_id"));
                row.add(rs.getDouble("monto"));
                row.add(rs.getInt("cantidad_anterior"));
                row.add(rs.getInt("cantidad_nueva"));
                row.add(rs.getTimestamp("fecha"));
                model.addRow(row);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Transacciones cargadas correctamente", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar transacciones: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ejecutarTransaccionDemo(ActionEvent e) {
        String nivelAislamiento = (String) cmbNivelAislamiento.getSelectedItem();
        int isolationLevel;
        
        if (conexion == null) {
            JOptionPane.showMessageDialog(this, 
                "No hay conexión a la base de datos",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        switch(nivelAislamiento) {
            case "READ UNCOMMITTED":
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
                break;
            case "READ COMMITTED":
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                break;
            case "REPEATABLE READ":
                isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
                break;
            case "SERIALIZABLE":
                isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
                break;
            default:
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
        }
        
        try {
            // Desactivar autocommit
            conexion.setAutoCommit(false);
            conexion.setTransactionIsolation(isolationLevel);
            
            // Iniciar transacción
            JOptionPane.showMessageDialog(this, 
                "Iniciando transacción con nivel: " + nivelAislamiento,
                "Información", JOptionPane.INFORMATION_MESSAGE);
            
            // Ejemplo de operación: actualizar stock y registrar transacción
            int productoId = 1; // Paracetamol
            int cantidadVendida = 5;
            
            // 1. Obtener stock actual
            PreparedStatement psStock = conexion.prepareStatement(
                "SELECT stock FROM productos WHERE id = ?");
            psStock.setInt(1, productoId);
            ResultSet rs = psStock.executeQuery();
            
            if (rs.next()) {
                int stockActual = rs.getInt("stock");
                
                // 2. Actualizar stock
                PreparedStatement psUpdate = conexion.prepareStatement(
                    "UPDATE productos SET stock = ? WHERE id = ?");
                psUpdate.setInt(1, stockActual - cantidadVendida);
                psUpdate.setInt(2, productoId);
                psUpdate.executeUpdate();
                
                // 3. Registrar transacción
                PreparedStatement psTrans = conexion.prepareStatement(
                    "INSERT INTO transacciones (tipo, producto_id, referencia_id, " +
                    "monto, cantidad_anterior, cantidad_nueva, fecha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())");
                psTrans.setString(1, "VENTA_DEMO");
                psTrans.setInt(2, productoId);
                psTrans.setInt(3, 0); // Sin referencia en este demo
                psTrans.setDouble(4, 5.50 * cantidadVendida);
                psTrans.setInt(5, stockActual);
                psTrans.setInt(6, stockActual - cantidadVendida);
                psTrans.executeUpdate();
                
                // Confirmar transacción
                conexion.commit();
                
                JOptionPane.showMessageDialog(this, 
                    "Transacción completada con éxito!\n" +
                    "Producto ID: " + productoId + "\n" +
                    "Stock anterior: " + stockActual + "\n" +
                    "Stock nuevo: " + (stockActual - cantidadVendida),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Actualizar tabla
                mostrarTransacciones(null);
            }
            
        } catch (SQLException ex) {
            try {
                conexion.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error en transacción (ROLLBACK ejecutado): " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex1) {
                JOptionPane.showMessageDialog(this, 
                    "Error al hacer rollback: " + ex1.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
