package com.s3sync.app.service;

import com.s3sync.app.entity.FileInfo;

import java.util.List;

public interface FileInfoFromDBService {

    List<FileInfo> findAll();

    List<FileInfo> searchBy(String name, String type);
}
