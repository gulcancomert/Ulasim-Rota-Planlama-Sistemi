package kocaeli.ulasim;

import java.util.ArrayList;
import java.util.List;

public class RotaPlanlayici {

    // Belirtilen başlangıç ve hedef duraklar arasında tüm rota alternatiflerini hesaplar.
    public static List<List<Durak>> tumRotalariHesapla(Graph graph, String baslangicId, String hedefId) {
        List<List<Durak>> rotalar = new ArrayList<>();
        List<Durak> currentRoute = new ArrayList<>();
        dfs(graph, baslangicId, hedefId, currentRoute, rotalar);
        return rotalar;
    }

    private static void dfs(Graph graph, String currentId, String hedefId, List<Durak> currentRoute, List<List<Durak>> rotalar) {
        Durak current = null;
        for (Durak d : graph.getDurakListesi()) {
            if (d.getId().equals(currentId)) {
                current = d;
                break;
            }
        }
        if (current == null) return;
        // Döngüye girmemek için (aynı durak birden geçmesin)
        if (currentRoute.contains(current)) return;
        currentRoute.add(current);
        if (currentId.equals(hedefId)) {
            rotalar.add(new ArrayList<>(currentRoute));
        } else {
            // Mevcut duraktaki nextStops üzerinden DFS
            if (current.getNextStops() != null) {
                for (NextStop ns : current.getNextStops()) {
                    dfs(graph, ns.getStopId(), hedefId, currentRoute, rotalar);
                }
            }
            // Transfer varsa, onu da ekle
            if (current.getTransfer() != null) {
                dfs(graph, current.getTransfer().getTransferStopId(), hedefId, currentRoute, rotalar);
            }
        }
        currentRoute.remove(currentRoute.size() - 1);
    }

    // Bir rota üzerindeki toplam ücret, süre ve mesafeyi hesaplayan yardımcı sınıf
    public static class RotaMetrics {
        public double toplamUcret;
        public double toplamSure;
        public double toplamMesafe;
    }

    // Verilen rota (Durak listesi) için, ardışık duraklar arasındaki bilgileri kullanarak rota metriklerini hesaplar.
    public static RotaMetrics hesaplaRotaMetrics(List<Durak> rota) {
        RotaMetrics metrics = new RotaMetrics();
        metrics.toplamUcret = 0.0;
        metrics.toplamSure = 0.0;
        metrics.toplamMesafe = 0.0;
        for (int i = 0; i < rota.size() - 1; i++) {
            Durak current = rota.get(i);
            Durak next = rota.get(i + 1);
            boolean edgeBulundu = false;
            if (current.getNextStops() != null) {
                for (NextStop ns : current.getNextStops()) {
                    if (ns.getStopId().equals(next.getId())) {
                        metrics.toplamUcret += ns.getUcret();
                        metrics.toplamSure += ns.getSure();
                        metrics.toplamMesafe += ns.getMesafe();
                        edgeBulundu = true;
                        break;
                    }
                }
            }
            // Eğer nextStops içinde bulunamadıysa, transfer kontrolü
            if (!edgeBulundu && current.getTransfer() != null && current.getTransfer().getTransferStopId().equals(next.getId())) {
                metrics.toplamUcret += current.getTransfer().getTransferUcret();
                metrics.toplamSure += current.getTransfer().getTransferSure();
                // Transferde mesafe bilgisi olmayabilir, burada eklenmeyebilir.
            }
        }
        return metrics;
    }
}