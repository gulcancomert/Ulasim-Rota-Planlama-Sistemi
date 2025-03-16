package kocaeli.ulasim;

import java.util.List;
import java.util.Scanner;

public class Main {

    // Haversine mesafe hesaplama metodunu Main sınıfına ekliyoruz.
    public static double haversineDistance(Konum k1, Konum k2) {
        double R = 6371; // Dünya yarıçapı (km)
        double dLat = Math.toRadians(k2.getEnlem() - k1.getEnlem());
        double dLon = Math.toRadians(k2.getBoylam() - k1.getBoylam());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(k1.getEnlem())) * Math.cos(Math.toRadians(k2.getEnlem()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Kocaeli sınırları (örnek değerler)
    private static final double MIN_LAT = 40.70;
    private static final double MAX_LAT = 40.85;
    private static final double MIN_LON = 29.90;
    private static final double MAX_LON = 30.05;

    // Girilen koordinatların Kocaeli sınırları içinde olup olmadığını kontrol eden metod
    private static boolean isWithinKocaeli(double lat, double lon) {
        return (lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON);
    }

    public static void main(String[] args) {
        // JSON dosya yolunu sisteminize göre ayarlayın:
        String jsonDosyaYolu = "C:\\Users\\HP\\OneDrive\\Masaüstü\\Maven2\\demo\\src\\main\\java\\kocaeli\\ulasim\\jsonveri.json";
        SehirVerisi sehirVerisi = JSONVeriYukleyici.verileriYukle(jsonDosyaYolu);

        if (sehirVerisi != null) {
            System.out.println("Şehir: " + sehirVerisi.getCity());
            System.out.println("Taksi Açılış Ücreti: " + sehirVerisi.getTaxi().getOpeningFee());
            System.out.println("Toplam Durak Sayısı: " + sehirVerisi.getDuraklar().size());

            // Graph ve rota hesaplamaları
            Graph graph = new Graph(sehirVerisi.getDuraklar());
            graph.baglantiOlustur();

            // Örnek: Kullanıcı tarafından girilen veya belirlenen konumlar
            Konum kullaniciKonum = new Konum(40.7769, 29.9780);
            Konum hedefKonum = new Konum(40.7831, 29.9326);

            // Kocaeli sınırları kontrolü
            if (!isWithinKocaeli(kullaniciKonum.getEnlem(), kullaniciKonum.getBoylam())) {
                System.out.println("Kullanıcı konumu Kocaeli sınırları dışında!");
                return;
            }
            if (!isWithinKocaeli(hedefKonum.getEnlem(), hedefKonum.getBoylam())) {
                System.out.println("Hedef konum Kocaeli sınırları dışında!");
                return;
            }

            // Kullanıcının konumundan en yakın durak ve hedef konumuna en yakın durak tespiti
            Durak baslangicDurak = graph.enYakinDurakBul(kullaniciKonum);
            Durak hedefDurak = graph.enYakinDurakBul(hedefKonum);

            System.out.println("Kullanıcıya en yakın durak: " + baslangicDurak);
            System.out.println("Hedefe en yakın durak: " + hedefDurak);

            // Kullanıcının konumu ile en yakın durak arasındaki mesafeyi hesaplıyoruz.
            double mesafeKullaniciBaslangic = haversineDistance(kullaniciKonum, new Konum(baslangicDurak.getLat(), baslangicDurak.getLon()));
            double mesafeHedefDurak = haversineDistance(new Konum(hedefDurak.getLat(), hedefDurak.getLon()), hedefKonum);
            System.out.println("Kullanıcı -> Baslangıç Durak Mesafesi: " + mesafeKullaniciBaslangic + " km");
            System.out.println("Hedef Durak -> Hedef Konum Mesafesi: " + mesafeHedefDurak + " km");

            // Eğer mesafe 3 km'den fazla ise, bu segment için taksi kullanılması gerekecek.
            double taksiUcretSegment1 = 0.0;
            double taksiUcretSegment2 = 0.0;
            if (mesafeKullaniciBaslangic > 3) {
                taksiUcretSegment1 = RotaHesaplayici.hesaplaTaksiUcreti(kullaniciKonum, new Konum(baslangicDurak.getLat(), baslangicDurak.getLon()), sehirVerisi.getTaxi());
            }
            if (mesafeHedefDurak > 3) {
                taksiUcretSegment2 = RotaHesaplayici.hesaplaTaksiUcreti(new Konum(hedefDurak.getLat(), hedefDurak.getLon()), hedefKonum, sehirVerisi.getTaxi());
            }

            // Rota alternatifleri hesaplanıyor.
            List<List<Durak>> alternatifRotalar = RotaPlanlayici.tumRotalariHesapla(graph, baslangicDurak.getId(), hedefDurak.getId());
            if (alternatifRotalar.isEmpty()) {
                System.out.println("Hiç rota bulunamadı.");
            } else {
                System.out.println("\n=== Tüm Alternatif Rotalar ===");
                double bestCost = Double.MAX_VALUE;
                int bestIndex = -1;
                for (int i = 0; i < alternatifRotalar.size(); i++) {
                    List<Durak> rota = alternatifRotalar.get(i);
                    RotaPlanlayici.RotaMetrics metrics = RotaPlanlayici.hesaplaRotaMetrics(rota);
                    // Toplam ücrete, gerekliyse taksi segmentlerinin ücretini ekliyoruz.
                    double toplamUcret = metrics.toplamUcret + taksiUcretSegment1 + taksiUcretSegment2;
                    System.out.println("Rota " + (i + 1) + ":");
                    for (Durak d : rota) {
                        System.out.print(d.getName() + " -> ");
                    }
                    System.out.println("Bitiş");
                    System.out.println("  Ücret: " + toplamUcret + " TL, Süre: " + metrics.toplamSure + " dk, Mesafe: " + metrics.toplamMesafe + " km");
                    if (taksiUcretSegment1 > 0) {
                        System.out.println("  (Başlangıç segmenti taksi ücreti: " + taksiUcretSegment1 + " TL)");
                    }
                    if (taksiUcretSegment2 > 0) {
                        System.out.println("  (Hedef segmenti taksi ücreti: " + taksiUcretSegment2 + " TL)");
                    }
                    System.out.println();

                    if (toplamUcret < bestCost) {
                        bestCost = toplamUcret;
                        bestIndex = i;
                    }
                }
                System.out.println("=== En Uygun Rota ===");
                if (bestIndex != -1) {
                    List<Durak> enIyiRota = alternatifRotalar.get(bestIndex);
                    RotaPlanlayici.RotaMetrics metrics = RotaPlanlayici.hesaplaRotaMetrics(enIyiRota);
                    double enIyiToplamUcret = metrics.toplamUcret + taksiUcretSegment1 + taksiUcretSegment2;
                    System.out.println("En Uygun Rota (Rota " + (bestIndex + 1) + "):");
                    for (Durak d : enIyiRota) {
                        System.out.print(d.getName() + " -> ");
                    }
                    System.out.println("Bitiş");
                    System.out.println("  Ücret: " + enIyiToplamUcret + " TL, Süre: " + metrics.toplamSure + " dk, Mesafe: " + metrics.toplamMesafe + " km");
                }
            }

            // Metin tabanlı arayüz için rota yazdırma ve ödeme işlemleri.
            Yolcu yolcu = new OgrenciYolcu("Mehmet Öğrenci");
            double rotaUcreti = RotaHesaplayici.hesaplaRotaUcreti(alternatifRotalar.get(0), 0.1)
                    + taksiUcretSegment1 + taksiUcretSegment2;
            double sonUcret = rotaUcreti * (1 - yolcu.getIndirimOrani());
            System.out.println("\nYolcu İndirimli Ücret: " + sonUcret + " TL");

            Odeme odeme = new KentKartOdeme(20.0);
            odeme.odemeIsle(sonUcret);

            double taksiUcreti = RotaHesaplayici.hesaplaTaksiUcreti(kullaniciKonum, hedefKonum, sehirVerisi.getTaxi());
            System.out.println("\n=== Alternatif: Sadece Taksi ===");
            System.out.println("Taksi Ücreti: " + taksiUcreti + " TL");

            // Tahmini varış saati hesaplama: mevcut sistem zamanı + rota süresi
            RotaPlanlayici.RotaMetrics bestMetrics = RotaPlanlayici.hesaplaRotaMetrics(alternatifRotalar.get(0));
            long currentTimeMillis = System.currentTimeMillis();
            long estimatedArrivalMillis = currentTimeMillis + (long)(bestMetrics.toplamSure * 60 * 1000);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            String estimatedArrivalTime = sdf.format(new java.util.Date(estimatedArrivalMillis));
            System.out.println("Tahmini Varış Saati: " + estimatedArrivalTime);

            // Kullanıcıya arayüz seçimi soruluyor:
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nHangi arayüzü kullanmak istersiniz? (1: Metin Tabanlı, 2: JavaFX Harita Tabanlı)");
            int secim = scanner.nextInt();
            if (secim == 1) {
                TextArayuz.gosterRota(alternatifRotalar.get(0), 0.1, sehirVerisi.getTaxi(), kullaniciKonum, hedefKonum);
            } else if (secim == 2) {
                javafx.application.Application.launch(DemoUygulamasi.class);
            } else {
                System.out.println("Geçersiz seçim, metin tabanlı arayüz kullanılıyor.");
                TextArayuz.gosterRota(alternatifRotalar.get(0), 0.1, sehirVerisi.getTaxi(), kullaniciKonum, hedefKonum);
            }
        } else {
            System.out.println("JSON verisi yüklenirken hata oluştu.");
        }
    }
}
