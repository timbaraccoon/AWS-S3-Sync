package com.s3sync.app.restcontroller;

import com.s3sync.app.entity.FileInfo;
import com.s3sync.app.restcontroller.requests.FilterParamsRequest;
import com.s3sync.app.service.FileInfoFromDBService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"Rest Controller for getting info from AWS S3 Bucket"})
@RestController
@RequestMapping("/rest")
public class RestFileInfoController {

    private final FileInfoFromDBService service;

    @Autowired
    public RestFileInfoController(FileInfoFromDBService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public List<FileInfo> getFileInfoList() {
        return service.findAll();
    }


    @GetMapping("/list/search")
    public List<FileInfo> getFilterFileInfoList(@RequestBody FilterParamsRequest filterParams) {

        String name = filterParams.getName();
        String type = filterParams.getType();

        if (name.trim().isEmpty() && type.trim().isEmpty()) {
            return service.findAll();
        }
        else {
            return service.searchBy(name, type);
        }
    }
}
