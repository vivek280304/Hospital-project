package com.vivek.HospitalManagement.DTO.ExceptionsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {


    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
