package kocaeli.ulasim;

import java.util.List;

public class Graph {
    private List<Durak> durakListesi;

    public Graph(List<Durak> durakListesi) {
        this.durakListesi = durakListesi;
    }

    public List<Durak> getDurakListesi() {
        return durakListesi;
    }

    public void baglantiOlustur() {
        // Her durak için nextStops'ta tanımlı durakları ilişkilendirecek bağlantıları oluşturabilirsiniz.
        System.out.println("Bağlantılar oluşturuldu.");
    }

    public Durak enYakinDurakBul(Konum konum) {
        Durak enYakin = null;
        double minMesafe = Double.MAX_VALUE;
        for (Durak d : durakListesi) {
            double mesafe = mesafeHesapla(konum, new Konum(d.getLat(), d.getLon()));
            if(mesafe < minMesafe) {
                minMesafe = mesafe;
                enYakin = d;
            }
        }
        return enYakin;
    }

    private double mesafeHesapla(Konum k1, Konum k2) {
        double dx = k1.getEnlem() - k2.getEnlem();
        double dy = k1.getBoylam() - k2.getBoylam();
        return Math.sqrt(dx * dx + dy * dy);
    }
}