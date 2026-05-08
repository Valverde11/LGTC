package io;

import algorithms.*;
import graph.*;
import util.LinkedList;


public class ReportGenerator {

    private final Graph         graph;
    private final LinkedList<Parcel> packages;
    private final LinkedList<Truck>   trucks;
    private final FloydWarshall       fw;
    private final Warshall            warshall;
    private final Prim                prim;
    private final Kruskal             kruskal;

    public ReportGenerator(Graph graph,
                           LinkedList<Parcel> packages,
                           LinkedList<Truck>   trucks,
                           FloydWarshall       fw,
                           Warshall            warshall,
                           Prim                prim,
                           Kruskal             kruskal) {
        this.graph    = graph;
        this.packages = packages;
        this.trucks   = trucks;
        this.fw       = fw;
        this.warshall = warshall;
        this.prim     = prim;
        this.kruskal  = kruskal;
    }

    /** Build and return the full report string. */
    public String generate() {
        StringBuilder sb = new StringBuilder();
        line(sb, "═", 70);
        sb.append("  LogísTEC — Reporte Final\n");
        line(sb, "═", 70);

        // ── Graph summary ─────────────────────────────────────────────────
        sb.append("\n▶ GRAFO DE LA CIUDAD\n");
        sb.append("  Vértices : ").append(graph.numVertices()).append("\n");
        sb.append("  Aristas  : ").append(graph.numEdges()).append("\n");
        sb.append("  Depósito : ").append(graph.getVertex(graph.getDepotIndex()).getId()).append("\n");

        // ── Warshall ──────────────────────────────────────────────────────
        sb.append("\n▶ CIERRE TRANSITIVO (Warshall)\n");
        sb.append(warshall.matrixToString(graph));

        // ── Packages ──────────────────────────────────────────────────────
        sb.append("\n▶ PAQUETES\n");
        for (Parcel p : packages) {
            String dest = graph.getVertex(p.getDestinationIndex()).getId();
            sb.append(String.format("  %-6s → %-4s  %3dkg  P%d  [%s]\n",
                    p.getId(), dest, p.getWeight(), p.getPriority(), p.getStatus()));
        }

        // ── MST comparison ────────────────────────────────────────────────
        sb.append("\n▶ ÁRBOL DE EXPANSIÓN MÍNIMA\n");
        sb.append("  Prim   : ").append(prim.getTotalWeight()).append("m  |  ")
          .append(String.format("%.3f", prim.getElapsedMs())).append(" ms\n");
        sb.append("  Kruskal: ").append(kruskal.getTotalWeight()).append("m  |  ")
          .append(String.format("%.3f", kruskal.getElapsedMs())).append(" ms\n");
        if (prim.getTotalWeight() == kruskal.getTotalWeight()) {
            sb.append("  ✓ Ambos algoritmos producen el mismo costo total.\n");
        } else {
            sb.append("  ✗ ADVERTENCIA: costos distintos (posible error).\n");
        }

        sb.append("\n  Aristas MST (Prim):\n");
        for (Edge e : prim.getMSTEdges()) {
            String u = graph.getVertex(e.getU()) != null ? graph.getVertex(e.getU()).getId() : String.valueOf(e.getU());
            String v = graph.getVertex(e.getV()) != null ? graph.getVertex(e.getV()).getId() : String.valueOf(e.getV());
            sb.append("    ").append(u).append(" -- ").append(v).append("  [").append(e.getWeight()).append("m]\n");
        }

        // ── Floyd-Warshall matrix ─────────────────────────────────────────
        sb.append("\n▶ MATRIZ DE DISTANCIAS MÍNIMAS (Floyd-Warshall)\n");
        sb.append(fw.matrixToString(graph));

        // ── Truck routes ──────────────────────────────────────────────────
        sb.append("\n▶ RUTAS DE CAMIONES\n");
        for (Truck t : trucks) {
            line(sb, "─", 60);
            sb.append("  Camión : ").append(t.getId()).append("\n");
            sb.append("  Carga  : ").append(t.getCurrentLoad()).append("kg / ")
              .append(t.getCapacity()).append("kg  (")
              .append(String.format("%.1f", t.occupancyPercent())).append("%)\n");

            if (t.getPackages().isEmpty()) {
                sb.append("  Sin paquetes asignados.\n");
                continue;
            }

            sb.append("  Paquetes: ");
            for (Parcel p : t.getPackages()) sb.append(p.getId()).append(" ");
            sb.append("\n");

            sb.append("  Dist NN : ").append(t.getRouteDistanceNN()).append("m\n");
            sb.append("  Dist MST: ").append(t.getRouteDistanceMST()).append("m\n");

            int best = Math.min(t.getRouteDistanceNN(), t.getRouteDistanceMST());
            String winner = t.getRouteDistanceMST() <= t.getRouteDistanceNN() ? "MST-based" : "Nearest Neighbor";
            sb.append("  Heurística ganadora: ").append(winner).append("\n");

            if (t.getRouteDistanceNN() > 0) {
                double saving = 100.0 * (t.getRouteDistanceNN() - t.getRouteDistanceMST()) / t.getRouteDistanceNN();
                sb.append("  Ahorro MST vs NN: ").append(String.format("%.1f", saving)).append("%\n");
            }

            sb.append("  Ruta: ");
            LinkedList<Integer> route = t.getRoute();
            boolean first = true;
            for (int v : route) {
                if (!first) sb.append(" → ");
                String id = graph.getVertex(v) != null ? graph.getVertex(v).getId() : String.valueOf(v);
                sb.append(id);
                first = false;
            }
            sb.append("  (").append(best).append("m)\n");
        }

        // ── Rejected packages ─────────────────────────────────────────────
        sb.append("\n▶ PAQUETES RECHAZADOS\n");
        boolean any = false;
        for (Parcel p : packages) {
            if (p.getStatus() == Parcel.Status.REJECTED) {
                sb.append("  ").append(p).append("\n");
                any = true;
            }
        }
        if (!any) sb.append("  Ninguno.\n");

        line(sb, "═", 70);
        return sb.toString();
    }

    private void line(StringBuilder sb, String ch, int n) {
        sb.append(ch.repeat(n)).append("\n");
    }
}
