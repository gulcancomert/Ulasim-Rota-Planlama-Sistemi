package org.kocaeli.ulasim;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Group;


/**
 * MapArayuzFX – Canvas tabanlı harita arayüzü.
 *
 * Bu sınıf, JSON dosyasından çekilen durak verilerini kullanarak, otobüs ve tramvay duraklarını,
 * aralarındaki bağlantıları (polyline) çizer. Kullanıcı, Canvas üzerinde ilk tıklamada
 * başlangıç konumunu (marker 1 – mavi) ve ikinci tıklamada hedef konumunu (marker 2 – yeşil) belirler.
 * Marker’lar draggable (sürüklenebilir) olup, her tıklama veya sürüklemede en yakın durak hesaplanır,
 * taksi ücreti (açılış ücreti + km başına ücret) hesaplanır ve rota bilgileri Canvas’ın sol üst köşesine yazılır.
 */
// Durak nesnelerini temsil eden inner sınıf
class Stop {
    String id;
    String name;
    double lat;
    double lon;
    String[] nextStops;
}

// En yakın durak hesaplaması için destek sınıf
class NearestStop {
    Stop stop;
    double distance;
}

public class MapArayuzFX extends Application {

    // İzmit/Kocaeli bölgesine ait sabit koordinat sınırları
    private final double minLat = 40.70;
    private final double maxLat = 40.85;
    private final double minLon = 29.90;
    private final double maxLon = 30.00;
    private final double margin = 50;
    private double drawWidth, drawHeight;

    // Başlangıç ve hedef konum için ayrı koordinatlar
    private double startLat = 40.78;
    private double startLon = 29.95;
    private double destLat = 0;
    private double destLon = 0;

    // Marker yarıçapı (Canvas üzerinde çizilen marker için)
    private final double markerRadius = 10;

    // Sürükleme modu: hangi marker sürüklenecek? (true: start, false: target)
    // Ayrı bir boolean yerine; tıklama sırası ile belirliyoruz.
    private boolean startSet = false;
    private boolean destSet = false;
    // Şu an hangi marker sürükleniyor (start: true, target: false)
    private Boolean draggingStart = null;

    // JSON verisindeki duraklar
    private Stop[] stops;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Harita Üzerinde Konum Seçimi");

        // Canvas oluşturuluyor
        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawWidth = canvas.getWidth() - 2 * margin;
        drawHeight = canvas.getHeight() - 2 * margin;

        // JSON dosyasından durak verilerini yükle
        loadStopsFromJson();

        // İlk çizimde harita, duraklar, polyline ve rota hesaplamaları çizilsin
        drawMap(gc);

