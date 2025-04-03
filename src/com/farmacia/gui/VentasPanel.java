package com.farmacia.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VentasPanel extends JPanel {
    
    private JTable tblProductos;
    private JTable tblCarrito;
    private JButton btnAgregarCarrito;
    private JButton btnFinalizarVenta;
    private JButton btnCancelarVenta;
    private JButton btnQuitarProducto;
    private JTextField txtBusqueda;
    private JTextField txtCantidad;
    private JLabel lblTotal;
    private JLabel lblCliente;
    
    private DefaultTableModel modelProductos;
    private DefaultTableModel modelCarrito;
    
    private Connection conexion;
    private List<ProductoCarrito> carrito;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    
    private class ProductoCarrito {
        int id;
        String nombre;
        double precio;
        int cantidad;
        int stockDisponible;
        
        public ProductoCarrito(int id, String nombre, double precio, int cantidad, int stockDisponible) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
            this.stockDisponible = stockDisponible;
        }
        
        public double getSubtotal() {
            return precio * cantidad;
        }
    }
    
    public VentasPanel(Connection conexion) {
        this.conexion = conexion;
        this.carrito = new ArrayList<>();
        initComponents();
        cargarProductos("");
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con búsqueda y datos del cliente
        JPanel pnlSuperior = new JPanel(new BorderLayout(10, 10));
        
        // Panel de búsqueda
        JPanel pnlBusqueda = new JPanel(new BorderLayout(5, 5));
        txtBusqueda = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> cargarProductos(txtBusqueda.getText()));
        
        pnlBusqueda.add(new JLabel("Buscar producto:"), BorderLayout.WEST);
        pnlBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        pnlBusqueda.add(btnBuscar, BorderLayout.EAST);
        
        // Panel de cantidad
        JPanel pnlCantidad = new JPanel(new BorderLayout(5, 5));
        txtCantidad = new JTextField("1");
        pnlCantidad.add(new JLabel("Cantidad:"), BorderLayout.WEST);
        pnlCantidad.add(txtCantidad, BorderLayout.CENTER);
        
        // Panel cliente (simplificado, podrías expandirlo)
        lblCliente = new JLabel("Cliente: GENERAL");
        
        // Organizar panel superior
        JPanel pnlControlesSuperiores = new JPanel(new GridLayout(1, 3, 10, 10));
        pnlControlesSuperiores.add(pnlBusqueda);
        pnlControlesSuperiores.add(pnlCantidad);
        pnlControlesSuperiores.add(lblCliente);
        
        pnlSuperior.add(pnlControlesSuperiores, BorderLayout.CENTER);
        
        // Tabla de productos
        modelProductos = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Precio", "Stock", "Categoría"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblProductos = new JTable(modelProductos);
        tblProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Tabla de carrito
        modelCarrito = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Precio", "Cantidad", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCarrito = new JTable(modelCarrito);
        tblCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Botones
        btnAgregarCarrito = new JButton("Agregar al carrito");
        btnAgregarCarrito.addActionListener(this::agregarAlCarrito);
        
        btnQuitarProducto = new JButton("Quitar producto");
        btnQuitarProducto.addActionListener(this::quitarDelCarrito);
        
        btnFinalizarVenta = new JButton("Finalizar venta (F2)");
        btnFinalizarVenta.addActionListener(this::finalizarVenta);
        
        btnCancelarVenta = new JButton("Cancelar venta (Esc)");
        btnCancelarVenta.addActionListener(this::cancelarVenta);
        
        // Configurar atajos de teclado
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        
        im.put(KeyStroke.getKeyStroke("F2"), "finalizar");
        am.put("finalizar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarVenta(null);
            }
        });
        
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancelar");
        am.put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelarVenta(null);
            }
        });
        
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBotones.add(btnAgregarCarrito);
        pnlBotones.add(btnQuitarProducto);
        pnlBotones.add(btnFinalizarVenta);
        pnlBotones.add(btnCancelarVenta);
        
        // Panel total
        JPanel pnlTotal = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: $0.00", JLabel.RIGHT);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        pnlTotal.add(lblTotal, BorderLayout.EAST);
        
        // Organización de paneles
        JPanel pnlIzquierda = new JPanel(new BorderLayout());
        pnlIzquierda.add(pnlSuperior, BorderLayout.NORTH);
        pnlIzquierda.add(new JScrollPane(tblProductos), BorderLayout.CENTER);
        
        JPanel pnlDerecha = new JPanel(new BorderLayout());
        pnlDerecha.add(new JScrollPane(tblCarrito), BorderLayout.CENTER);
        pnlDerecha.add(pnlTotal, BorderLayout.SOUTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlIzquierda, pnlDerecha);
        splitPane.setResizeWeight(0.6);
        
        add(splitPane, BorderLayout.CENTER);
        add(pnlBotones, BorderLayout.SOUTH);
    }
    
    private void cargarProductos(String busqueda) {
        modelProductos.setRowCount(0);
        
        try {
            String sql = "SELECT id, nombre, precio, stock, categoria FROM productos WHERE stock > 0 ";
            
            if (!busqueda.isEmpty()) {
                sql += "AND (nombre LIKE ? OR categoria LIKE ?)";
            }
            
            sql += " ORDER BY nombre";
            
            PreparedStatement ps = conexion.prepareStatement(sql);
            
            if (!busqueda.isEmpty()) {
                ps.setString(1, "%" + busqueda + "%");
                ps.setString(2, "%" + busqueda + "%");
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                modelProductos.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    df.format(rs.getDouble("precio")),
                    rs.getInt("stock"),
                    rs.getString("categoria")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void agregarAlCarrito(ActionEvent e) {
        int selectedRow = tblProductos.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto de la lista",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "La cantidad debe ser mayor a cero",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int id = (int) modelProductos.getValueAt(selectedRow, 0);
            String nombre = (String) modelProductos.getValueAt(selectedRow, 1);
            double precio = Double.parseDouble(
                ((String) modelProductos.getValueAt(selectedRow, 2)).replace(",", ""));
            int stock = (int) modelProductos.getValueAt(selectedRow, 3);
            
            if (cantidad > stock) {
                JOptionPane.showMessageDialog(this, 
                    "No hay suficiente stock. Stock disponible: " + stock,
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verificar si el producto ya está en el carrito
            for (ProductoCarrito item : carrito) {
                if (item.id == id) {
                    int nuevaCantidad = item.cantidad + cantidad;
                    if (nuevaCantidad > item.stockDisponible) {
                        JOptionPane.showMessageDialog(this, 
                            "No hay suficiente stock. Stock disponible: " + item.stockDisponible,
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    item.cantidad = nuevaCantidad;
                    actualizarCarrito();
                    return;
                }
            }
            
            // Si no está en el carrito, agregarlo
            carrito.add(new ProductoCarrito(id, nombre, precio, cantidad, stock));
            actualizarCarrito();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Ingrese una cantidad válida",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void quitarDelCarrito(ActionEvent e) {
        int selectedRow = tblCarrito.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto del carrito",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        carrito.remove(selectedRow);
        actualizarCarrito();
    }
    
    private void actualizarCarrito() {
        modelCarrito.setRowCount(0);
        double total = 0;
        
        for (ProductoCarrito item : carrito) {
            modelCarrito.addRow(new Object[]{
                item.id,
                item.nombre,
                df.format(item.precio),
                item.cantidad,
                df.format(item.getSubtotal())
            });
            total += item.getSubtotal();
        }
        
        lblTotal.setText("Total: $" + df.format(total));
    }
    
    private void finalizarVenta(ActionEvent e) {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "El carrito está vacío",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            conexion.setAutoCommit(false);
            conexion.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            // 1. Crear registro de venta
            String sqlVenta = "INSERT INTO ventas (fecha, total, estado) VALUES (?, ?, ?)";
            PreparedStatement psVenta = conexion.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            
            double totalVenta = carrito.stream().mapToDouble(ProductoCarrito::getSubtotal).sum();
            
            psVenta.setTimestamp(1, new Timestamp(new Date().getTime()));
            psVenta.setDouble(2, totalVenta);
            psVenta.setString(3, "COMPLETADA");
            psVenta.executeUpdate();
            
            // Obtener ID de la venta recién insertada
            int ventaId = 0;
            ResultSet rs = psVenta.getGeneratedKeys();
            if (rs.next()) {
                ventaId = rs.getInt(1);
            }
            
            // 2. Procesar cada item del carrito
            for (ProductoCarrito item : carrito) {
                // Insertar detalle de venta
                String sqlDetalle = "INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario) " +
                                   "VALUES (?, ?, ?, ?)";
                PreparedStatement psDetalle = conexion.prepareStatement(sqlDetalle);
                psDetalle.setInt(1, ventaId);
                psDetalle.setInt(2, item.id);
                psDetalle.setInt(3, item.cantidad);
                psDetalle.setDouble(4, item.precio);
                psDetalle.executeUpdate();
                
                // Actualizar stock
                String sqlUpdateStock = "UPDATE productos SET stock = stock - ? WHERE id = ?";
                PreparedStatement psStock = conexion.prepareStatement(sqlUpdateStock);
                psStock.setInt(1, item.cantidad);
                psStock.setInt(2, item.id);
                psStock.executeUpdate();
                
                // Registrar transacción
                String sqlTransaccion = "INSERT INTO transacciones " +
                    "(tipo, producto_id, referencia_id, monto, cantidad_anterior, cantidad_nueva, fecha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";
                PreparedStatement psTrans = conexion.prepareStatement(sqlTransaccion);
                psTrans.setString(1, "VENTA");
                psTrans.setInt(2, item.id);
                psTrans.setInt(3, ventaId);
                psTrans.setDouble(4, item.getSubtotal());
                psTrans.setInt(5, item.stockDisponible);
                psTrans.setInt(6, item.stockDisponible - item.cantidad);
                psTrans.executeUpdate();
            }
            
            conexion.commit();
            
            // Mostrar ticket/resumen
            mostrarTicket(ventaId, totalVenta);
            
            // Limpiar carrito
            carrito.clear();
            actualizarCarrito();
            cargarProductos("");
            
        } catch (SQLException ex) {
            try {
                conexion.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error al procesar la venta: " + ex.getMessage() + "\nSe ha realizado rollback.",
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
    
private void mostrarTicket(int ventaId, double total) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuilder ticket = new StringBuilder();
        
        ticket.append("FARMACIA XYZ\n");
        ticket.append("Ticket #").append(ventaId).append("\n");
        ticket.append("Fecha: ").append(sdf.format(new Date())).append("\n");
        ticket.append("--------------------------------\n");
        
        for (ProductoCarrito item : carrito) {
            ticket.append(String.format("%-20s %5d x %6s\n", 
                item.nombre.substring(0, Math.min(item.nombre.length(), 20)), 
                item.cantidad, 
                df.format(item.precio)));
        }
        
        ticket.append("--------------------------------\n");
        ticket.append(String.format("TOTAL: $%15s\n", df.format(total)));
        ticket.append("\nGracias por su compra!");
        
        JTextArea txtTicket = new JTextArea(ticket.toString());
        txtTicket.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(txtTicket), "Ticket de Venta", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cancelarVenta(ActionEvent e) {
        if (carrito.isEmpty()) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea cancelar la venta actual?",
            "Confirmar cancelación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            carrito.clear();
            actualizarCarrito();
        }
    }
}