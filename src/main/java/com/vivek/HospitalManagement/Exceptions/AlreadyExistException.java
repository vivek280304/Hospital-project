package com.vivek.HospitalManagement.Exceptions;

public class AlreadyExistException extends RuntimeException{

    public AlreadyExistException(String message) {
        super(message);
    }
}
