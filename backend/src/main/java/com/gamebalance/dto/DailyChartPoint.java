package com.gamebalance.dto;

public class DailyChartPoint {
    private String date;
    private String label;
    private double value;
    private double secondaryValue;

    public DailyChartPoint() {
    }

    public DailyChartPoint(String date, String label, double value) {
        this.date = date;
        this.label = label;
        this.value = value;
    }

    public DailyChartPoint(String date, String label, double value, double secondaryValue) {
        this.date = date;
        this.label = label;
        this.value = value;
        this.secondaryValue = secondaryValue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(double secondaryValue) {
        this.secondaryValue = secondaryValue;
    }
}
