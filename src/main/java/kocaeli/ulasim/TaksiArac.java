package kocaeli.ulasim;


public class TaksiArac extends Arac {

    public TaksiArac(String plaka) {
        super(plaka);
    }
    @Override
    public void sur() {
        System.out.println("Taksi hareket ediyor.");
    }
}