package kocaeli.ulasim;

public class OgrenciYolcu extends Yolcu {

    public OgrenciYolcu(String isim) {
        super(isim);
    }
    @Override
    public double getIndirimOrani() {
        return 0.5; // %50 indirim
    }
}