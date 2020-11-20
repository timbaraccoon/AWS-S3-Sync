package com.s3sync.app.controller;

import com.s3sync.app.entity.FileInfo;
import com.s3sync.app.service.FileInfoFromDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api")
public class FileInfoController {

    private final FileInfoFromDBService service;

    @Autowired
    public FileInfoController(FileInfoFromDBService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String getListEmployees(Model model) {
        List<FileInfo> fileInfoList = service.findAll();
        model.addAttribute("fileInfoList", fileInfoList);

        return "api/list";
    }

    @GetMapping("/search")
    public String search(@RequestParam("name") String name,
                         @RequestParam("type") String type,
                         Model theModel) {

        if (name.trim().isEmpty() && type.trim().isEmpty()) {
            return "redirect:/api/list";
        }
        else {
            List<FileInfo> filterFileInfoList = service.searchBy(name, type);
            theModel.addAttribute("fileInfoList", filterFileInfoList);

            return "api/list";
        }
    }
}
