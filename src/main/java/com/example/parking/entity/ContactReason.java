package com.example.parking.entity;

public enum ContactReason {
    WRONG_PARKING("Noto'g'ri Parkovka"),
    BLOCKING_ROAD("Yo'lni To'sib Qo'ygan"),
    EMERGENCY("Favqulodda Holat"),
    OTHER("Boshqa Sabab");
    
    private final String description;
    
    ContactReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
