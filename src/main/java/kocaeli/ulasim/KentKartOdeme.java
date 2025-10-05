package kocaeli.ulasim;

public class KentKartOdeme implements Odeme {
    private double bakiye;

    public KentKartOdeme(double bakiye) {
        this.bakiye = bakiye;
    }
    
 
    @Override
    public void odemeIsle(double tutar) {
        odemeIsle(tutar, 0.0);
    }
   
    public void odemeIsle(double tutar, double indirimOrani) {
        double odenecek = tutar * (1 - indirimOrani);
        if (odenecek <= bakiye) {
            bakiye -= odenecek;
            System.out.println("KentKart ile ödeme yapıldı: " + odenecek + " TL, kalan bakiye: " + bakiye);
        } else {
            System.out.println("KentKart bakiyesi yetersiz!");
        }
    }
    
    public double getBakiye() {
        return bakiye;
    }
}
