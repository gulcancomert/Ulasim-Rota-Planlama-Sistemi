package org.kocaeli.ulasim;



public class Konum {
    private double enlem;
    private double boylam;

    public Konum(double enlem, double boylam) {
        this.enlem = enlem;
        this.boylam = boylam;
    }
    public double getEnlem() {
        return enlem;
    }
    public void setEnlem(double enlem) {
        this.enlem = enlem;
    }
    public double getBoylam() {
        return boylam;
    }
    public void setBoylam(double boylam) {
        this.boylam = boylam;
    }
    @Override
    public String toString() {
        return "Konum{" + "enlem=" + enlem + ", boylam=" + boylam + '}';
    }
}
