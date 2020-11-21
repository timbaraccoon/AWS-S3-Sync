package com.s3sync.app.restcontroller.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class RestControllerErrorResponse {

    private final int status;
    private final String message;
    private final long timeStamp;

}
