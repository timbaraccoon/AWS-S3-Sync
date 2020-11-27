package com.s3sync.app.service;

import com.s3sync.app.dao.FileInfoRepository;
import com.s3sync.app.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileInfoFromDBServiceImpl implements FileInfoFromDBService {

    private final FileInfoRepository repository;

    @Autowired
    public FileInfoFromDBServiceImpl(FileInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<FileInfo> findAll() {
        return repository.findAll();
    }

    @Override
    public List<FileInfo> searchBy(String name, String type) {
        return repository.findByNameContainsAndTypeContainsAllIgnoreCase(name, type);
    }

    @Override
    public FileInfo findById(int id) {
        Optional<FileInfo> result = repository.findById(id);
        FileInfo fileInfo;

        if (result.isPresent()) {
            fileInfo = result.get();
        } else {
            throw new RuntimeException("Can't find Object with id: " + id);
        }
        return fileInfo;
    }

    @Override
    public List<FileInfo> findAllByLastModifiedOrder() {
        return repository.findAllByOrderByLastModifiedDesc();
    }

    @Override
    public List<FileInfo> findAllByNameOrder() {
        return repository.findAllByOrderByNameAsc();
    }
}
