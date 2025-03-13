package kocaeli.ulasim;

import java.util.List;

public class RotaHesaplayici {

    public static double hesaplaRotaUcreti(List<Durak> rota, double aktarimIndirimi) {
        double toplamUcret = 0.0;
        // Örnek hesap: her bağlantı için 2 TL alındığını varsayalım.
        if(rota != null && rota.size() > 1) {
            toplamUcret = (rota.size() - 1) * 2.0;
        }
        toplamUcret *= (1 - aktarimIndirimi);
        return toplamUcret;
    }

    public static double hesaplaRotaSuresi(List<Durak> rota) {
        double toplamSure = 0.0;
        // Örnek: her bağlantı için 5 dakika.
        if(rota != null && rota.size() > 1) {
            toplamSure = (rota.size() - 1) * 5.0;
        }
        return toplamSure;
    }

    public static double hesaplaTaksiUcreti(Konum k1, Konum k2, Taksi taksiParametreleri) {
        // Basit Euclidean mesafe; gerçek hesaplamada Haversine kullanılabilir.
        double mesafe = Math.sqrt(Math.pow(k1.getEnlem() - k2.getEnlem(), 2)
                + Math.pow(k1.getBoylam() - k2.getBoylam(), 2));
        double ucret = taksiParametreleri.getOpeningFee() + (mesafe * taksiParametreleri.getCostPerKm());
        return ucret;
    }
}