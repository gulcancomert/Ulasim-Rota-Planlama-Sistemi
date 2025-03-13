package kocaeli.ulasim;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class DemoUygulamasi extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("İzmit Ulaşım Rota Planlama ve Harita Görselleştirme");

        // --- 1) KULLANICI GİRDİLERİ ALANI ---
        Label lblBaslangicZamani = new Label("Seyahat Başlangıç Zamanı:");
        TextField tfBaslangicZamani = new TextField("2025-04-04 08:00");

        Label lblMevcutKonum = new Label("Mevcut Konum (Enlem, Boylam):");
        TextField tfCurrentLat = new TextField("40.7769");
        TextField tfCurrentLon = new TextField("29.9780");

        Label lblHedefKonum = new Label("Hedef Konum (Enlem, Boylam):");
        TextField tfDestLat = new TextField("40.7831");
        TextField tfDestLon = new TextField("29.9326");

        Label lblYolcuTipi = new Label("Yolcu Tipi:");
        ComboBox<String> cbYolcuTipi = new ComboBox<>();
        cbYolcuTipi.getItems().addAll("Genel", "Öğrenci", "Yaşlı");
        cbYolcuTipi.setValue("Genel");

        Button btnBitir = new Button("Bitir");

        VBox inputBox = new VBox(8,
                lblBaslangicZamani, tfBaslangicZamani,
                lblMevcutKonum, new HBox(5, tfCurrentLat, tfCurrentLon),
                lblHedefKonum, new HBox(5, tfDestLat, tfDestLon),
                lblYolcuTipi, cbYolcuTipi,
                btnBitir
        );
        inputBox.setPadding(new Insets(10));

        // --- 2) HARİTA GÖRSELLEŞTİRME ALANI ---
        WebView webView = new WebView();
        webView.setPrefSize(900, 600);
        WebEngine webEngine = webView.getEngine();

        // --- 3) ANA DÜZEN ---
        BorderPane root = new BorderPane();
        root.setLeft(inputBox);
        root.setCenter(webView);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // --- 4) HARİTA YÜKLEME ---
        btnBitir.setOnAction(e -> {
            String startTime = tfBaslangicZamani.getText().trim();
            String cLat = tfCurrentLat.getText().trim();
            String cLon = tfCurrentLon.getText().trim();
            String dLat = tfDestLat.getText().trim();
            String dLon = tfDestLon.getText().trim();
            String yolcuTipi = cbYolcuTipi.getValue();

            // Leaflet.js ile HTML içeriğini oluştur
            String htmlContent = createMapHTML(startTime, cLat, cLon, dLat, dLon, yolcuTipi);
            webEngine.loadContent(htmlContent, "text/html");
        });
    }

    /**
     * Leaflet.js ile İzmit merkezli bir harita oluşturur.
     * Mevcut konum ve hedef konum marker’ları eklenir; rota daha düzgün bir şekilde gösterilir.
     */
    private String createMapHTML(String startTime, String cLat, String cLon,
                                 String dLat, String dLon, String yolcuTipi) {
        // Başlangıç ve hedef noktaları
        String busLat = "40.78259", busLon = "29.94628";    // Otogar (Bus)
        String tramLat = "40.78245", tramLon = "29.94610";    // Otogar (Tram)
        String transferLat = "40.770965", transferLon = "29.959499"; // Transfer noktası

        // Yeni polyline noktaları: mevcut konum -> otobüs durağı -> hedef konum
        // Polyline rotayı daha yumuşak hale getirecek.
        String polyPoints = 
            "[" + cLat + ", " + cLon + "]," +
            "[" + busLat + ", " + busLon + "]," +
            "[" + dLat + ", " + dLon + "]";

        return "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "  <meta charset='utf-8'/>\n"
            + "  <title>Izmit Harita</title>\n"
            + "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
            + "  <link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css'/>\n"
            + "  <script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>\n"
            + "  <style>\n"
            + "    #map { height: 100%; }\n"
            + "    html, body { margin: 0; padding: 0; height: 100%; }\n"
            + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<div id='map'></div>\n"
            + "<script>\n"
            + "  var map = L.map('map').setView([40.78, 29.95], 13);\n"
            + "  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' }).addTo(map);\n"
            + "  var currentMarker = L.marker([" + cLat + ", " + cLon + "]).addTo(map);\n"
            + "  currentMarker.bindPopup('<b>Mevcut Konum</b>').openPopup();\n"
            + "  var destMarker = L.marker([" + dLat + ", " + dLon + "]).addTo(map);\n"
            + "  destMarker.bindPopup('<b>Hedef Konum</b>');\n"
            + "  var routeCoords = [" + polyPoints + "];\n"
            + "  var routeLine = L.polyline(routeCoords, {color: 'red'}).addTo(map);\n"
            + "  map.fitBounds(routeLine.getBounds());\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
