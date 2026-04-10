package com.example.miniproject1004.models;

public class Seat {
    private String id;
    private boolean isBooked;
    private boolean isSelected;

    public Seat(String id, boolean isBooked) {
        this.id = id;
        this.isBooked = isBooked;
        this.isSelected = false;
    }

    public String getId() { return id; }
    public boolean isBooked() { return isBooked; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}