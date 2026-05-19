package ui;

import algorithms.*;
import graph.*;
import io.*;
import planner.Planner;
import util.LinkedList;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;

/**
 * Main Swing window — LogísTEC.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class MainWindow extends JFrame {

    private static final Color BG        = new Color(15, 19, 28);
    private static final Color PANEL_BG  = new Color(22, 28, 40);
    private static final Color BORDER_C  = new Color(40, 55, 75);
    private static final Color TEXT_C    = new Color(200, 215, 235);
    private static final Color MUTED_C   = new Color(100, 120, 145);

    // State
    private Graph               graph;
    private LinkedList<Parcel>  packages;
    private LinkedList<Truck>   trucks;
    private FloydWarshall       fw;
    private Warshall            warshall;
    private Prim                prim;
    private Kruskal             kruskal;

    // UI
    private final GraphPanel  graphPanel  = new GraphPanel();
    private final JTextArea   reportArea  = new JTextArea();
    private final JTextField  srcField    = new JTextField(6);
    private final JTextField  dstField    = new JTextField(6);
    private final JTextArea   pathArea    = new JTextArea(5, 50);
    private final JLabel      statusLabel = new JLabel("Sin caso cargado.");

    public MainWindow() {
        super("LogisTEC - Sistema de Distribucion Logistica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // Top bar
        root.add(buildTopBar(), BorderLayout.NORTH);

        // Center tabs
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(PANEL_BG);
        tabs.setForeground(TEXT_C);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        styleTabPane(tabs);

        tabs.addTab("  Grafo  ", graphPanel);
        tabs.addTab("  Reporte  ", buildReportPanel());
        tabs.addTab("  Camino minimo  ", buildPathPanel());

        root.add(tabs, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        statusBar.setBackground(new Color(10, 14, 22));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        statusLabel.setForeground(MUTED_C);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.add(statusLabel);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        bar.setBackground(new Color(10, 14, 22));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));

        JLabel logo = new JLabel("LogisTEC");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(new Color(52, 211, 153));
        bar.add(logo);

        bar.add(Box.createHorizontalStrut(10));

        JButton btnLoad = makeBtn("Cargar JSON",           new Color(37, 99, 235));
        JButton btnRun  = makeBtn("Ejecutar Planificacion", new Color(22, 163, 74));
        btnLoad.addActionListener(e -> loadFile());
        btnRun.addActionListener(e  -> runPlanning());
        bar.add(btnLoad);
        bar.add(btnRun);
        return bar;
    }

    private JPanel buildReportPanel() {
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setBackground(new Color(14, 18, 27));
        reportArea.setForeground(new Color(190, 210, 230));
        reportArea.setCaretColor(Color.WHITE);
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(14, 16, 14, 16));
        reportArea.setLineWrap(false);

        JScrollPane sp = new JScrollPane(reportArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        sp.getVerticalScrollBar().setBackground(PANEL_BG);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(sp);
        return p;
    }

    private JPanel buildPathPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setBackground(PANEL_BG);

        styleField(srcField); styleField(dstField);
        top.add(label("Origen:"));  top.add(srcField);
        top.add(label("Destino:")); top.add(dstField);

        JButton btn = makeBtn("Calcular (Dijkstra)", new Color(109, 40, 217));
        btn.addActionListener(e -> calcPath());
        top.add(btn);
        p.add(top, BorderLayout.NORTH);

        pathArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        pathArea.setBackground(new Color(14, 18, 27));
        pathArea.setForeground(new Color(190, 210, 230));
        pathArea.setEditable(false);
        pathArea.setMargin(new Insets(12, 14, 12, 14));

        JScrollPane sp = new JScrollPane(pathArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Actions ────────────────────────────────────────────────────────────────

    private void loadFile() {
        JFileChooser fc = new JFileChooser(".");
        fc.setDialogTitle("Seleccionar archivo JSON");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            CityLoader loader = new CityLoader();
            loader.load(fc.getSelectedFile().getAbsolutePath());
            graph    = loader.getGraph();
            packages = loader.getParcels();
            trucks   = loader.getTrucks();
            graphPanel.setData(graph, null, null);
            status("Caso cargado: " + fc.getSelectedFile().getName()
                   + "  (" + graph.numVertices() + "V, " + graph.numEdges() + "E, "
                   + packages.size() + " paquetes, " + trucks.size() + " camiones)");
        } catch (Exception ex) {
            error("Error al cargar: " + ex.getMessage());
        }
    }

    private void runPlanning() {
        if (graph == null) { error("Primero cargue un archivo JSON."); return; }
        try {
            status("Ejecutando algoritmos...");
            warshall = new Warshall(graph);
            fw       = new FloydWarshall(graph);
            prim     = new Prim(graph);
            kruskal  = new Kruskal(graph);

            Planner planner = new Planner(graph, fw, warshall);
            planner.validateReachability(packages);
            planner.assignPackages(packages, trucks);
            planner.planRoutes(trucks);

            ReportGenerator rg = new ReportGenerator(graph, packages, trucks, fw, warshall, prim, kruskal);
            reportArea.setText(rg.generate());
            reportArea.setCaretPosition(0);

            graphPanel.setData(graph, trucks, prim);
            status("Planificacion completada.  Prim=" + prim.getTotalWeight()
                   + "m  |  Kruskal=" + kruskal.getTotalWeight() + "m");
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error: " + ex.getMessage());
        }
    }

    private void calcPath() {
        if (graph == null || fw == null) { pathArea.setText("Primero ejecute la planificacion."); return; }
        int src = graph.indexOf(srcField.getText().trim());
        int dst = graph.indexOf(dstField.getText().trim());
        if (src == -1) { pathArea.setText("Vertice origen no encontrado."); return; }
        if (dst == -1) { pathArea.setText("Vertice destino no encontrado."); return; }
        Dijkstra d = new Dijkstra(graph, src);
        pathArea.setText(d.resultToString(graph, dst));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void styleTabPane(JTabbedPane t) {
        t.setBorder(BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPane.selected",     PANEL_BG);
        UIManager.put("TabbedPane.background",   BG);
        UIManager.put("TabbedPane.foreground",   TEXT_C);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(7, 18, 7, 18));
        return b;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_C);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(25, 32, 48));
        f.setForeground(TEXT_C);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            new EmptyBorder(4, 8, 4, 8)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private void status(String msg) { statusLabel.setText(msg); }
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
