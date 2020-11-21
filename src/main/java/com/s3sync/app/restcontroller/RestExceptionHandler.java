package com.s3sync.app.restcontroller;

import com.s3sync.app.restcontroller.exceptions.RestControllerErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<RestControllerErrorResponse> handleException(Exception otherException) {
        RestControllerErrorResponse errorResponse = new RestControllerErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                otherException.getMessage(),
                System.currentTimeMillis());

        return new ResponseEntity<RestControllerErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
