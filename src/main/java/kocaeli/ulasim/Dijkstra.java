package kocaeli.ulasim;



import java.util.ArrayList;
import java.util.List;

public class Dijkstra {
    private Graph graph;

    public Dijkstra(Graph graph) {
        this.graph = graph;
    }

    public List<Durak> kisaYolHesapla(String baslangicId, String hedefId) {
        List<Durak> rota = new ArrayList<>();
        // Basit örnek: başlangıç ve hedef durakları bulup listeye ekliyoruz.
        for(Durak d : graph.getDurakListesi()) {
            if(d.getId().equals(baslangicId)) {
                rota.add(d);
            }
            if(d.getId().equals(hedefId)) {
                rota.add(d);
            }
        }
        return rota;
    }
}