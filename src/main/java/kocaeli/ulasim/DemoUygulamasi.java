package kocaeli.ulasim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * DemoUygulamasi: Tek parça kod.
 * - Kocaeli sınır kontrolü
 * - Durak ikonunu "bus_stop" resmi ile gösterme
 * - BFS ile rota arama (en mantıklı/en kısa rota)
 */
public class DemoUygulamasi extends Application {

    // Sol panel alanları
    private TextField tfCurrentLat;
    private TextField tfCurrentLon;
    private TextField tfDestLat;
    private TextField tfDestLon;
    private TextField tfNakit;
    private TextField tfKrediKartLimiti;
    private TextField tfKentKartBakiye;
    private Label lblCalcSummary;
    private Label lblRouteSummary;

    private WebView webView;
    private Button btnHesapla;

    private final double TAKSI_ESEK = 3.0; // 3 km

    // Kocaeli koordinat sınırları
    private final double MIN_LAT = 40.70;
    private final double MAX_LAT = 40.85;
    private final double MIN_LON = 29.90;
    private final double MAX_LON = 30.05;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("İzmit Ulaşım Rota Planlama ve Harita Görselleştirme");

        // 1) Sol Panel
        Label lblBaslangicZamani = new Label("Seyahat Başlangıç Zamanı:");
        lblBaslangicZamani.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        TextField tfBaslangicZamani = new TextField("2025-04-04 08:00");
        tfBaslangicZamani.setStyle("-fx-font-size: 14px;");

        Label lblMevcutKonum = new Label("Mevcut Konum (Enlem, Boylam):");
        lblMevcutKonum.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfCurrentLat = new TextField("0");
        tfCurrentLat.setStyle("-fx-font-size: 14px;");
        tfCurrentLon = new TextField("0");
        tfCurrentLon.setStyle("-fx-font-size: 14px;");

        Label lblHedefKonum = new Label("Hedef Konum (Enlem, Boylam):");
        lblHedefKonum.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfDestLat = new TextField("0");
        tfDestLat.setStyle("-fx-font-size: 14px;");
        tfDestLon = new TextField("0");
        tfDestLon.setStyle("-fx-font-size: 14px;");

        Label lblYolcuTipi = new Label("Yolcu Tipi:");
        lblYolcuTipi.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        ComboBox<String> cbYolcuTipi = new ComboBox<>();
        cbYolcuTipi.getItems().addAll("Genel", "Öğrenci", "Öğretmen", "65+");
        cbYolcuTipi.setValue("Genel");
        cbYolcuTipi.setStyle("-fx-font-size: 14px;");

        Label lblNakit = new Label("Nakit (TL):");
        lblNakit.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfNakit = new TextField("100");
        tfNakit.setStyle("-fx-font-size: 14px;");

        Label lblKrediKart = new Label("Kredi Kartı Limiti (TL):");
        lblKrediKart.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfKrediKartLimiti = new TextField("500");
        tfKrediKartLimiti.setStyle("-fx-font-size: 14px;");

        Label lblKentKart = new Label("KentKart Bakiyesi (TL):");
        lblKentKart.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfKentKartBakiye = new TextField("200");
        tfKentKartBakiye.setStyle("-fx-font-size: 14px;");

        btnHesapla = new Button("Hesapla");
        btnHesapla.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: #8fbc8f;");
        btnHesapla.setOnAction(e -> calculateNavigation(cbYolcuTipi.getValue()));

        Button btnSifirla = new Button("Sıfırla");
        btnSifirla.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: #ffb6c1;");
        btnSifirla.setOnAction(e -> {
            WebEngine webEngine = webView.getEngine();
            webEngine.executeScript("resetMarkers()");
        });

        HBox buttonBox = new HBox(10, btnHesapla, btnSifirla);

