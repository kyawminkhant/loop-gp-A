package model;

public class StockRecord {
    private String location;
    private int monday;
    private int tuesday;
    private int wednesday;
    private int thursday;
    private int friday;

    public StockRecord(String location, int mon, int tue, int wed, int thu, int fri) {
        this.location = location;
        this.monday = mon;
        this.tuesday = tue;
        this.wednesday = wed;
        this.thursday = thu;
        this.friday = fri;
    }

    public String getLocation() { return location; }
    public int getMonday() { return monday; }
    public int getTuesday() { return tuesday; }
    public int getWednesday() { return wednesday; }
    public int getThursday() { return thursday; }
    public int getFriday() { return friday; }
}