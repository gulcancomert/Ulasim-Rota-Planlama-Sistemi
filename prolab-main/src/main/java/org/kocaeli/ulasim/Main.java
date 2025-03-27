package org.kocaeli.ulasim;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String jsonDosyaYolu = "C:\\Users\\HP\\OneDrive\\Masaüstü\\prolab-main\\prolab-main\\src\\main\\java\\org\\kocaeli\\ulasim\\jsonveri.txt";
        SehirVerisi sehirVerisi = JSONVeriYukleyici.verileriYukle(jsonDosyaYolu);

        if (sehirVerisi != null) {
            System.out.println("Şehir: " + sehirVerisi.getCity());
            System.out.println("Taksi Açılış Ücreti: " + sehirVerisi.getTaxi().getOpeningFee());
            System.out.println("Toplam Durak Sayısı: " + sehirVerisi.getDuraklar().size());

            Graph graph = new Graph(sehirVerisi.getDuraklar());
            graph.baglantiOlustur();

            // Örnek: Kullanıcı tarafından belirlenen konumlar (enlem, boylam)
            Konum kullaniciKonum = new Konum(40.7769, 29.9780);
            Konum hedefKonum = new Konum(40.7831, 29.9326);

            Durak baslangicDurak = graph.enYakinDurakBul(kullaniciKonum);
            Durak hedefDurak = graph.enYakinDurakBul(hedefKonum);

            System.out.println("Kullanıcıya en yakın durak: " + baslangicDurak);
            System.out.println("Hedefe en yakın durak: " + hedefDurak);

            CustomRotaPlanlayici customRotaPlanlayici = new CustomRotaPlanlayici(graph, new HaversineDistanceCalculator());
            List<List<Durak>> alternatifRotalar = customRotaPlanlayici.calculateRoutes(baslangicDurak.getId(), hedefDurak.getId());

            if (alternatifRotalar.isEmpty()) {
                System.out.println("Hiç rota bulunamadı, 'Sadece Taksi' fallback rotası oluşturuluyor...");
                List<Durak> fallbackRota = new ArrayList<>();
                fallbackRota.add(baslangicDurak);
                fallbackRota.add(hedefDurak);
                alternatifRotalar.add(fallbackRota);
            }

            System.out.println("\n=== Tüm Alternatif Rotalar ===");
            double bestCost = Double.MAX_VALUE;
            int bestIndex = -1;
            List<Durak> enIyiRota = null;

            for (int i = 0; i < alternatifRotalar.size(); i++) {
                List<Durak> rota = alternatifRotalar.get(i);
                double rotaUcreti = hesaplaRotaUcreti(rota);
                System.out.println("Rota " + (i + 1) + ":");
                for (Durak d : rota) {
                    System.out.print(d.getName() + " -> ");
                }
                System.out.println("Bitiş");
                System.out.println("  Ücret: " + rotaUcreti + " TL\n");

                if (rotaUcreti < bestCost) {
                    bestCost = rotaUcreti;
                    bestIndex = i;
                }
            }

            System.out.println("=== En Uygun Rota ===");
            if (bestIndex != -1) {
                enIyiRota = alternatifRotalar.get(bestIndex);
                System.out.println("En Uygun Rota (Rota " + (bestIndex + 1) + "):");
                for (Durak d : enIyiRota) {
                    System.out.print(d.getName() + " -> ");
                }
                System.out.println("Bitiş");
                System.out.println("  Ücret: " + bestCost + " TL");
            } else {
                System.out.println("Uygun rota bulunamadı!");
            }

            // Kullanıcıya arayüz seçimi sormak yerine, direkt JavaFX harita arayüzünü açalım:
            // (Harita arayüzü, DemoUygulamasi sınıfında tanımlı ve start(Stage) metodu ile çalışır.)
            javafx.application.Application.launch(DemoUygulamasi.class);

            // Eğer computed rota bilgilerini haritada göstermek isterseniz,
            // DemoUygulamasi içinde bu bilgileri tutacak statik değişkenler ayarlayabilirsiniz.
        } else {
            System.out.println("JSON verisi yüklenirken hata oluştu.");
        }
    }

    public static double hesaplaRotaUcreti(List<Durak> rota) {
        double toplamUcret = 0.0;
        if (rota != null && rota.size() > 1) {
            // Basit örnek: her adım için sabit bir ücret ekleyelim
            toplamUcret = (rota.size() - 1) * 3.0;
        }
        return toplamUcret;
    }
}
