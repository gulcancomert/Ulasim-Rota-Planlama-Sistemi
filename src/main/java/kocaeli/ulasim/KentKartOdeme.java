package kocaeli.ulasim;



public class KentKartOdeme implements Odeme {
    private double bakiye;

    public KentKartOdeme(double bakiye) {
        this.bakiye = bakiye;
    }
    @Override
    public void odemeIsle(double tutar) {
        if(tutar <= bakiye) {
            bakiye -= tutar;
            System.out.println("KentKart ile ödeme yapıldı: " + tutar + " TL, kalan bakiye: " + bakiye);
        } else {
            System.out.println("KentKart bakiyesi yetersiz!");
        }
    }
}