package kocaeli.ulasim;

import java.util.List;

public class RotaHesaplayici {

    public static double hesaplaRotaUcreti(List<Durak> rota, double aktarimIndirimi) {
        double toplamUcret = 0.0;
        if (rota != null && rota.size() > 1) {
            for (int i = 0; i < rota.size() - 1; i++) {
                Durak current = rota.get(i);
                if (current.getNextStops() != null && !current.getNextStops().isEmpty()) {
                    // Varsayılan olarak ilk bağlantının ücreti kullanılıyor
                    NextStop ns = current.getNextStops().get(0);
                    toplamUcret += ns.getUcret();
                }
                if (current.getTransfer() != null) {
                    toplamUcret += current.getTransfer().getTransferUcret();
                }
            }
        }
        // Aktarım indirimi uygulanıyor
        toplamUcret *= (1 - aktarimIndirimi);
        return toplamUcret;
    }

    public static double hesaplaRotaSuresi(List<Durak> rota) {
        double toplamSure = 0.0;
        if (rota != null && rota.size() > 1) {
            for (int i = 0; i < rota.size() - 1; i++) {
                Durak current = rota.get(i);
                if (current.getNextStops() != null && !current.getNextStops().isEmpty()) {
                    toplamSure += current.getNextStops().get(0).getSure();
                }
                if (current.getTransfer() != null) {
                    toplamSure += current.getTransfer().getTransferSure();
                }
            }
        }
        return toplamSure;
    }

    public static double hesaplaToplamMesafe(List<Durak> rota) {
        double toplamMesafe = 0.0;
        if (rota != null && rota.size() > 1) {
            for (int i = 0; i < rota.size() - 1; i++) {
                Durak current = rota.get(i);
                if (current.getNextStops() != null && !current.getNextStops().isEmpty()) {
                    toplamMesafe += current.getNextStops().get(0).getMesafe();
                }
            }
        }
        return toplamMesafe;
    }

    public static double hesaplaTaksiUcreti(Konum k1, Konum k2, Taksi taksiParametreleri) {
        // Haversine mesafe hesaplaması kullanılarak daha gerçekçi sonuç elde edilir.
        double mesafe = haversineDistance(k1.getEnlem(), k1.getBoylam(), k2.getEnlem(), k2.getBoylam());
        double ucret = taksiParametreleri.getOpeningFee() + (mesafe * taksiParametreleri.getCostPerKm());
        return ucret;
    }

    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Dünya yarıçapı (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}