package kocaeli.ulasim;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class DemoUygulamasi extends Application {

    // Sol paneldeki alanlar
    private TextField tfCurrentLat;
    private TextField tfCurrentLon;
    private TextField tfDestLat;
    private TextField tfDestLon;
    private TextField tfNakit;
    private TextField tfKrediKartLimiti;
    private TextField tfKentKartBakiye;
    private Label lblCalcSummary;     // Sol panelin altındaki kısa mesaj
    private Label lblRouteSummary;    // Harita altındaki özet

    private WebView webView;
    private Button btnHesapla;
    private final double TAKSI_ESEK = 3.0; // 3 km

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("İzmit Ulaşım Rota Planlama ve Harita Görselleştirme");

        // 1) Sol Panel: Kullanıcı Girdileri
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
        tfNakit = new TextField("0");
        tfNakit.setStyle("-fx-font-size: 14px;");

        Label lblKrediKart = new Label("Kredi Kartı Limiti (TL):");
        lblKrediKart.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfKrediKartLimiti = new TextField("0");
        tfKrediKartLimiti.setStyle("-fx-font-size: 14px;");

        Label lblKentKart = new Label("KentKart Bakiyesi (TL):");
        lblKentKart.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        tfKentKartBakiye = new TextField("0");
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
        String htmlContent = createMapHTML();
        webEngine.loadContent(htmlContent, "text/html");
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

    /**
     * calculateNavigation:
     *  - JSON verisini okur, Graph oluşturur, en yakın durakları tespit eder,
     *    tüm alternatif rotaları hesaplar.
     *  - Her segmentte transit bağlantısı yoksa yürüyüş mesafesi hesaplanır.
     *  - Yolcu indirimi uygulanır.
     *  - Tüm alternatif rotaların adım adım açıklaması hazırlanır.
     *  - Alternatif ulaşım seçenekleri (🚖, 🚍, 🚋, 🛑) uygun olanlara göre kategori olarak yazılır.
     *  - Sonuçlar yeni bir pencere (Stage) içinde gösterilir.
     */
    private void calculateNavigation(String yolcuTipi) {
        try {
            double currLat = Double.parseDouble(tfCurrentLat.getText().trim().replace(',', '.'));
            double currLon = Double.parseDouble(tfCurrentLon.getText().trim().replace(',', '.'));
            double destLatVal = Double.parseDouble(tfDestLat.getText().trim().replace(',', '.'));
            double destLonVal = Double.parseDouble(tfDestLon.getText().trim().replace(',', '.'));
            double nakitMiktar = Double.parseDouble(tfNakit.getText().trim().replace(',', '.'));
            double krediKartLimit = Double.parseDouble(tfKrediKartLimiti.getText().trim().replace(',', '.'));
            double kentKartMiktar = Double.parseDouble(tfKentKartBakiye.getText().trim().replace(',', '.'));

            double indirimOrani = 0.0;
            if ("Öğrenci".equals(yolcuTipi)) {
                indirimOrani = 0.5;
            } else if ("65+".equals(yolcuTipi)) {
                indirimOrani = 0.3;
            }

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

            Durak startDurak = graph.enYakinDurakBul(currentKonum);
            Durak endDurak = graph.enYakinDurakBul(destKonum);

            double startDistance = haversineDistance(currentKonum, new Konum(startDurak.getLat(), startDurak.getLon()));
            double endDistance   = haversineDistance(destKonum, new Konum(endDurak.getLat(), endDurak.getLon()));

            // BFS ile rota arıyoruz
            List<List<Durak>> alternatifRotalar = RotaPlanlayici.tumRotalariHesapla(
                    graph, startDurak.getId(), endDurak.getId()
            );

            // === BFS sonuçları boş ise fallback (Sadece Taksi) rotası ekle ===
            if (alternatifRotalar.isEmpty()) {
                lblCalcSummary.setText("Hiç rota bulunamadı, 'Sadece Taksi' fallback rotası oluşturuldu.");
                List<Durak> fallback = new ArrayList<>();
                fallback.add(startDurak);
                fallback.add(endDurak);
                alternatifRotalar.add(fallback);
            }

            if (alternatifRotalar.isEmpty()) {
                // Yine de boşsa
                lblCalcSummary.setText("Fallback rotası da oluşturulamadı!");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Mevcut Konumun En Yakın Durağı: %s (%.2f km)\n",
                    startDurak.getName(), startDistance));
            sb.append(String.format("Hedef Konumun En Yakın Durağı: %s (%.2f km)\n\n",
                    endDurak.getName(), endDistance));

            double taksiUcretStart = 0, taksiUcretEnd = 0;
            if (startDistance > TAKSI_ESEK) {
                taksiUcretStart = sehirVerisi.getTaxi().getOpeningFee()
                        + sehirVerisi.getTaxi().getCostPerKm() * startDistance;
                sb.append(String.format("Mevcut -> %s: TAKSİ (%.2f km, Ücret: %.2f TL)\n",
                        startDurak.getName(), startDistance, taksiUcretStart));
            } else {
                sb.append(String.format("Mevcut -> %s: YÜRÜYEREK (%.2f km, 0 TL)\n",
                        startDurak.getName(), startDistance));
            }
            if (endDistance > TAKSI_ESEK) {
                taksiUcretEnd = sehirVerisi.getTaxi().getOpeningFee()
                        + sehirVerisi.getTaxi().getCostPerKm() * endDistance;
                sb.append(String.format("%s -> Hedef: TAKSİ (%.2f km, Ücret: %.2f TL)\n\n",
                        endDurak.getName(), endDistance, taksiUcretEnd));
            } else {
                sb.append(String.format("%s -> Hedef: YÜRÜYEREK (%.2f km, 0 TL)\n\n",
                        endDurak.getName(), endDistance));
            }

            sb.append("=== Tüm Alternatif Rotaların Detayları ===\n\n");

            for (int i = 0; i < alternatifRotalar.size(); i++) {
                List<Durak> rota = alternatifRotalar.get(i);

                // Kategori belirleme
                boolean allBus = true;
                boolean allTram = true;
                for (Durak d : rota) {
                    if (!d.getId().startsWith("bus_")) allBus = false;
                    if (!d.getId().startsWith("tram_")) allTram = false;
                }
                List<String> kategoriList = new ArrayList<>();
                if (allBus) kategoriList.add("🚍 Sadece Otobüs");
                if (allTram) kategoriList.add("🚋 Tramvay Öncelikli");

                // En az aktarmalı
                int minSteps = Integer.MAX_VALUE;
                for (List<Durak> r : alternatifRotalar) {
                    if (r.size() < minSteps) {
                        minSteps = r.size();
                    }
                }
                if (rota.size() == minSteps) {
                    kategoriList.add("🛑 En Az Aktarmalı Rota");
                }

                sb.append(String.format("----- Alternatif Rota %d -----\n", i + 1));
                if (!kategoriList.isEmpty()) {
                    sb.append("Kategori: ").append(String.join(", ", kategoriList)).append("\n");
                }

                // Adım adım açıklama
                String rotaDetayi = detayliRotaAciklamasiWithBaslangicVeHedef(
                        rota, startDistance, endDistance, indirimOrani, sehirVerisi.getTaxi()
                );
                sb.append(rotaDetayi);

                RotaPlanlayici.RotaMetrics m = RotaPlanlayici.hesaplaRotaMetrics(rota);
                double realToplamMesafe = m.toplamMesafe + startDistance + endDistance;
                double realToplamUcret = (m.toplamUcret + taksiUcretStart + taksiUcretEnd) * (1 - indirimOrani);
                sb.append(String.format("Toplam Ücret: %.2f TL\n", realToplamUcret));
                sb.append(String.format("Toplam Mesafe: %.2f km\n", realToplamMesafe));
                sb.append(String.format("Toplam Süre: %.0f dk\n\n", m.toplamSure));
            }

            // Sadece taksi (doğrudan)
            double taxiDirectDist = haversineDistance(currentKonum, destKonum);
            double taxiDirectCost = sehirVerisi.getTaxi().getOpeningFee()
                    + sehirVerisi.getTaxi().getCostPerKm() * taxiDirectDist;
            taxiDirectCost = taxiDirectCost * (1 - indirimOrani);
            sb.append(String.format("🚖 Sadece Taksi: Doğrudan taksi maliyeti = %.2f TL\n\n", taxiDirectCost));

            showResultInNewWindow(sb.toString());

        } catch (Exception e) {
            lblCalcSummary.setText("Hesaplama hatası: " + e.getMessage());
        }
    }

    /**
     * detayliRotaAciklamasiWithBaslangicVeHedef:
     * - Mevcut konumdan ilk dura (rota.get(0)) olan adımı ve
     * - Rota içindeki adımları detaylandırır,
     * - Son olarak, son durağından hedef konuma olan adımı ekler.
     */
    private String detayliRotaAciklamasiWithBaslangicVeHedef(
            List<Durak> rota,
            double startDistance,
            double endDistance,
            double indirimOrani,
            Taksi taxi
    ) {
        StringBuilder sb = new StringBuilder();
        int stepNo = 1;
        // Mevcut Konum -> İlk Durak
        if (startDistance > TAKSI_ESEK) {
            double taxiCost = taxi.getOpeningFee() + taxi.getCostPerKm() * startDistance;
            sb.append(String.format(
                    "%d. Adım: Mevcut Konum -> '%s': TAKSİ (%.2f km, Ücret: %.2f TL)\n",
                    stepNo, rota.get(0).getName(), startDistance, taxiCost
            ));
        } else {
            sb.append(String.format(
                    "%d. Adım: Mevcut Konum -> '%s': YÜRÜYEREK (%.2f km, 0 TL)\n",
                    stepNo, rota.get(0).getName(), startDistance
            ));
        }
        stepNo++;

        // Rota içindeki geçişler
        sb.append(detayliRotaAciklamasi(rota, indirimOrani, stepNo));

        // Son adım: Son Durak -> Hedef Konum
        int lastStep = stepNo + rota.size() - 1; // yaklaşık hesap
        if (endDistance > TAKSI_ESEK) {
            double taxiCost = taxi.getOpeningFee() + taxi.getCostPerKm() * endDistance;
            sb.append(String.format(
                    "%d. Adım: '%s' -> Hedef: TAKSİ (%.2f km, Ücret: %.2f TL)\n",
                    lastStep, rota.get(rota.size()-1).getName(), endDistance, taxiCost
            ));
        } else {
            sb.append(String.format(
                    "%d. Adım: '%s' -> Hedef: YÜRÜYEREK (%.2f km, 0 TL)\n",
                    lastStep, rota.get(rota.size()-1).getName(), endDistance
            ));
        }
        return sb.toString();
    }

    /**
     * detayliRotaAciklamasi:
     * Her adımda transit bilgi varsa onu, yoksa yürüyerek mesafesini (0 TL) gösterir.
     */
    private String detayliRotaAciklamasi(List<Durak> rota, double indirimOrani, int startingStep) {
        StringBuilder sb = new StringBuilder();
        int stepNo = startingStep;
        for (int i = 0; i < rota.size() - 1; i++) {
            Durak curr = rota.get(i);
            Durak nxt = rota.get(i + 1);

            double cost = 0, mesafe = 0;
            int sure = 0;
            boolean found = false;
            String arac = "";

            if (curr.getNextStops() != null) {
                for (NextStop ns : curr.getNextStops()) {
                    if (ns.getStopId().equals(nxt.getId())) {
                        cost = ns.getUcret();
                        mesafe = ns.getMesafe();
                        sure = ns.getSure();
                        if (curr.getId().startsWith("bus_") || nxt.getId().startsWith("bus_")) {
                            arac = "Otobüs";
                        } else if (curr.getId().startsWith("tram_") || nxt.getId().startsWith("tram_")) {
                            arac = "Tramvay";
                        } else {
                            arac = "Transfer";
                        }
                        found = true;
                        break;
                    }
                }
            }
            if (!found && curr.getTransfer() != null
                    && curr.getTransfer().getTransferStopId().equals(nxt.getId())) {
                cost = curr.getTransfer().getTransferUcret();
                sure = curr.getTransfer().getTransferSure();
                mesafe = 0;
                arac = "Transfer";
                found = true;
            }

            sb.append(String.format(
                    "%d. Adım: '%s' -> '%s' ",
                    stepNo, curr.getName(), nxt.getName()
            ));

            if (found) {
                sb.append(String.format(
                        "%s (Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL)\n",
                        arac, mesafe, sure, cost * (1 - indirimOrani)
                ));
            } else {
                double walkingDistance = haversineDistance(
                        new Konum(curr.getLat(), curr.getLon()),
                        new Konum(nxt.getLat(), nxt.getLon())
                );
                sb.append(String.format(
                        "YÜRÜYEREK (Mesafe: %.2f km, 0 TL)\n",
                        walkingDistance
                ));
            }
            stepNo++;
        }
        return sb.toString();
    }

    private void showResultInNewWindow(String details) {
        Stage stage = new Stage();
        stage.setTitle("Tüm Rotalar - Detaylar");
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

    public void updateRouteSummary(String summary) {
        Platform.runLater(() -> lblRouteSummary.setText(summary));
    }

    // Haritaya tıklayınca gelen konumlar
    public void updateCurrentLocation(double lat, double lon) {
        Platform.runLater(() -> {
            tfCurrentLat.setText(String.format("%.4f", lat));
            tfCurrentLon.setText(String.format("%.4f", lon));
        });
    }

    public void updateDestinationLocation(double lat, double lon) {
        Platform.runLater(() -> {
            tfDestLat.setText(String.format("%.4f", lat));
            tfDestLon.setText(String.format("%.4f", lon));
        });
    }

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
     * Ayrıca tramvay durakları için ayrı bir ikon tanımladık.
     * 
     * Turuncu (otobüs) ve mor (tramvay) hatlar, OSRM üzerinden
     * ana yolları takip ederek çizilir.
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
            "// bus_stop icon (örnek) - Kendi sunucunuzdaki veya internette erişilebilir URL kullanın\n" +
            "var busStopIcon = L.icon({\n" +
            "  iconUrl: 'file:///C:/Users/HP/OneDrive/Masaüstü/Maven2/demo/src/main/java/kocaeli/ulasim/resimdurak.jpg',\n" +
            "  iconSize: [32, 32],\n" +
            "  iconAnchor: [16, 32],\n" +
            "  popupAnchor: [0, -32]\n" +
            "});\n" +
            "// tram_stop icon (örnek) - Tramvay durakları için farklı bir resim\n" +
            "var tramStopIcon = L.icon({\n" +
            "  iconUrl: 'file:///C:/Users/HP/OneDrive/Masaüstü/Maven2/demo/src/main/java/kocaeli/ulasim/tramvay.jpeg',\n" +
            "  iconSize: [32, 32],\n" +
            "  iconAnchor: [16, 32],\n" +
            "  popupAnchor: [0, -32]\n" +
            "});\n";

        String extraFunctions =
            "function haversineDistance(lat1, lon1, lat2, lon2) {\n" +
            "  var R = 6371;\n" +
            "  var dLat = (lat2 - lat1) * Math.PI / 180;\n" +
            "  var dLon = (lon2 - lon1) * Math.PI / 180;\n" +
            "  var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +\n" +
            "          Math.cos(lat1 * Math.PI/180) * Math.cos(lat2 * Math.PI/180) *\n" +
            "          Math.sin(dLon/2) * Math.sin(dLon/2);\n" +
            "  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));\n" +
            "  return R * c;\n" +
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
            "  return openingFee + (perKm * distance);\n" +
            "}\n" +
            "function updateRouteInfo(startData, destData) {\n" +
            "  var infoText = 'Başlangıç Durak: ' + startData.stop.name + ' (' + startData.distance.toFixed(2) + ' km)\\n';\n" +
            "  infoText += 'Hedef Durak: ' + destData.stop.name + ' (' + destData.distance.toFixed(2) + ' km)\\n';\n" +
            "  if(startData.distance > 3) {\n" +
            "    infoText += 'Başlangıç için taksi önerisi: ' + calculateTaxiFare(startData.distance).toFixed(2) + ' TL\\n';\n" +
            "  }\n" +
            "  if(destData.distance > 3) {\n" +
            "    infoText += 'Hedef için taksi önerisi: ' + calculateTaxiFare(destData.distance).toFixed(2) + ' TL\\n';\n" +
            "  }\n" +
            "  if(window.javaApp) {\n" +
            "    window.javaApp.updateRouteSummary(infoText);\n" +
            "  }\n" +
            "}";

        String extraHTML =
            "<div id='routeInfo' style='position:absolute;bottom:10px;left:10px;background:white;padding:10px;z-index:1000;max-width:300px;'></div>";

        // --- HTML İçeriği ---
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
            + "      var startPos = startMarker.getLatLng();\n"
            + "      var destPos = destMarker.getLatLng();\n"
            + "      var startData = findNearestStop(startPos.lat, startPos.lng);\n"
            + "      var destData = findNearestStop(destPos.lat, destPos.lng);\n"
            + "      updateRouteInfo(startData, destData);\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  map.on('click', function(e){\n"
            + "    if(!startMarker){\n"
            + "      startMarker = L.marker(e.latlng, {draggable:true, icon:redIcon}).addTo(map);\n"
            + "      startMarker.on('drag', function(evt){updateInfoAndJava(evt.target.getLatLng(), 'start');});\n"
            + "      startMarker.on('dragend', function(evt){updateInfoAndJava(evt.target.getLatLng(), 'start');});\n"
            + "      updateInfoAndJava(e.latlng, 'start');\n"
            + "    } else if(!destMarker){\n"
            + "      destMarker = L.marker(e.latlng, {draggable:true, icon:blueIcon}).addTo(map);\n"
            + "      destMarker.on('drag', function(evt){updateInfoAndJava(evt.target.getLatLng(), 'dest');});\n"
            + "      destMarker.on('dragend', function(evt){updateInfoAndJava(evt.target.getLatLng(), 'dest');});\n"
            + "      updateInfoAndJava(e.latlng, 'dest');\n"
            + "    } else {\n"
            + "      destMarker.setLatLng(e.latlng);\n"
            + "      updateInfoAndJava(e.latlng, 'dest');\n"
            + "    }\n"
            + "  });\n"
            + "\n"
            + "  function resetMarkers(){\n"
            + "    if(startMarker){ map.removeLayer(startMarker); startMarker = null; }\n"
            + "    if(destMarker){ map.removeLayer(destMarker); destMarker = null; }\n"
            + "    document.getElementById('info').innerHTML='Seçilen konum: (0,0)';\n"
            + "    document.getElementById('routeInfo').innerText='';\n"
            + "    if(window.javaApp){\n"
            + "      javaApp.updateCurrentLocation(0,0);\n"
            + "      javaApp.updateDestinationLocation(0,0);\n"
            + "      javaApp.updateRouteSummary('');\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  var stops = " + stopsJSArray + ";\n"
            + "  // Durakları, id'sine göre uygun ikonla gösterelim: tram_ için tramStopIcon, bus_ için busStopIcon\n"
            + "  stops.forEach(function(stop){\n"
            + "    var iconToUse = busStopIcon; // varsayılan\n"
            + "    if(stop.id.indexOf('tram_') === 0){\n"
            + "      iconToUse = tramStopIcon;\n"
            + "    }\n"
            + "    L.marker([stop.lat, stop.lon], {icon: iconToUse}).addTo(map).bindPopup(stop.name);\n"
            + "  });\n"
            + "\n"
            + "  // === Otobüs Duraklarını OSRM ile Ana Yollardan Çizelim (Turuncu) ===\n"
            + "  var busStops = stops.filter(function(s){ return s.id.indexOf('bus_') === 0; });\n"
            + "  for(var i=0; i<busStops.length-1; i++) {\n"
            + "    (function(i){\n"
            + "      var from = busStops[i];\n"
            + "      var to = busStops[i+1];\n"
            + "      var url = 'https://router.project-osrm.org/route/v1/driving/'\n"
            + "                + from.lon + ',' + from.lat + ';'\n"
            + "                + to.lon + ',' + to.lat\n"
            + "                + '?overview=full&geometries=geojson';\n"
            + "      fetch(url)\n"
            + "        .then(function(response){ return response.json(); })\n"
            + "        .then(function(data){\n"
            + "          if(data && data.routes && data.routes[0]) {\n"
            + "            var coords = data.routes[0].geometry.coordinates.map(function(c){\n"
            + "              return [c[1], c[0]]; // [lat, lon]\n"
            + "            });\n"
            + "            L.polyline(coords, {color:'blue', weight:3}).addTo(map);\n"
            + "          }\n"
            + "        })\n"
            + "        .catch(function(err){ console.error('OSRM Hatası:', err); });\n"
            + "    })(i);\n"
            + "  }\n"
            + "\n"
            + "  // === Tramvay Duraklarını OSRM ile Ana Yollardan Çizelim (Mor) ===\n"
            + "  var tramStops = stops.filter(function(s){ return s.id.indexOf('tram_') === 0; });\n"
            + "  for(var j=0; j<tramStops.length-1; j++) {\n"
            + "    (function(j){\n"
            + "      var from = tramStops[j];\n"
            + "      var to = tramStops[j+1];\n"
            + "      var url = 'https://router.project-osrm.org/route/v1/driving/'\n"
            + "                + from.lon + ',' + from.lat + ';'\n"
            + "                + to.lon + ',' + to.lat\n"
            + "                + '?overview=full&geometries=geojson';\n"
            + "      fetch(url)\n"
            + "        .then(function(response){ return response.json(); })\n"
            + "        .then(function(data){\n"
            + "          if(data && data.routes && data.routes[0]) {\n"
            + "            var coords = data.routes[0].geometry.coordinates.map(function(c){\n"
            + "              return [c[1], c[0]]; // [lat, lon]\n"
            + "            });\n"
            + "            L.polyline(coords, {color:'red', weight:3}).addTo(map);\n"
            + "          }\n"
            + "        })\n"
            + "        .catch(function(err){ console.error('OSRM Hatası:', err); });\n"
            + "    })(j);\n"
            + "  }\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
