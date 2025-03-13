package kocaeli.ulasim;

public abstract class Yolcu {
    private String isim;

    public Yolcu(String isim) {
        this.isim = isim;
    }
    public String getIsim() {
        return isim;
    }
    public abstract double getIndirimOrani();
}