package com.farmacia.gui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private Connection conexion;
    
    // Paneles
    private ProductosPanel pnlProductos;
    private VentasPanel pnlVentas;
    private TransaccionesPanel pnlTransacciones;
    private InventarioPanel pnlInventario;
    
    public MainFrame() {
        initComponents();
        setupDatabase();
        setupFrame();
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
    }
    
    private void setupDatabase() {
    try {
        Class.forName("com.mysql.jdbc.Driver");
        conexion = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/localdb", 
            "root", 
            "");
            
        // Crear instancias de los paneles
        pnlProductos = new ProductosPanel(conexion);
        pnlVentas = new VentasPanel(conexion);
        pnlTransacciones = new TransaccionesPanel(); // Cambio aquí
        pnlTransacciones.setConnection(conexion); // Método setter para la conexión
        pnlInventario = new InventarioPanel(conexion);
        
        // Añadir pestañas
        tabbedPane.addTab("Productos", pnlProductos);
        tabbedPane.addTab("Ventas", pnlVentas);
        tabbedPane.addTab("Transacciones", pnlTransacciones);
        tabbedPane.addTab("Inventario", pnlInventario);
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, 
            "Error al conectar con la base de datos: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
    
    private void setupFrame() {
        setTitle("Sistema de Gestión de Farmacia - NetBeans");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        // Configurar menú superior
        setupMenuBar();
        
        // Panel principal
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de estado inferior
        add(createStatusPanel(), BorderLayout.SOUTH);
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menú Archivo
        JMenu mnArchivo = new JMenu("Archivo");
        
        JMenuItem mntmBackup = new JMenuItem("Respaldar BD");
        mntmBackup.addActionListener(e -> backupDatabase());
        
        JMenuItem mntmSalir = new JMenuItem("Salir");
        mntmSalir.addActionListener(e -> System.exit(0));
        
        mnArchivo.add(mntmBackup);
        mnArchivo.addSeparator();
        mnArchivo.add(mntmSalir);
        
        // Menú Herramientas
        JMenu mnHerramientas = new JMenu("Herramientas");
        
        JMenuItem mntmCalculadora = new JMenuItem("Calculadora");
        mntmCalculadora.addActionListener(e -> abrirCalculadora());
        
        mnHerramientas.add(mntmCalculadora);
        
        // Menú Ayuda
        JMenu mnAyuda = new JMenu("Ayuda");
        
        JMenuItem mntmAcercaDe = new JMenuItem("Acerca de");
        mntmAcercaDe.addActionListener(e -> mostrarAcercaDe());
        
        mnAyuda.add(mntmAcercaDe);
        
        // Añadir menús a la barra
        menuBar.add(mnArchivo);
        menuBar.add(mnHerramientas);
        menuBar.add(mnAyuda);
        
        setJMenuBar(menuBar);
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel lblEstado = new JLabel(" Conectado a farmacia_db");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblUsuario = new JLabel("Usuario: Admin ", SwingConstants.RIGHT);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 12));
        
        statusPanel.add(lblEstado, BorderLayout.WEST);
        statusPanel.add(lblUsuario, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    private void backupDatabase() {
        JOptionPane.showMessageDialog(this, 
            "Función de respaldo no implementada aún",
            "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirCalculadora() {
        try {
            Runtime.getRuntime().exec("calc.exe");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "No se pudo abrir la calculadora",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarAcercaDe() {
        String mensaje = "Sistema de Gestión de Farmacia\n" +
                        "Versión 1.0\n\n" +
                        "Desarrollado en Java con MySQL\n" +
                        "© 2023 - Todos los derechos reservados";
        
        JOptionPane.showMessageDialog(this, 
            mensaje,
            "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void dispose() {
        // Cerrar la conexión a la base de datos al salir
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Establecer el look and feel del sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar la aplicación: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

class InventarioPanel extends JPanel {
    // Panel básico de inventario (para implementar)
    public InventarioPanel(Connection conexion) {
        setLayout(new BorderLayout());
        add(new JLabel("Módulo de Inventario - En desarrollo", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}