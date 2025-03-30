package kocaeli.ulasim;

public class KrediKartiOdeme implements Odeme {
    private double kartLimiti;

    public KrediKartiOdeme(double kartLimiti) {
        this.kartLimiti = kartLimiti;
    }

    // Varsayılan ödeme, indirimsiz
    @Override
    public void odemeIsle(double tutar) {
        odemeIsle(tutar, 0.0);
    }
    
    // İndirimin uygulanabildiği ödeme metodu
    public void odemeIsle(double tutar, double indirimOrani) {
        double odenecek = tutar * (1 - indirimOrani);
        if (odenecek <= kartLimiti) {
            kartLimiti -= odenecek;
            System.out.println("Kredi Kartı ile ödeme yapıldı: " + odenecek + " TL, kalan limit: " + kartLimiti);
        } else {
            System.out.println("Kredi Kartı limiti yetersiz!");
        }
    }
    
    public double getKartLimiti() {
        return kartLimiti;
    }
}
