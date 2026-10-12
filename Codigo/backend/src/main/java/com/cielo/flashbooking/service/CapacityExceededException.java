package com.cielo.flashbooking.service;

public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException() {
        super("Requested quantity exceeds available event capacity");
    }
}
