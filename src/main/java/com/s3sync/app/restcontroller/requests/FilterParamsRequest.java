package com.s3sync.app.restcontroller.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class FilterParamsRequest {

    private final String name;
    private final String type;

}
