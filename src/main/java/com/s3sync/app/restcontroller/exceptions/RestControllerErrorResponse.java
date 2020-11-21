package com.s3sync.app.restcontroller.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RestControllerErrorResponse {

    private final int status;
    private final String message;
    private final long timeStamp;

    public RestControllerErrorResponse(int status, String message, long timeStamp) {
        this.status = status;
        this.message = message;
        this.timeStamp = timeStamp;
    }

}
