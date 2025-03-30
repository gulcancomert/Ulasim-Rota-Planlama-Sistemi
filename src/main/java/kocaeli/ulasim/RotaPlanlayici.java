package kocaeli.ulasim;

import java.util.ArrayList;
import java.util.List;

public class RotaPlanlayici {

    // Rota metriklerini tutan yardımcı sınıf
    public static class RotaMetrics {
        public double toplamUcret;
        public double toplamSure;
        public double toplamMesafe;
    }

    /**
     * Tüm rotaları DFS ile bulur (başlangıçID -> hedefID).
     * Mevcut CustomRotaPlanlayici'nizde de benzer bir mantık var;
     * isterseniz burayı kullanmaya gerek kalmayabilir.
     */
    public static List<List<Durak>> tumRotalariHesapla(Graph graph, String baslangicId, String hedefId) {
        List<List<Durak>> rotalar = new ArrayList<>();
        List<Durak> currentRoute = new ArrayList<>();
        dfs(graph, baslangicId, hedefId, currentRoute, rotalar);
        return rotalar;
    }

    private static void dfs(Graph graph, String currentId, String hedefId,
                            List<Durak> currentRoute, List<List<Durak>> rotalar) {
        Durak current = null;
        for (Durak d : graph.getDurakListesi()) {
            if (d.getId().equals(currentId)) {
                current = d;
                break;
            }
        }
        if (current == null) return;

        // Aynı durağa tekrar giriliyorsa (döngü engelle)
        if (currentRoute.contains(current)) return;

        // Geçerli durağı ekle
        currentRoute.add(current);

        // Hedefe ulaştıysak
        if (currentId.equals(hedefId)) {
            rotalar.add(new ArrayList<>(currentRoute));
        } else {
            // Normal NextStop
            if (current.getNextStops() != null) {
                for (NextStop ns : current.getNextStops()) {
                    dfs(graph, ns.getStopId(), hedefId, currentRoute, rotalar);
                }
            }
            // Transfer (bus->tram vb.)
            if (current.getTransfer() != null) {
                dfs(graph, current.getTransfer().getTransferStopId(), hedefId, currentRoute, rotalar);
            }
        }

        // Geri adım
        currentRoute.remove(currentRoute.size() - 1);
    }

    /**
     * Verilen rota (Durak listesi) için:
     *  - Toplam ücret
     *  - Toplam süre
     *  - Toplam mesafe
     * değerlerini hesaplar.
     */
    public static RotaMetrics hesaplaRotaMetrics(List<Durak> rota) {
        RotaMetrics metrics = new RotaMetrics();

        for (int i = 0; i < rota.size() - 1; i++) {
            Durak current = rota.get(i);
            Durak next   = rota.get(i + 1);

            boolean edgeBulundu = false;

            // 1) nextStops içinde arayalım
            if (current.getNextStops() != null) {
                for (NextStop ns : current.getNextStops()) {
                    if (ns.getStopId().equals(next.getId())) {
                        metrics.toplamUcret  += ns.getUcret();
                        metrics.toplamSure   += ns.getSure();
                        metrics.toplamMesafe += ns.getMesafe(); // JSON'daki mesafe
                        edgeBulundu = true;
                        break;
                    }
                }
            }

            // 2) Transfer kontrol edelim (bus->tram vs.)
            if (!edgeBulundu && current.getTransfer() != null
                    && current.getTransfer().getTransferStopId().equals(next.getId())) {
                metrics.toplamUcret  += current.getTransfer().getTransferUcret();
                metrics.toplamSure   += current.getTransfer().getTransferSure();
                // eğer transfer mesafesi JSON'da yoksa 0 eklenebilir
                edgeBulundu = true;
            }

            // 3) Eğer hala 'edgeBulundu' false ise => YÜRÜYEREK
            if (!edgeBulundu) {
                double walkingDist = haversineDistance(
                        current.getLat(), current.getLon(),
                        next.getLat(),    next.getLon()
                );
                // Yürüme => ücret = 0, süre isterseniz yaklaşıksal ekleyin
                metrics.toplamMesafe += walkingDist;
            }
        }

        return metrics;
    }

    // Haversine hesaplama
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
