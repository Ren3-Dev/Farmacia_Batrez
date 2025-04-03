package com.farmacia.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductosPanel extends JPanel {
    
    private JTable tblProductos;
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnActualizar;
    private JTextField txtBusqueda;
    
    private DefaultTableModel model;
    private Connection conexion;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    
    // Categorías predefinidas para el ComboBox
    private String[] categorias = {
        "Analgésicos",
        "Antiinflamatorios",
        "Antibióticos",
        "Antihistamínicos",
        "Digestivos",
        "Vitaminas",
        "Primeros auxilios",
        "Cardiovascular",
        "Dermatológicos",
        "Otros"
    };
    
    public ProductosPanel(Connection conexion) {
        this.conexion = conexion;
        initComponents();
        cargarProductos("");
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con búsqueda
        JPanel pnlBusqueda = new JPanel(new BorderLayout(5, 5));
        txtBusqueda = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> cargarProductos(txtBusqueda.getText()));
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarProductos(""));
        
        JPanel pnlBotonesBusqueda = new JPanel(new GridLayout(1, 2, 5, 5));
        pnlBotonesBusqueda.add(btnBuscar);
        pnlBotonesBusqueda.add(btnActualizar);
        
        pnlBusqueda.add(new JLabel("Buscar producto:"), BorderLayout.WEST);
        pnlBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        pnlBusqueda.add(pnlBotonesBusqueda, BorderLayout.EAST);
        
        // Tabla de productos
        model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Descripción", "Categoría", "Stock", "Precio"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que la tabla no sea editable directamente
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Integer.class; // Stock es entero
                if (columnIndex == 5) return Double.class; // Precio es decimal
                return String.class;
            }
        };
        
        tblProductos = new JTable(model);
        tblProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProductos.setAutoCreateRowSorter(true);
        
        // Botones CRUD
        btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(this::mostrarDialogoAgregar);
        
        btnEditar = new JButton("Editar");
        btnEditar.addActionListener(this::mostrarDialogoEditar);
        
        btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(this::eliminarProducto);
        
        JPanel pnlBotonesCRUD = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBotonesCRUD.add(btnAgregar);
        pnlBotonesCRUD.add(btnEditar);
        pnlBotonesCRUD.add(btnEliminar);
        
        // Organización general
        add(pnlBusqueda, BorderLayout.NORTH);
        add(new JScrollPane(tblProductos), BorderLayout.CENTER);
        add(pnlBotonesCRUD, BorderLayout.SOUTH);
    }
    
    private void cargarProductos(String busqueda) {
        model.setRowCount(0); // Limpiar tabla
        
        try {
            String sql = "SELECT id, nombre, descripcion, categoria, stock, precio FROM productos";
            
            if (!busqueda.isEmpty()) {
                sql += " WHERE nombre LIKE ? OR descripcion LIKE ? OR categoria LIKE ?";
            }
            
            sql += " ORDER BY nombre";
            
            PreparedStatement ps = conexion.prepareStatement(sql);
            
            if (!busqueda.isEmpty()) {
                String likeParam = "%" + busqueda + "%";
                ps.setString(1, likeParam);
                ps.setString(2, likeParam);
                ps.setString(3, likeParam);
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getInt("stock"),
                    rs.getDouble("precio")
                });
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarDialogoAgregar(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Agregar Nuevo Producto");
        dialog.setModal(true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Campos del formulario
        JTextField txtNombre = new JTextField();
        JTextArea txtDescripcion = new JTextArea(3, 20);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        JComboBox<String> cmbCategoria = new JComboBox<>(categorias);
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        JFormattedTextField txtPrecio = new JFormattedTextField(df);
        txtPrecio.setColumns(10);
        
        // Configurar el campo de precio para aceptar solo números
        txtPrecio.setValue(0.00);
        
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Descripción:"));
        panel.add(scrollDesc);
        panel.add(new JLabel("Categoría:"));
        panel.add(cmbCategoria);
        panel.add(new JLabel("Stock inicial:"));
        panel.add(spnStock);
        panel.add(new JLabel("Precio unitario:"));
        panel.add(txtPrecio);
        
        // Botones
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(ev -> {
            guardarProducto(
                txtNombre.getText(),
                txtDescripcion.getText(),
                cmbCategoria.getSelectedItem().toString(),
                (Integer) spnStock.getValue(),
                Double.parseDouble(txtPrecio.getText().replace(",", ""))
            );
            dialog.dispose();
        });
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(ev -> dialog.dispose());
        
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(pnlBotones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void mostrarDialogoEditar(ActionEvent e) {
        int selectedRow = tblProductos.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto para editar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener datos del producto seleccionado
        int id = (int) model.getValueAt(selectedRow, 0);
        String nombre = (String) model.getValueAt(selectedRow, 1);
        String descripcion = (String) model.getValueAt(selectedRow, 2);
        String categoria = (String) model.getValueAt(selectedRow, 3);
        int stock = (int) model.getValueAt(selectedRow, 4);
        double precio = (double) model.getValueAt(selectedRow, 5);
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Editar Producto");
        dialog.setModal(true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Campos del formulario con datos actuales
        JTextField txtNombre = new JTextField(nombre);
        JTextArea txtDescripcion = new JTextArea(descripcion, 3, 20);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        JComboBox<String> cmbCategoria = new JComboBox<>(categorias);
        cmbCategoria.setSelectedItem(categoria);
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(stock, 0, 9999, 1));
        JFormattedTextField txtPrecio = new JFormattedTextField(df);
        txtPrecio.setValue(precio);
        
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Descripción:"));
        panel.add(scrollDesc);
        panel.add(new JLabel("Categoría:"));
        panel.add(cmbCategoria);
        panel.add(new JLabel("Stock:"));
        panel.add(spnStock);
        panel.add(new JLabel("Precio unitario:"));
        panel.add(txtPrecio);
        
        // Botones
        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.addActionListener(ev -> {
            actualizarProducto(
                id,
                txtNombre.getText(),
                txtDescripcion.getText(),
                cmbCategoria.getSelectedItem().toString(),
                (Integer) spnStock.getValue(),
                Double.parseDouble(txtPrecio.getText().replace(",", ""))
            );
            dialog.dispose();
        });
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(ev -> dialog.dispose());
        
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(pnlBotones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void guardarProducto(String nombre, String descripcion, String categoria, int stock, double precio) {
        try {
            // Validación básica
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "El nombre del producto es requerido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Iniciar transacción
            conexion.setAutoCommit(false);
            
            // 1. Insertar producto
            String sql = "INSERT INTO productos (nombre, descripcion, categoria, stock, precio) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setString(3, categoria);
            ps.setInt(4, stock);
            ps.setDouble(5, precio);
            ps.executeUpdate();
            
            // Obtener ID del nuevo producto
            int productoId = 0;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                productoId = rs.getInt(1);
            }
            
            // 2. Registrar transacción
            String sqlTransaccion = "INSERT INTO transacciones " +
                "(tipo, producto_id, referencia_id, monto, cantidad_anterior, cantidad_nueva, fecha) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
            
            PreparedStatement psTrans = conexion.prepareStatement(sqlTransaccion);
            psTrans.setString(1, "ALTA_PRODUCTO");
            psTrans.setInt(2, productoId);
            psTrans.setInt(3, 0); // Sin referencia
            psTrans.setDouble(4, 0); // Sin monto
            psTrans.setInt(5, 0); // Stock anterior (0 para nuevo producto)
            psTrans.setInt(6, stock); // Nuevo stock
            psTrans.executeUpdate();
            
            conexion.commit();
            
            JOptionPane.showMessageDialog(this, 
                "Producto agregado correctamente",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            cargarProductos(""); // Refrescar lista
            
        } catch (SQLException ex) {
            try {
                conexion.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error al guardar producto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex1) {
                JOptionPane.showMessageDialog(this, 
                    "Error grave al hacer rollback: " + ex1.getMessage(),
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
    
    private void actualizarProducto(int id, String nombre, String descripcion, String categoria, int stock, double precio) {
        try {
            // Validación básica
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "El nombre del producto es requerido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Obtener stock actual para registro de transacción
            int stockAnterior = 0;
            PreparedStatement psStock = conexion.prepareStatement(
                "SELECT stock FROM productos WHERE id = ?");
            psStock.setInt(1, id);
            ResultSet rs = psStock.executeQuery();
            if (rs.next()) {
                stockAnterior = rs.getInt("stock");
            }
            
            // Iniciar transacción
            conexion.setAutoCommit(false);
            
            // 1. Actualizar producto
            String sql = "UPDATE productos SET nombre = ?, descripcion = ?, categoria = ?, stock = ?, precio = ? " +
                         "WHERE id = ?";
            
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setString(3, categoria);
            ps.setInt(4, stock);
            ps.setDouble(5, precio);
            ps.setInt(6, id);
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No se encontró el producto a actualizar",
                    "Error", JOptionPane.ERROR_MESSAGE);
                conexion.rollback();
                return;
            }
            
            // 2. Registrar transacción solo si cambió el stock
            if (stockAnterior != stock) {
                String sqlTransaccion = "INSERT INTO transacciones " +
                    "(tipo, producto_id, referencia_id, monto, cantidad_anterior, cantidad_nueva, fecha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";
                
                PreparedStatement psTrans = conexion.prepareStatement(sqlTransaccion);
                psTrans.setString(1, "AJUSTE_STOCK");
                psTrans.setInt(2, id);
                psTrans.setInt(3, 0); // Sin referencia
                psTrans.setDouble(4, 0); // Sin monto
                psTrans.setInt(5, stockAnterior);
                psTrans.setInt(6, stock);
                psTrans.executeUpdate();
            }
            
            conexion.commit();
            
            JOptionPane.showMessageDialog(this, 
                "Producto actualizado correctamente",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            cargarProductos(""); // Refrescar lista
            
        } catch (SQLException ex) {
            try {
                conexion.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error al actualizar producto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex1) {
                JOptionPane.showMessageDialog(this, 
                    "Error grave al hacer rollback: " + ex1.getMessage(),
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
    
    private void eliminarProducto(ActionEvent e) {
        int selectedRow = tblProductos.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto para eliminar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) model.getValueAt(selectedRow, 0);
        String nombre = (String) model.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea eliminar el producto: " + nombre + "?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Obtener stock actual para registro de transacción
                int stockAnterior = 0;
                PreparedStatement psStock = conexion.prepareStatement(
                    "SELECT stock FROM productos WHERE id = ?");
                psStock.setInt(1, id);
                ResultSet rs = psStock.executeQuery();
                if (rs.next()) {
                    stockAnterior = rs.getInt("stock");
                }
                
                // Iniciar transacción
                conexion.setAutoCommit(false);
                
                // 1. Eliminar producto
                String sql = "DELETE FROM productos WHERE id = ?";
                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setInt(1, id);
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows == 0) {
                    JOptionPane.showMessageDialog(this, 
                        "No se encontró el producto a eliminar",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    conexion.rollback();
                    return;
                }
                
                // 2. Registrar transacción
                String sqlTransaccion = "INSERT INTO transacciones " +
                    "(tipo, producto_id, referencia_id, monto, cantidad_anterior, cantidad_nueva, fecha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";
                
                PreparedStatement psTrans = conexion.prepareStatement(sqlTransaccion);
                psTrans.setString(1, "BAJA_PRODUCTO");
                psTrans.setInt(2, id);
                psTrans.setInt(3, 0); // Sin referencia
                psTrans.setDouble(4, 0); // Sin monto
                psTrans.setInt(5, stockAnterior);
                psTrans.setInt(6, 0); // Stock nuevo (0 porque se eliminó)
                psTrans.executeUpdate();
                
                conexion.commit();
                
                JOptionPane.showMessageDialog(this, 
                    "Producto eliminado correctamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                cargarProductos(""); // Refrescar lista
                
            } catch (SQLException ex) {
                try {
                    conexion.rollback();
                    JOptionPane.showMessageDialog(this, 
                        "Error al eliminar producto: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex1) {
                    JOptionPane.showMessageDialog(this, 
                        "Error grave al hacer rollback: " + ex1.getMessage(),
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
}