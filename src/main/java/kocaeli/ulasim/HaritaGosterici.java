package kocaeli.ulasim;



import java.util.List;

public class HaritaGosterici {
    public static void haritaGoster(List<Durak> rota) {
        System.out.println("=== Harita Görselleştirme ===");
        for(Durak d : rota) {
            System.out.print(d.getName() + " -> ");
        }
        System.out.println("Bitiş");
    }
}
