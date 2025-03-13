package kocaeli.ulasim;

public class Otobus extends Arac {

    public Otobus(String plaka) {
        super(plaka);
    }
    @Override
    public void sur() {
        System.out.println("Otobüs hareket ediyor.");
    }
}