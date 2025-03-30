package kocaeli.ulasim;

public class NakitOdeme implements Odeme {
    private double nakit;

    public NakitOdeme(double nakit) {
        this.nakit = nakit;
    }
    
    // Varsayılan ödeme, indirimsiz
    @Override
    public void odemeIsle(double tutar) {
        odemeIsle(tutar, 0.0);
    }
    
    // İndirimin uygulanabildiği ödeme metodu
    public void odemeIsle(double tutar, double indirimOrani) {
        double odenecek = tutar * (1 - indirimOrani);
        if (odenecek <= nakit) {
            nakit -= odenecek;
            System.out.println("Nakit ödeme yapıldı: " + odenecek + " TL, kalan nakit: " + nakit);
        } else {
            System.out.println("Nakit yetersiz!");
        }
    }
    
    public double getNakit() {
        return nakit;
    }
}
