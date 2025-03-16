package kocaeli.ulasim;

import java.util.List;

public class TextArayuz {
    // Mevcut metot (orijinal detaylar)
    public static void gosterRota(List<Durak> rota) {
        System.out.println("=== Rota Detayları (Metin Tabanlı) ===");
        for (Durak d : rota) {
            System.out.println("Durak: " + d.getName());
            System.out.println("   Tip: " + d.getType());
            System.out.println("   Konum: (" + d.getLat() + ", " + d.getLon() + ")");
            System.out.println("   Son Durak: " + d.isSonDurak());
            if (d.getNextStops() != null && !d.getNextStops().isEmpty()) {
                System.out.println("   Next Stops:");
                for (NextStop ns : d.getNextStops()) {
                    System.out.println("      " + ns.getStopId() + " - Mesafe: " + ns.getMesafe() +
                            ", Süre: " + ns.getSure() + ", Ücret: " + ns.getUcret());
                }
            }
            if (d.getTransfer() != null) {
                Transfer t = d.getTransfer();
                System.out.println("   Transfer: " + t.getTransferStopId() + " - Süre: " +
                        t.getTransferSure() + ", Ücret: " + t.getTransferUcret());
            }
            System.out.println();
        }
    }

    // Yeni eklenen detaylı rota çıktısını gösteren metot
    public static void gosterRota(List<Durak> rota, double aktarimIndirimi, Taksi taxi, Konum kullaniciKonum, Konum hedefKonum) {
        // Önce, mevcut rota detaylarını yazdırıyoruz.
        gosterRota(rota);
        // Ardından hesaplamaları yapıp detaylı özet bilgileri ekrana yazdırıyoruz.
        double toplamUcret = RotaHesaplayici.hesaplaRotaUcreti(rota, aktarimIndirimi);
        double toplamSure = RotaHesaplayici.hesaplaRotaSuresi(rota);
        double toplamMesafe = RotaHesaplayici.hesaplaToplamMesafe(rota);

        System.out.println("=== Toplam ===");
        System.out.println("Ücret: " + toplamUcret + " TL");
        System.out.println("Süre: " + toplamSure + " dk");
        System.out.println("Mesafe: " + toplamMesafe + " km");
        System.out.println("=== Alternatif Rotalar ===");
        System.out.println("🚖 Sadece Taksi (Daha hızlı, ancak maliyetli)");
        System.out.println("🚍 Sadece Otobüs (Daha uygun maliyetli, ancak daha uzun sürebilir)");
        System.out.println("🚋 Tramvay Öncelikli (Rahat ve dengeli bir ulaşım seçeneği)");
        System.out.println("🛑 En Az Aktarmalı Rota (Daha az bekleme süresi)");
    }
}