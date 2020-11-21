package com.s3sync.app.restcontroller.requests;

import lombok.Getter;

@Getter
public class FilterParamsRequest {

    private final String name;
    private final String type;

    public FilterParamsRequest(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
