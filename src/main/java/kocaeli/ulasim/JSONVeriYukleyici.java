package kocaeli.ulasim;


import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JSONVeriYukleyici {

   
    public static SehirVerisi verileriYukle(String dosyaYolu) {
        try (FileReader reader = new FileReader(dosyaYolu)) {
            Gson gson = new Gson();
            SehirVerisi veri = gson.fromJson(reader, SehirVerisi.class);

            // JSON verisi başarılı bir şekilde okunduğunda, verilerin doğruluğunu kontrol et
            if (veri == null) {
                System.out.println("Veri dosyasındaki içerik hatalı veya eksik.");
                return null;
            }

        
            return veri;

        } catch (JsonSyntaxException e) {
           
            System.out.println("JSON format hatası: " + e.getMessage());
            return null;
        } catch (Exception e) {
 
            System.out.println("JSON verileri yüklenirken hata: " + e.getMessage());
            return null;
        }
    }
}
