package com.example.finguard.domain;

/**
 * Evento que contiene información para actualizar características de cliente.
 */
public class CustomerEvent {
    private String customerId;
    private String eventType;
    private Object eventData;
    private long timestamp;

    // Constructores
    public CustomerEvent() {}

    public CustomerEvent(String customerId, String eventType, Object eventData, long timestamp) {
        this.customerId = customerId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Object getEventData() { return eventData; }
    public void setEventData(Object eventData) { this.eventData = eventData; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
