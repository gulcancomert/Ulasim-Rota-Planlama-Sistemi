package kocaeli.ulasim;

import java.util.List;
import java.util.Scanner;

import javafx.application.Application;

/**
 * Main Sınıfı
 * Kullanıcıdan seyahat bilgilerini alır ve harita üzerinden güzergahı gösterir.
 */
public class Main {

    public static void main(String[] args) {
        // JSON dosya yolunu sisteminize göre ayarlayın:
        String jsonDosyaYolu = "C:\\Users\\HP\\OneDrive\\Masaüstü\\Maven2\\demo\\src\\main\\java\\kocaeli\\ulasim\\jsonveri.txt";
        SehirVerisi sehirVerisi = JSONVeriYukleyici.verileriYukle(jsonDosyaYolu);

        if (sehirVerisi != null) {
            System.out.println("Şehir: " + sehirVerisi.getCity());
            System.out.println("Taksi Açılış Ücreti: " + sehirVerisi.getTaxi().getOpeningFee());
            System.out.println("Toplam Durak Sayısı: " + sehirVerisi.getDuraklar().size());

            // Graph ve rota hesaplamaları
            Graph graph = new Graph(sehirVerisi.getDuraklar());
            graph.baglantiOlustur();
            Konum kullaniciKonum = new Konum(40.7769, 29.9780);
            Konum hedefKonum = new Konum(40.7831, 29.9326);

            Durak baslangicDurak = graph.enYakinDurakBul(kullaniciKonum);
            Durak hedefDurak = graph.enYakinDurakBul(hedefKonum);

            System.out.println("Kullanıcıya en yakın durak: " + baslangicDurak);
            System.out.println("Hedefe en yakın durak: " + hedefDurak);

            Dijkstra dijkstra = new Dijkstra(graph);
            List<Durak> rota = dijkstra.kisaYolHesapla(baslangicDurak.getId(), hedefDurak.getId());

            double aktarimIndirimi = 0.1;
            double rotaUcreti = RotaHesaplayici.hesaplaRotaUcreti(rota, aktarimIndirimi);
            double rotaSuresi = RotaHesaplayici.hesaplaRotaSuresi(rota);

            System.out.println("\n=== Toplu Taşıma Rota Bilgileri ===");
            for (Durak d : rota) {
                System.out.print(d.getName() + " -> ");
            }
            System.out.println("Bitiş");
            System.out.println("Aktarım İndirimi: %" + (aktarimIndirimi * 100));
            System.out.println("Toplam Ücret: " + rotaUcreti + " TL");
            System.out.println("Toplam Süre: " + rotaSuresi + " dk");

            // Yolcu ve ödeme örneği
            Yolcu yolcu = new OgrenciYolcu("Mehmet Öğrenci");
            double sonUcret = rotaUcreti * (1 - yolcu.getIndirimOrani());
            System.out.println("\nYolcu İndirimli Ücret: " + sonUcret + " TL");

            Odeme odeme = new KentKartOdeme(20.0);
            odeme.odemeIsle(sonUcret);

            double taksiUcreti = RotaHesaplayici.hesaplaTaksiUcreti(kullaniciKonum, hedefKonum, sehirVerisi.getTaxi());
            System.out.println("\n=== Alternatif: Sadece Taksi ===");
            System.out.println("Taksi Ücreti: " + taksiUcreti + " TL");

            // Kullanıcıya hangi arayüzü kullanmak istediğini soralım:
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nHangi arayüzü kullanmak istersiniz? (1: Metin Tabanlı, 2: JavaFX Harita Tabanlı)");
            int secim = scanner.nextInt();
            if (secim == 1) {
                TextArayuz.gosterRota(rota);
            } else if (secim == 2) {
                // JavaFX arayüzü için DemoUygulamasi sınıfını çalıştırıyoruz.
                Application.launch(DemoUygulamasi.class);
            } else {
                System.out.println("Geçersiz seçim, metin tabanlı arayüz kullanılıyor.");
                TextArayuz.gosterRota(rota);
            }
        } else {
            System.out.println("JSON verisi yüklenirken hata oluştu.");
        }
    }
}

