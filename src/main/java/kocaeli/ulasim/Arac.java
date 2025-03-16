package kocaeli.ulasim;


public abstract class Arac {
    private String plaka;

    public Arac(String plaka) {
        this.plaka = plaka;
    }

    public String getPlaka() {
        return plaka;
    }

    public abstract void sur();
}