        // Fare olayları:
        // Mouse basıldığında, marker'a yakınlık kontrolü yapılarak hangi marker sürüklenecek belirlenir.
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            double clickX = e.getX();
            double clickY = e.getY();
            // Eğer başlangıç marker henüz belirlenmemişse, tıklama otomatik olarak başlangıç konumu olarak alınır.
            if (!startSet) {
                updateStartPosition(e);
                startSet = true;
                // İlk marker, başlangıç olarak belirlendi.
            } else if (!destSet) {
                // Eğer hedef marker henüz belirlenmemişse, tıklama hedef konumu olarak alınır.
                updateDestPosition(e);
                destSet = true;
            } else {
                // Eğer her iki marker belirlenmişse, tıklama noktasının hangi marker'a daha yakın olduğunu kontrol edelim.
                double startX = margin + ((startLon - minLon) / (maxLon - minLon)) * drawWidth;
                double startY = margin + ((maxLat - startLat) / (maxLat - minLat)) * drawHeight;
                double destX = margin + ((destLon - minLon) / (maxLon - minLon)) * drawWidth;
                double destY = margin + ((maxLat - destLat) / (maxLat - minLat)) * drawHeight;
                double distToStart = Math.hypot(clickX - startX, clickY - startY);
                double distToDest = Math.hypot(clickX - destX, clickY - destY);
                if (distToStart < distToDest && distToStart <= markerRadius) {
                    draggingStart = true;
                } else if (distToDest < distToStart && distToDest <= markerRadius) {
                    draggingStart = false;
                } else {
                    // Eğer tıklama marker'ların yakınında değilse, hedef marker konumunu güncelleyelim.
                    updateDestPosition(e);
                }
            }
            drawMap(gc);
        });

        // Mouse sürüklenirken: Eğer bir marker sürükleniyorsa, konum güncellenir.
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (startSet && destSet) {
                if (draggingStart != null) {
                    if (draggingStart) {
                        updateStartPosition(e);
                    } else {
                        updateDestPosition(e);
                    }
                    drawMap(gc);
                }
            } else if (startSet && !destSet && draggingStart != null && draggingStart) {
                // Sadece başlangıç marker'ı var ve onun sürüklenmesi isteniyorsa
                updateStartPosition(e);
                drawMap(gc);
            }
        });

        // Mouse bırakıldığında, sürükleme modu kapatılır.
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> draggingStart = null);

        Scene scene = new Scene(new Group(canvas));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // JSON dosyasından durak verilerini yükler (resources/duraklar.json)
    private void loadStopsFromJson() {
        try {
            InputStream is = getClass().getResourceAsStream("/duraklar.json");
            if (is != null) {
                InputStreamReader reader = new InputStreamReader(is);
                stops = new Gson().fromJson(reader, Stop[].class);
                reader.close();
            } else {
                System.out.println("JSON dosyası bulunamadı!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tıklama veya sürükleme sonucu başlangıç konumunu günceller.
    private void updateStartPosition(MouseEvent e) {
        double clickX = e.getX();
        double clickY = e.getY();
        startLon = minLon + ((clickX - margin) / drawWidth) * (maxLon - minLon);
        startLat = maxLat - ((clickY - margin) / drawHeight) * (maxLat - minLat);
        System.out.println("Başlangıç Konumu Güncellendi -> Enlem: " + startLat + ", Boylam: " + startLon);
    }

    // Tıklama veya sürükleme sonucu hedef konumunu günceller.
    private void updateDestPosition(MouseEvent e) {
        double clickX = e.getX();
        double clickY = e.getY();
        destLon = minLon + ((clickX - margin) / drawWidth) * (maxLon - minLon);
        destLat = maxLat - ((clickY - margin) / drawHeight) * (maxLat - minLat);
        System.out.println("Hedef Konumu Güncellendi -> Enlem: " + destLat + ", Boylam: " + destLon);
    }

    // Canvas üzerinde haritayı, JSON'dan çekilen durakları, polyline bağlantıları ve rota bilgilerini çizer.
    private void drawMap(GraphicsContext gc) {
        double width = gc.getCanvas().getWidth();
        double height = gc.getCanvas().getHeight();

        // Tuvali temizle
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        // Harita alanı çerçevesi
        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeRect(margin, margin, drawWidth, drawHeight);

        // Duraklar: JSON verilerindeki duraklar farklı renklerle çizilir
        if (stops != null) {
            for (Stop stop : stops) {
                double stopX = margin + ((stop.lon - minLon) / (maxLon - minLon)) * drawWidth;
                double stopY = margin + ((maxLat - stop.lat) / (maxLat - minLat)) * drawHeight;
                double diameter = 16;
                if (stop.id.startsWith("bus_")) {
                    gc.setFill(Color.ORANGE);
                } else if (stop.id.startsWith("tram_")) {
                    gc.setFill(Color.PURPLE);
                } else {
                    gc.setFill(Color.GRAY);
                }
                gc.fillOval(stopX - diameter / 2, stopY - diameter / 2, diameter, diameter);
                gc.setFill(Color.BLACK);
                gc.setFont(new Font(12));
                gc.fillText(stop.name, stopX + diameter / 2 + 2, stopY);
            }
        }

        // Otobüs durakları arası bağlantı (orange)
        if (stops != null) {
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(3);
            boolean firstBus = true;
            double prevX = 0, prevY = 0;
            for (Stop stop : stops) {
                if (stop.id.startsWith("bus_")) {
                    double currX = margin + ((stop.lon - minLon) / (maxLon - minLon)) * drawWidth;
                    double currY = margin + ((maxLat - stop.lat) / (maxLat - minLat)) * drawHeight;
                    if (!firstBus) {
                        gc.strokeLine(prevX, prevY, currX, currY);
                    } else {
                        firstBus = false;
                    }
                    prevX = currX;
                    prevY = currY;
                }
            }
        }

        // Tramvay durakları arası bağlantı (purple)
        if (stops != null) {
            gc.setStroke(Color.PURPLE);
            gc.setLineWidth(3);
            boolean firstTram = true;
            double prevX = 0, prevY = 0;
            for (Stop stop : stops) {
                if (stop.id.startsWith("tram_")) {
                    double currX = margin + ((stop.lon - minLon) / (maxLon - minLon)) * drawWidth;
                    double currY = margin + ((maxLat - stop.lat) / (maxLat - minLat)) * drawHeight;
                    if (!firstTram) {
                        gc.strokeLine(prevX, prevY, currX, currY);
                    } else {
                        firstTram = false;
                    }
                    prevX = currX;
                    prevY = currY;
                }
            }
        }

        // Rota hesaplamaları: Eğer başlangıç ve/veya hedef marker belirlenmişse, en yakın durak ve taksi bilgisi hesaplanır.
        String infoText = "";
        if (startSet) {
            NearestStop nsStart = findNearestStop(startLat, startLon);
            infoText += "Başlangıç En Yakın Durak: " + nsStart.stop.name + " (" + String.format("%.2f", nsStart.distance) + " km)";
            if (nsStart.distance > 3) {
                double taxiFare = calculateTaxiFare(nsStart.distance);
                infoText += " - Taksi Ücreti: " + String.format("%.2f", taxiFare) + " TL";
            }
            infoText += "\n";
        }
        if (destSet) {
            NearestStop nsDest = findNearestStop(destLat, destLon);
            infoText += "Hedef En Yakın Durak: " + nsDest.stop.name + " (" + String.format("%.2f", nsDest.distance) + " km)";
            if (nsDest.distance > 3) {
                double taxiFare = calculateTaxiFare(nsDest.distance);
                infoText += " - Taksi Ücreti: " + String.format("%.2f", taxiFare) + " TL";
            }
        }
        gc.setFill(Color.DARKGREEN);
        gc.setFont(new Font(14));
        gc.fillText(infoText, margin + 10, margin + 20);

        // Seçili konum markerları çizimi: Başlangıç marker (mavi) ve hedef marker (yeşil)
        // Başlangıç marker sadece eğer belirlenmişse
        if (startSet) {
            int startX = (int) (margin + ((startLon - minLon) / (maxLon - minLon)) * drawWidth);
            int startY = (int) (margin + ((maxLat - startLat) / (maxLat - minLat)) * drawHeight);
            gc.setFill(Color.BLUE);
            gc.fillOval(startX - markerRadius, startY - markerRadius, markerRadius * 2, markerRadius * 2);
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(12));
            gc.fillText(String.format("(%.4f, %.4f)", startLat, startLon), startX + markerRadius + 2, startY);
        }
        // Hedef marker sadece eğer belirlenmişse
        if (destSet) {
            int destX = (int) (margin + ((destLon - minLon) / (maxLon - minLon)) * drawWidth);
            int destY = (int) (margin + ((maxLat - destLat) / (maxLat - minLat)) * drawHeight);
            gc.setFill(Color.GREEN);
            gc.fillOval(destX - markerRadius, destY - markerRadius, markerRadius * 2, markerRadius * 2);
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(12));
            gc.fillText(String.format("(%.4f, %.4f)", destLat, destLon), destX + markerRadius + 2, destY);
        }
    }

    // Haversine mesafe hesaplama (km cinsinden)
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Dünya yarıçapı (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // En yakın durak hesaplaması için destek sınıf
    private class NearestStop {
        Stop stop;
        double distance;
    }

    // Verilen koordinata en yakın durak (NearestStop) bulunur.
    private NearestStop findNearestStop(double lat, double lon) {
        NearestStop ns = new NearestStop();
        ns.distance = Double.MAX_VALUE;
        for (Stop s : stops) {
            double d = haversineDistance(lat, lon, s.lat, s.lon);
            if (d < ns.distance) {
                ns.distance = d;
                ns.stop = s;
            }
        }
        return ns;
    }

    // Taksi ücret hesaplaması: Açılış ücreti + km başına ücret çarpımı
    private double calculateTaxiFare(double distance) {
        double openingFee = 10.0;
        double perKmCost = 4.0;
        return openingFee + perKmCost * distance;
    }

    // Durak nesnelerini temsil eden inner sınıf
    class Stop {
        String id;
        String name;
        double lat;
        double lon;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
