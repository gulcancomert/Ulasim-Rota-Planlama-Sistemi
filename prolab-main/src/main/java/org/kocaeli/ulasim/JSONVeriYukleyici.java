package org.kocaeli.ulasim;


import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JSONVeriYukleyici {

    /**
     * JSON dosyasını okur ve SehirVerisi nesnesine dönüştürür.
     *
     * @param dosyaYolu JSON dosyasının tam yolu
     * @return SehirVerisi nesnesi
     */
    public static SehirVerisi verileriYukle(String dosyaYolu) {
        try (FileReader reader = new FileReader(dosyaYolu)) {
            Gson gson = new Gson();
            SehirVerisi veri = gson.fromJson(reader, SehirVerisi.class);

            // JSON verisi başarılı bir şekilde okunduğunda, verilerin doğruluğunu kontrol et
            if (veri == null) {
                System.out.println("Veri dosyasındaki içerik hatalı veya eksik.");
                return null;
            }

            // Veriyi başarıyla döndür
            return veri;

        } catch (JsonSyntaxException e) {
            // JSON format hatası durumunda hata mesajı
            System.out.println("JSON format hatası: " + e.getMessage());
            return null;
        } catch (Exception e) {
            // Diğer hatalar
            System.out.println("JSON verileri yüklenirken hata: " + e.getMessage());
            return null;
        }
    }
}
