package logistec.ui;

import logistec.algorithms.Prim;
import logistec.graph.*;
import logistec.util.LinkedList;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Swing panel that draws the city graph, marks the depot, delivery points,
 * MST edges, and each truck's route in a distinct color.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class GraphPanel extends JPanel {

    private static final int   VERTEX_RADIUS  = 14;
    private static final Color DEPOT_COLOR    = new Color(220, 50,  50);
    private static final Color DELIVERY_COLOR = new Color(50,  130, 220);
    private static final Color INTER_COLOR    = new Color(120, 120, 120);
    private static final Color EDGE_COLOR     = new Color(200, 200, 200);
    private static final Color MST_COLOR      = new Color(80,  200, 120);
    private static final Color[] TRUCK_COLORS = {
        new Color(255, 165,   0),   // orange
        new Color(148,   0, 211),   // purple
        new Color(0,   180, 180),   // cyan
        new Color(255, 215,   0),   // gold
        new Color(255,  20, 147),   // deep pink
    };

    private Graph              graph;
    private LinkedList<Truck>  trucks;
    private Prim               prim;
    private int                padding = 40;

    public GraphPanel() {
        setBackground(new Color(30, 30, 40));
        setPreferredSize(new Dimension(900, 650));
    }

    public void setData(Graph graph, LinkedList<Truck> trucks, Prim prim) {
        this.graph  = graph;
        this.trucks = trucks;
        this.prim   = prim;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (graph == null) {
            g0.setColor(Color.GRAY);
            g0.drawString("Cargue un archivo JSON para visualizar el grafo.", 50, 50);
            return;
        }
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] xs = new int[graph.numVertices()];
        int[] ys = new int[graph.numVertices()];
        computeCoords(xs, ys);

        // 1. Draw base edges
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(EDGE_COLOR);
        for (int u = 0; u < graph.numVertices(); u++) {
            for (Graph.AdjEntry e : graph.adj(u)) {
                if (e.target > u) { // avoid duplicates
                    g.drawLine(xs[u], ys[u], xs[e.target], ys[e.target]);
                }
            }
        }

        // 2. Draw MST edges
        if (prim != null) {
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(MST_COLOR);
            for (Edge e : prim.getMSTEdges()) {
                g.drawLine(xs[e.getU()], ys[e.getU()], xs[e.getV()], ys[e.getV()]);
            }
        }

        // 3. Draw truck routes
        if (trucks != null) {
            int colorIdx = 0;
            for (Truck t : trucks) {
                if (t.getRoute().isEmpty()) { colorIdx++; continue; }
                Color c = TRUCK_COLORS[colorIdx % TRUCK_COLORS.length];
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{8, 5}, 0));
                g.setColor(c);
                LinkedList<Integer> route = t.getRoute();
                int[] rv = toArray(route);
                for (int i = 0; i < rv.length - 1; i++) {
                    drawArrow(g, xs[rv[i]], ys[rv[i]], xs[rv[i+1]], ys[rv[i+1]]);
                }
                colorIdx++;
            }
        }

        // 4. Draw vertices
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < graph.numVertices(); i++) {
            Vertex v = graph.getVertex(i);
            if (v == null) continue;
            Color fill = v.isDepot() ? DEPOT_COLOR : v.isDelivery() ? DELIVERY_COLOR : INTER_COLOR;
            g.setColor(fill);
            g.fillOval(xs[i] - VERTEX_RADIUS, ys[i] - VERTEX_RADIUS, VERTEX_RADIUS * 2, VERTEX_RADIUS * 2);
            g.setColor(Color.WHITE);
            g.drawOval(xs[i] - VERTEX_RADIUS, ys[i] - VERTEX_RADIUS, VERTEX_RADIUS * 2, VERTEX_RADIUS * 2);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(v.getId());
            g.drawString(v.getId(), xs[i] - tw / 2, ys[i] + fm.getAscent() / 2 - 1);
        }

        // 5. Legend
        drawLegend(g);
    }

    private void computeCoords(int[] xs, int[] ys) {
        int W = getWidth()  - 2 * padding;
        int H = getHeight() - 2 * padding;
        // Find bounding box of vertex positions
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int i = 0; i < graph.numVertices(); i++) {
            Vertex v = graph.getVertex(i);
            if (v == null) continue;
            minX = Math.min(minX, v.getX()); maxX = Math.max(maxX, v.getX());
            minY = Math.min(minY, v.getY()); maxY = Math.max(maxY, v.getY());
        }
        int rangeX = Math.max(1, maxX - minX);
        int rangeY = Math.max(1, maxY - minY);
        for (int i = 0; i < graph.numVertices(); i++) {
            Vertex v = graph.getVertex(i);
            if (v == null) { xs[i] = padding; ys[i] = padding; continue; }
            xs[i] = padding + (int) ((double)(v.getX() - minX) / rangeX * W);
            ys[i] = padding + (int) ((double)(v.getY() - minY) / rangeY * H);
        }
    }

    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int   len    = 8;
        int   mx     = (x1 + x2) / 2;
        int   my     = (y1 + y2) / 2;
        g.fillPolygon(
            new int[]{ mx, mx - (int)(len * Math.cos(angle - 0.4)), mx - (int)(len * Math.cos(angle + 0.4)) },
            new int[]{ my, my - (int)(len * Math.sin(angle - 0.4)), my - (int)(len * Math.sin(angle + 0.4)) }, 3
        );
    }

    private void drawLegend(Graphics2D g) {
        int lx = 12, ly = getHeight() - 120;
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString("Leyenda", lx, ly);
        ly += 16;
        drawDot(g, lx, ly, DEPOT_COLOR);    g.setColor(Color.WHITE); g.drawString(" Depósito",      lx + 14, ly + 4); ly += 15;
        drawDot(g, lx, ly, DELIVERY_COLOR); g.setColor(Color.WHITE); g.drawString(" Entrega",        lx + 14, ly + 4); ly += 15;
        drawDot(g, lx, ly, INTER_COLOR);    g.setColor(Color.WHITE); g.drawString(" Intersección",   lx + 14, ly + 4); ly += 15;
        drawDot(g, lx, ly, MST_COLOR);      g.setColor(Color.WHITE); g.drawString(" MST",             lx + 14, ly + 4); ly += 15;
        if (trucks != null) {
            int ci = 0;
            for (Truck t : trucks) {
                if (!t.getRoute().isEmpty()) {
                    drawDot(g, lx, ly, TRUCK_COLORS[ci % TRUCK_COLORS.length]);
                    g.setColor(Color.WHITE);
                    g.drawString(" Ruta " + t.getId(), lx + 14, ly + 4);
                    ly += 15;
                }
                ci++;
            }
        }
    }

    private void drawDot(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.fillOval(x, y - 6, 10, 10);
    }

    private int[] toArray(LinkedList<Integer> list) {
        int[] arr = new int[list.size()];
        int i = 0;
        for (int v : list) arr[i++] = v;
        return arr;
    }
}
