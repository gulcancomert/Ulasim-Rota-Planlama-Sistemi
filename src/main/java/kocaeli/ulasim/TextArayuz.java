package kocaeli.ulasim;

import java.util.List;

public class TextArayuz {
    public static void gosterRota(List<Durak> rota) {
        System.out.println("=== Rota Detayları (Metin Tabanlı) ===");
        for (Durak d : rota) {
            System.out.println("Durak: " + d.getName());
            System.out.println("   Tip: " + d.getType());
            System.out.println("   Konum: (" + d.getLat() + ", " + d.getLon() + ")");
            System.out.println("   Son Durak: " + d.isSonDurak());
            if (d.getNextStops() != null && !d.getNextStops().isEmpty()) {
                System.out.println("   Next Stops:");
                for (NextStop ns : d.getNextStops()) {
                    System.out.println("      " + ns.getStopId() + " - Mesafe: " + ns.getMesafe() +
                            ", Süre: " + ns.getSure() + ", Ücret: " + ns.getUcret());
                }
            }
            if (d.getTransfer() != null) {
                Transfer t = d.getTransfer();
                System.out.println("   Transfer: " + t.getTransferStopId() + " - Süre: " +
                        t.getTransferSure() + ", Ücret: " + t.getTransferUcret());
            }
            System.out.println();
        }
    }
}