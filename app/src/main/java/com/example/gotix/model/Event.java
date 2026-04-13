package com.example.gotix.model;

public class Event {
    private String id;
    private String title;
    private String date;
    private String location;
    private String category;
    private int totalSeats;
    private int availableSeats;

    public Event(String id, String title, String date, String location, String category, int totalSeats) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.category = category;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
