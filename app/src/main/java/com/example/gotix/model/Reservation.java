package com.example.gotix.model;

public class Reservation {
    private String id;
    private String eventId;
    private String userId;
    private String eventTitle;
    private int numTickets;
    private String status; // CONFIRMED, CANCELLED
    private long reservedAt;

    public Reservation(String id, String eventId, String userId, String eventTitle, int numTickets) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.eventTitle = eventTitle;
        this.numTickets = numTickets;
        this.status = "CONFIRMED";
        this.reservedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public String getEventTitle() { return eventTitle; }
    public int getNumTickets() { return numTickets; }
    public String getStatus() { return status; }
    public long getReservedAt() { return reservedAt; }
    public void setStatus(String status) { this.status = status; }
}
