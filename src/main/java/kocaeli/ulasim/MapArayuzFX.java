package kocaeli.ulasim;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.List;

public class MapArayuzFX extends Application {

    // Rota bilgilerini tutacak statik değişken (Main'den set edilecek)
    private static List<Durak> rota;

    // Main sınıfından çağırarak rota bilgisini set edelim
    public static void setRota(List<Durak> rota) {
        MapArayuzFX.rota = rota;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rota Harita Görselleştirme (JavaFX)");
        Group root = new Group();
        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawMap(gc, canvas.getWidth(), canvas.getHeight());
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void drawMap(GraphicsContext gc, double width, double height) {
        if (rota == null || rota.isEmpty()) return;

        // Rotadaki enlem ve boylam değerlerinin min/max'unu bulalım.
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        for (Durak d : rota) {
            if (d.getLat() < minLat) minLat = d.getLat();
            if (d.getLat() > maxLat) maxLat = d.getLat();
            if (d.getLon() < minLon) minLon = d.getLon();
            if (d.getLon() > maxLon) maxLon = d.getLon();
        }

        double margin = 50;
        double drawWidth = width - 2 * margin;
        double drawHeight = height - 2 * margin;

        // Tuvali temizleyelim:
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        gc.setFont(new Font(12));
        double prevX = -1, prevY = -1;
        for (Durak d : rota) {
            // Basit ölçeklendirme: longitude'u x'e, latitude'ı y'ye dönüştürelim.
            int x = (int) (margin + ((d.getLon() - minLon) / (maxLon - minLon)) * drawWidth);
            int y = (int) (margin + ((maxLat - d.getLat()) / (maxLat - minLat)) * drawHeight);

            gc.setFill(Color.BLUE);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setFill(Color.BLACK);
            gc.fillText(d.getName(), x + 7, y);
            if (prevX != -1 && prevY != -1) {
                gc.setStroke(Color.RED);
                gc.strokeLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }
    }
}