        VBox inputBox = new VBox(12,
                lblBaslangicZamani, tfBaslangicZamani,
                lblMevcutKonum, new HBox(8, tfCurrentLat, tfCurrentLon),
                lblHedefKonum, new HBox(8, tfDestLat, tfDestLon),
                lblYolcuTipi, cbYolcuTipi,
                lblNakit, tfNakit,
                lblKrediKart, tfKrediKartLimiti,
                lblKentKart, tfKentKartBakiye,
                buttonBox
        );
        inputBox.setPadding(new Insets(15));
        inputBox.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f8ff, #e6f7ff); "
                + "-fx-border-color: #cccccc; -fx-border-width: 2px;");

        lblCalcSummary = new Label("Navigation sonuçları burada görünecek...");
        lblCalcSummary.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        inputBox.getChildren().add(lblCalcSummary);

        // 2) Harita Alanı
        webView = new WebView();
        webView.setPrefSize(900, 600);
        webView.setOnContextMenuRequested(e -> e.consume());
        WebEngine webEngine = webView.getEngine();

        // Harita HTML içeriği
        String htmlContent = createMapHTML();
        webEngine.loadContent(htmlContent, "text/html");

        // Harita tam yüklendiğinde JS tarafına Java referansı verelim
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        // 3) Harita Altındaki Özet
        lblRouteSummary = new Label("En İyi Rota bilgisi burada görünecek...");
        lblRouteSummary.setPadding(new Insets(10));
        lblRouteSummary.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-font-size: 14px;");

        BorderPane root = new BorderPane();
        root.setLeft(inputBox);
        root.setCenter(webView);
        root.setBottom(lblRouteSummary);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // Kullanıcının güncellenen konum değerlerini Kocaeli sınırları içinde tutmak için yardımcı metot
    private boolean isWithinKocaeli(double lat, double lon) {
        return (lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON);
    }

    // Mevcut konum güncelleme: Kocaeli sınır kontrolü
    public void updateCurrentLocation(double lat, double lon) {
        if (!isWithinKocaeli(lat, lon)) {
            System.out.println("Uyarı: Mevcut konum Kocaeli sınırları dışında!");
            return;
        }
        Platform.runLater(() -> {
            tfCurrentLat.setText(String.format("%.4f", lat));
            tfCurrentLon.setText(String.format("%.4f", lon));
        });
    }

    // Hedef konum güncelleme: Kocaeli sınır kontrolü
    public void updateDestinationLocation(double lat, double lon) {
        if (!isWithinKocaeli(lat, lon)) {
            System.out.println("Uyarı: Hedef konum Kocaeli sınırları dışında!");
            return;
        }
        Platform.runLater(() -> {
            tfDestLat.setText(String.format("%.4f", lat));
            tfDestLon.setText(String.format("%.4f", lon));
        });
    }

    /**
     * calculateNavigation:
     *  - JSON verisini okur, Graph oluşturur, en yakın durakları tespit eder,
     *    BFS ile tüm alternatif rotaları hesaplar.
     */
    private void calculateNavigation(String yolcuTipi) {
        try {
            double currLat = Double.parseDouble(tfCurrentLat.getText().trim().replace(',', '.'));
            double currLon = Double.parseDouble(tfCurrentLon.getText().trim().replace(',', '.'));
            double destLatVal = Double.parseDouble(tfDestLat.getText().trim().replace(',', '.'));
            double destLonVal = Double.parseDouble(tfDestLon.getText().trim().replace(',', '.'));

            // (Nakit, Kredi, KentKart) vb. okuyoruz, ama bu örnekte direkt rota hesaplamaya odaklanacağız
            // double nakitMiktar = Double.parseDouble(tfNakit.getText().trim().replace(',', '.'));
            // ...

            // Yolcu tipi indirimi
            double indirimOrani = 0.0;
            if ("Öğrenci".equals(yolcuTipi)) {
                indirimOrani = 0.5;
            } else if ("65+".equals(yolcuTipi)) {
                indirimOrani = 0.3;
            }

            // JSON verisi
            SehirVerisi sehirVerisi = JSONVeriYukleyici.verileriYukle(
                    "C:\\Users\\HP\\OneDrive\\Masaüstü\\Maven2\\demo\\src\\main\\java\\kocaeli\\ulasim\\jsonveri.json"
            );
            if (sehirVerisi == null) {
                lblCalcSummary.setText("JSON verisi yüklenemedi!");
                return;
            }

            Graph graph = new Graph(sehirVerisi.getDuraklar());
            graph.baglantiOlustur();

            Konum currentKonum = new Konum(currLat, currLon);
            Konum destKonum = new Konum(destLatVal, destLonVal);

            // En yakın duraklar
            Durak startDurak = graph.enYakinDurakBul(currentKonum);
            Durak endDurak = graph.enYakinDurakBul(destKonum);

            double startDist = haversineDistance(currentKonum, new Konum(startDurak.getLat(), startDurak.getLon()));
            double endDist   = haversineDistance(destKonum, new Konum(endDurak.getLat(), endDurak.getLon()));

            // BFS ile tüm rotaları bulalım
            List<List<Durak>> tumRotalar = tumRotalariBulBFS(graph, startDurak.getId(), endDurak.getId());
            if (tumRotalar.isEmpty()) {
                lblCalcSummary.setText("Rota bulunamadı!");
                return;
            }

            // En iyi rota seçimi
            double bestCost = Double.MAX_VALUE;
            int bestIndex = -1;
            StringBuilder sbAll = new StringBuilder();
            sbAll.append("Mevcut -> en yakın durak: ").append(startDurak.getName()).append(String.format(" (%.2f km)\n", startDist));
            sbAll.append("Hedef -> en yakın durak: ").append(endDurak.getName()).append(String.format(" (%.2f km)\n\n", endDist));

            sbAll.append("=== Tüm Olası Rotalar ===\n\n");
            for (int i = 0; i < tumRotalar.size(); i++) {
                List<Durak> rota = tumRotalar.get(i);
                // Metrikleri hesaplayalım
                RotaMetrics metrics = hesaplaRotaMetrics(rota);
                // Taksiler eklenecekse:
                double taksiStart = (startDist > TAKSI_ESEK) ? sehirVerisi.getTaxi().getOpeningFee() + sehirVerisi.getTaxi().getCostPerKm()*startDist : 0;
                double taksiEnd   = (endDist   > TAKSI_ESEK) ? sehirVerisi.getTaxi().getOpeningFee() + sehirVerisi.getTaxi().getCostPerKm()*endDist   : 0;
                double totalCost = metrics.toplamUcret + taksiStart + taksiEnd;

                sbAll.append("Rota #").append(i+1).append(": ");
                for (Durak d : rota) {
                    sbAll.append(d.getName()).append(" -> ");
                }
                sbAll.append("Bitiş\n");
                sbAll.append(String.format("  Ücret: %.2f TL, Süre: %.0f dk, Mesafe: %.2f km\n", totalCost, metrics.toplamSure, metrics.toplamMesafe));
                if (totalCost < bestCost) {
                    bestCost = totalCost;
                    bestIndex = i;
                }
                sbAll.append("\n");
            }
            if (bestIndex < 0) {
                lblCalcSummary.setText("Hiç rota seçilemedi!");
                return;
            }
            // En iyi rota
            sbAll.append("=== En İyi Rota (#").append(bestIndex+1).append(") ===\n");
            List<Durak> bestRota = tumRotalar.get(bestIndex);
            for (Durak d : bestRota) {
                sbAll.append(d.getName()).append(" -> ");
            }
            sbAll.append("Bitiş\n");

            // Sonucu ayrı pencerede göster
            showResultInNewWindow(sbAll.toString());

        } catch (Exception e) {
            lblCalcSummary.setText("Hesaplama hatası: " + e.getMessage());
        }
    }

    /**
     * BFS ile tüm rotaları bulur.
     * "bus_otogar" -> "bus_41burda" gibi
     */
    private List<List<Durak>> tumRotalariBulBFS(Graph graph, String startId, String endId) {
        List<List<Durak>> allPaths = new ArrayList<>();
        List<Durak> duraklar = graph.getDurakListesi();
        Durak start = null, end = null;
        for (Durak d : duraklar) {
            if (d.getId().equals(startId)) start = d;
            if (d.getId().equals(endId))   end = d;
        }
        if (start == null || end == null) return allPaths;

        // BFS queue: path
        Queue<List<Durak>> queue = new LinkedList<>();
        List<Durak> firstPath = new ArrayList<>();
        firstPath.add(start);
        queue.add(firstPath);

        while (!queue.isEmpty()) {
            List<Durak> path = queue.poll();
            Durak last = path.get(path.size()-1);
            if (last.getId().equals(endId)) {
                allPaths.add(path);
                continue; // BFS ile tüm olası yolları eklemek için continue
            }
            // nextStops + transfer
            if (last.getNextStops() != null) {
                for (NextStop ns : last.getNextStops()) {
                    Durak nextDurak = null;
                    for (Durak dd : duraklar) {
                        if (dd.getId().equals(ns.getStopId())) {
                            nextDurak = dd;
                            break;
                        }
                    }
                    if (nextDurak != null && !path.contains(nextDurak)) {
                        List<Durak> newPath = new ArrayList<>(path);
                        newPath.add(nextDurak);
                        queue.add(newPath);
                    }
                }
            }
            if (last.getTransfer() != null) {
                // Transfer
                String tid = last.getTransfer().getTransferStopId();
                Durak transferDurak = null;
                for (Durak dd : duraklar) {
                    if (dd.getId().equals(tid)) {
                        transferDurak = dd;
                        break;
                    }
                }
                if (transferDurak != null && !path.contains(transferDurak)) {
                    List<Durak> newPath = new ArrayList<>(path);
                    newPath.add(transferDurak);
                    queue.add(newPath);
                }
            }
        }
        return allPaths;
    }

    /**
     * Rota Metrikleri (toplamUcret, toplamSüre, toplamMesafe)
     */
    private RotaMetrics hesaplaRotaMetrics(List<Durak> rota) {
        double sumUcret = 0;
        double sumMesafe = 0;
        double sumSure = 0;
        for (int i=0; i<rota.size()-1; i++) {
            Durak curr = rota.get(i);
            Durak nxt  = rota.get(i+1);
            // nextStops
            boolean found = false;
            if (curr.getNextStops() != null) {
                for (NextStop ns : curr.getNextStops()) {
                    if (ns.getStopId().equals(nxt.getId())) {
                        sumUcret += ns.getUcret();
                        sumMesafe+= ns.getMesafe();
                        sumSure  += ns.getSure();
                        found = true;
                        break;
                    }
                }
            }
            if (!found && curr.getTransfer()!=null) {
                if (curr.getTransfer().getTransferStopId().equals(nxt.getId())) {
                    sumUcret += curr.getTransfer().getTransferUcret();
                    sumSure  += curr.getTransfer().getTransferSure();
                    // Mesafe = 0 (transfer)
                }
            }
        }
        RotaMetrics rm = new RotaMetrics();
        rm.toplamUcret  = sumUcret;
        rm.toplamMesafe = sumMesafe;
        rm.toplamSure   = sumSure;
        return rm;
    }

    // BFS Rota Metrik tutucu
    private static class RotaMetrics {
        double toplamUcret;
        double toplamMesafe;
        double toplamSure;
    }

    // Pencerede sonuç göster
    private void showResultInNewWindow(String details) {
        Stage stage = new Stage();
        stage.setTitle("Rota Detayları");
        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        VBox vbox = new VBox(textArea);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 650, 600);
        stage.setScene(scene);
        stage.show();
    }

    // Haversine
    private double haversineDistance(Konum k1, Konum k2) {
        double R = 6371;
        double dLat = Math.toRadians(k2.getEnlem() - k1.getEnlem());
        double dLon = Math.toRadians(k2.getBoylam() - k1.getBoylam());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(k1.getEnlem())) * Math.cos(Math.toRadians(k2.getEnlem()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * createMapHTML:
     * Durak ikonunu "bus_stop" resmi (örnek URL) ile gösteriyoruz.
     */
    private String createMapHTML() {
        // stopsJSArray: JSON verisi client side'da durak konumlarını ekrana basmak için
        String stopsJSArray =
            "[\n" +
            "  { \"id\": \"bus_otogar\", \"name\": \"Otogar (Bus)\", \"lat\": 40.78259, \"lon\": 29.94628 },\n" +
            "  { \"id\": \"bus_sekapark\", \"name\": \"Sekapark (Bus)\", \"lat\": 40.76520, \"lon\": 29.96190 },\n" +
            "  { \"id\": \"bus_yahyakaptan\", \"name\": \"Yahya Kaptan (Bus)\", \"lat\": 40.770965, \"lon\": 29.959499 },\n" +
            "  { \"id\": \"bus_umuttepe\", \"name\": \"Umuttepe (Bus)\", \"lat\": 40.82103, \"lon\": 29.91843 },\n" +
            "  { \"id\": \"bus_symbolavm\", \"name\": \"Symbol AVM (Bus)\", \"lat\": 40.77788, \"lon\": 29.94991 },\n" +
            "  { \"id\": \"bus_41burda\", \"name\": \"41 Burda AVM (Bus)\", \"lat\": 40.77731, \"lon\": 29.92512 },\n" +
            "  { \"id\": \"tram_otogar\", \"name\": \"Otogar (Tram)\", \"lat\": 40.78245, \"lon\": 29.94610 },\n" +
            "  { \"id\": \"tram_yahyakaptan\", \"name\": \"Yahya Kaptan (Tram)\", \"lat\": 40.77160, \"lon\": 29.96010 },\n" +
            "  { \"id\": \"tram_sekapark\", \"name\": \"Sekapark (Tram)\", \"lat\": 40.76200, \"lon\": 29.96550 },\n" +
            "  { \"id\": \"tram_halkevi\", \"name\": \"Halkevi (Tram)\", \"lat\": 40.76350, \"lon\": 29.93870 }\n" +
            "]";

        String monkeyPatch =
            "if (L.Draggable && L.Draggable.prototype._onDown) {\n" +
            "  var originalOnDown = L.Draggable.prototype._onDown;\n" +
            "  L.Draggable.prototype._onDown = function(e) {\n" +
            "    if (e.pointerType === 'mouse' && e.buttons === 0) {\n" +
            "      e.buttons = 1;\n" +
            "    }\n" +
            "    return originalOnDown.call(this, e);\n" +
            "  };\n" +
            "}";

        String iconDefinitions =
            "// startMarker (red), destMarker (blue)\n" +
            "var redIcon = L.icon({\n" +
            "  iconUrl: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',\n" +
            "  iconSize: [32, 32],\n" +
            "  iconAnchor: [16, 32],\n" +
            "  popupAnchor: [0, -32]\n" +
            "});\n" +
            "var blueIcon = L.icon({\n" +
            "  iconUrl: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',\n" +
            "  iconSize: [32, 32],\n" +
            "  iconAnchor: [16, 32],\n" +
            "  popupAnchor: [0, -32]\n" +
            "});\n" +
            "// bus_stop icon (örnek)\n" +
            "var busStopIcon = L.icon({\n" +
            "  iconUrl: 'https://cdn-icons-png.flaticon.com/512/2980/2980445.png',\n" +
            "  iconSize: [32, 32],\n" +
            "  iconAnchor: [16, 32],\n" +
            "  popupAnchor: [0, -32]\n" +
            "});\n";

        String extraFunctions =
            "function haversineDistance(lat1, lon1, lat2, lon2) {\n" +
            "  var R = 6371;\n" +
            "  var dLat = (lat2 - lat1) * Math.PI / 180;\n" +
            "  var dLon = (lon2 - lon1) * Math.PI / 180;\n" +
            "  var a = Math.sin(dLat/2)*Math.sin(dLat/2) +\n" +
            "          Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*\n" +
            "          Math.sin(dLon/2)*Math.sin(dLon/2);\n" +
            "  var c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));\n" +
            "  return R*c;\n" +
            "}\n" +
            "function findNearestStop(lat, lon) {\n" +
            "  var nearest = null;\n" +
            "  var minDist = Infinity;\n" +
            "  stops.forEach(function(stop) {\n" +
            "    var d = haversineDistance(lat, lon, stop.lat, stop.lon);\n" +
            "    if(d < minDist) { minDist = d; nearest = stop; }\n" +
            "  });\n" +
            "  return {stop: nearest, distance: minDist};\n" +
            "}\n" +
            "function calculateTaxiFare(distance) {\n" +
            "  var openingFee = 10;\n" +
            "  var perKm = 4;\n" +
            "  return openingFee + (perKm*distance);\n" +
            "}\n" +
            "function updateRouteInfo(startData, destData) {\n" +
            "  var infoText = 'Başlangıç Durak: '+startData.stop.name+' ('+startData.distance.toFixed(2)+' km)\\n';\n" +
            "  infoText += 'Hedef Durak: '+destData.stop.name+' ('+destData.distance.toFixed(2)+' km)\\n';\n" +
            "  if(startData.distance>3) {\n" +
            "    infoText+='Başlangıç için taksi önerisi: '+calculateTaxiFare(startData.distance).toFixed(2)+' TL\\n';\n" +
            "  }\n" +
            
            "  if(destData.distance>3) {\n" +
            "    infoText+='Hedef için taksi önerisi: '+calculateTaxiFare(destData.distance).toFixed(2)+' TL\\n';\n" +
            "  }\n" +
            "  if(window.javaApp) {\n" +
            "    window.javaApp.updateRouteSummary(infoText);\n" +
            "  }\n" +
            "}";

        String extraHTML =
            "<div id='routeInfo' style='position:absolute;bottom:10px;left:10px;background:white;padding:10px;z-index:1000;max-width:300px;'></div>";

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
            + "    #info { position: absolute; top: 10px; left: 10px; background: white; padding: 5px; z-index: 1000; }\n"
            + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<div id='info'>Seçilen konum: (0, 0)</div>\n"
            + extraHTML + "\n"
            + "<div id='map'></div>\n"
            + "<script>\n"
            + monkeyPatch + "\n"
            + iconDefinitions + "\n"
            + extraFunctions + "\n"
            + "  var map = L.map('map', { tap: false }).setView([40.78, 29.95], 13);\n"
            + "  map.dragging.enable();\n"
            + "  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n"
            + "    maxZoom: 19,\n"
            + "    attribution: '© OpenStreetMap'\n"
            + "  }).addTo(map);\n"
            + "\n"
            + "  map.whenReady(function(){\n"
            + "    console.log('Harita tam yüklendi.');\n"
            + "  });\n"
            + "\n"
            + "  var startMarker = null;\n"
            + "  var destMarker = null;\n"
            + "  function updateInfoAndJava(latlng, type) {\n"
            + "    document.getElementById('info').innerHTML='Seçilen konum: ('+latlng.lat.toFixed(4)+', '+latlng.lng.toFixed(4)+')';\n"
            + "    if(window.javaApp){\n"
            + "      if(type==='start'){ javaApp.updateCurrentLocation(latlng.lat, latlng.lng); }\n"
            + "      else if(type==='dest'){ javaApp.updateDestinationLocation(latlng.lat, latlng.lng); }\n"
            + "    }\n"
            + "    if(startMarker && destMarker){\n"
            + "      var startPos=startMarker.getLatLng();\n"
            + "      var destPos=destMarker.getLatLng();\n"
            + "      var startData=findNearestStop(startPos.lat, startPos.lng);\n"
            + "      var destData=findNearestStop(destPos.lat, destPos.lng);\n"
            + "      updateRouteInfo(startData,destData);\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  map.on('click',function(e){\n"
            + "    if(!startMarker){\n"
            + "      startMarker=L.marker(e.latlng,{draggable:true,icon:redIcon}).addTo(map);\n"
            + "      startMarker.on('drag',function(evt){updateInfoAndJava(evt.target.getLatLng(),'start');});\n"
            + "      startMarker.on('dragend',function(evt){updateInfoAndJava(evt.target.getLatLng(),'start');});\n"
            + "      updateInfoAndJava(e.latlng,'start');\n"
            + "    }else if(!destMarker){\n"
            + "      destMarker=L.marker(e.latlng,{draggable:true,icon:blueIcon}).addTo(map);\n"
            + "      destMarker.on('drag',function(evt){updateInfoAndJava(evt.target.getLatLng(),'dest');});\n"
            + "      destMarker.on('dragend',function(evt){updateInfoAndJava(evt.target.getLatLng(),'dest');});\n"
            + "      updateInfoAndJava(e.latlng,'dest');\n"
            + "    }else{\n"
            + "      destMarker.setLatLng(e.latlng);\n"
            + "      updateInfoAndJava(e.latlng,'dest');\n"
            + "    }\n"
            + "  });\n"
            + "\n"
            + "  function resetMarkers(){\n"
            + "    if(startMarker){map.removeLayer(startMarker);startMarker=null;}\n"
            + "    if(destMarker){map.removeLayer(destMarker);destMarker=null;}\n"
            + "    document.getElementById('info').innerHTML='Seçilen konum: (0,0)';\n"
            + "    document.getElementById('routeInfo').innerText='';\n"
            + "    if(window.javaApp){\n"
            + "      javaApp.updateCurrentLocation(0,0);\n"
            + "      javaApp.updateDestinationLocation(0,0);\n"
            + "      javaApp.updateRouteSummary('');\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  var stops="+stopsJSArray+";\n"
            + "  // Durakları busStopIcon ile gösterelim\n"
            + "  stops.forEach(function(stop){\n"
            + "    L.marker([stop.lat,stop.lon],{icon:busStopIcon}).addTo(map).bindPopup(stop.name);\n"
            + "  });\n"
            + "\n"
            + "  // Otobüs durakları -> turuncu polyline\n"
            + "  var busStops=stops.filter(function(s){return s.id.indexOf('bus_')===0;});\n"
            + "  if(busStops.length>1){\n"
            + "    var busCoords=busStops.map(function(s){return[s.lat,s.lon];});\n"
            + "    L.polyline(busCoords,{color:'orange',weight:3}).addTo(map);\n"
            + "  }\n"
            + "  // Tramvay durakları -> mor polyline\n"
            + "  var tramStops=stops.filter(function(s){return s.id.indexOf('tram_')===0;});\n"
            + "  if(tramStops.length>1){\n"
            + "    var tramCoords=tramStops.map(function(s){return[s.lat,s.lon];});\n"
            + "    L.polyline(tramCoords,{color:'purple',weight:3}).addTo(map);\n"
            + "  }\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>";
    }

    // Basit bir veri tutucu sınıf: BFS ile rota metrikleri */

    /* 
    private static class RotaMetrics {
        double toplamUcret;
        double toplamMesafe;
        double toplamSure;
    }
*/
    public static void main(String[] args) {
        launch(args);
    }
}
