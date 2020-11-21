package com.s3sync.app.controller;

import com.s3sync.app.entity.FileInfo;
import com.s3sync.app.service.FileInfoFromDBService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Api(tags = {"Controller for mapping Info from AWS S3 Bucket to HTML"})
@Controller
@RequestMapping("/api")
public class FileInfoController {

    private final FileInfoFromDBService serviceDB;

    @Autowired
    public FileInfoController(FileInfoFromDBService serviceDB) {
        this.serviceDB = serviceDB;
    }

    @GetMapping("/list")
    public String getFileInfoList(Model model) {
        List<FileInfo> fileInfoList = serviceDB.findAll();
        model.addAttribute("fileInfoList", fileInfoList);

        return "api/list";
    }

    @GetMapping("/search")
    public String getFilterFileInfoList(@RequestParam("name") String name,
                         @RequestParam("type") String type,
                         Model theModel) {

        if (StringUtils.hasText(name) && StringUtils.hasText(type)) {
            List<FileInfo> filterFileInfoList = serviceDB.searchBy(name, type);
            theModel.addAttribute("fileInfoList", filterFileInfoList);

            return "api/list";
        }
        else {
            return "redirect:/api/list";
        }
    }
}
