package org.kocaeli.ulasim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomRotaPlanlayici {

    private Graph graph;
    private DistanceCalculator distanceCalculator;

    public CustomRotaPlanlayici(Graph graph, DistanceCalculator distanceCalculator) {
        this.graph = graph;
        this.distanceCalculator = distanceCalculator;
    }

    /**
     * Belirtilen başlangıç ve bitiş durakları arasında DFS ile tüm rotaları bulur.
     * @param startId Başlangıç durak ID'si
     * @param endId Bitiş durak ID'si
     * @return Bulunan rota listeleri
     */
    public List<List<Durak>> calculateRoutes(String startId, String endId) {
        List<List<Durak>> routes = new ArrayList<>();
        Durak start = getDurakById(startId);
        Durak end = getDurakById(endId);
        if (start == null || end == null) {
            return routes;
        }
        List<Durak> currentRoute = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfs(start, end, currentRoute, routes, visited);
        return routes;
    }

    private void dfs(Durak current, Durak end, List<Durak> currentRoute, List<List<Durak>> routes, Set<String> visited) {
        visited.add(current.getId());
        currentRoute.add(current);

        if (current.getId().equals(end.getId())) {
            routes.add(new ArrayList<>(currentRoute));
        } else {
            List<Durak> neighbors = getNeighbors(current);
            for (Durak neighbor : neighbors) {
                if (!visited.contains(neighbor.getId())) {
                    dfs(neighbor, end, currentRoute, routes, visited);
                }
            }
        }
        currentRoute.remove(currentRoute.size() - 1);
        visited.remove(current.getId());
    }

    /**
     * Verilen durak için, nextStops ve varsa transfer bilgisine göre komşu durakları döndürür.
     */
    private List<Durak> getNeighbors(Durak d) {
        List<Durak> neighbors = new ArrayList<>();
        if (d.getNextStops() != null) {
            for (NextStop ns : d.getNextStops()) {
                Durak neighbor = getDurakById(ns.getStopId());
                if (neighbor != null) {
                    neighbors.add(neighbor);
                }
            }
        }
        if (d.getTransfer() != null) {
            Durak transferNeighbor = getDurakById(d.getTransfer().getTransferStopId());
            if (transferNeighbor != null) {
                neighbors.add(transferNeighbor);
            }
        }
        return neighbors;
    }

    private Durak getDurakById(String id) {
        for (Durak d : graph.getDurakListesi()) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }
}
