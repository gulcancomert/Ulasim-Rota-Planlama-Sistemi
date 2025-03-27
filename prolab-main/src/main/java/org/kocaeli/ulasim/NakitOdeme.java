package org.kocaeli.ulasim;



public class NakitOdeme implements Odeme {
    @Override
    public void odemeIsle(double tutar) {
        System.out.println("Nakit ödeme yapıldı: " + tutar + " TL");
    }
}