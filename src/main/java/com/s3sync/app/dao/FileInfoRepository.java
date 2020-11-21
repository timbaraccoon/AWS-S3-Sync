package com.s3sync.app.dao;

import com.s3sync.app.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileInfoRepository extends JpaRepository<FileInfo, Integer> {

    List<FileInfo> findByNameContainsAndTypeContainsAllIgnoreCase(
                                        String name, String type);
}
