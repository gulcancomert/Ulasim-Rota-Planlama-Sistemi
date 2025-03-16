package kocaeli.ulasim;

public class KrediKartiOdeme implements Odeme {
    private double kartLimiti;

    public KrediKartiOdeme(double kartLimiti) {
        this.kartLimiti = kartLimiti;
    }
    @Override
    public void odemeIsle(double tutar) {
        if(tutar <= kartLimiti) {
            System.out.println("Kredi Kartı ile ödeme yapıldı: " + tutar + " TL");
        } else {
            System.out.println("Kredi Kartı limiti yetersiz!");
        }
    }
}