package com.vivek.HospitalManagement.Exceptions;

public class SlotAlreadyBookedException extends RuntimeException{

    public SlotAlreadyBookedException(String message) {
        super(message);
    }
}
