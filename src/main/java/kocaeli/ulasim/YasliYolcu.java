package kocaeli.ulasim;


public class YasliYolcu extends Yolcu {

    public YasliYolcu(String isim) {
        super(isim);
    }
    @Override
    public double getIndirimOrani() {
        return 0.3; // %30 indirim
    }
}