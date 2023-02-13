package com.example;

public class Data {

    private int yr;
    private int month;
    private double rla;

    public Data(int yr, int month, double rla) {
        this.yr = yr;
        this.month = month;
        this.rla = rla;
    }

    public Data(int yr, int month, String rla) {
        this.yr = yr;
        this.month = month;
        this.rla = Double.parseDouble(rla);
    }

    public int getYr() {
        return yr;
    }

    public int getMonth() {
        return month;
    }

    public double getRla() {
        return rla;
    }

    public void setRla(double rla) {
        this.rla = rla;
    }
}
