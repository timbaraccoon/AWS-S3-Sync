package com.s3sync.app.dao;

import com.s3sync.app.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface FileInfoRepository extends JpaRepository<FileInfo, Integer> {

    List<FileInfo> findByNameContainsAndTypeContainsAllIgnoreCase(String name, String type);

    @Query("select coalesce(max(el.lastModified), 1) from FileInfo el")
    Date getMaxLastModified();

    @Transactional
    @Modifying
    @Query("update FileInfo set confirmed = :confirmed")
    void updateAllToConfirmed(@Param("confirmed") boolean confirmed);

    @Transactional
    @Modifying
    @Query("update FileInfo set confirmed = :confirmed where objectVersion in :objectVersions")
    void updateAllInSetToConfirmed(@Param("confirmed") boolean confirmed, Collection<String> objectVersions);

    @Transactional
    long deleteByConfirmed(boolean confirmed);

    List<FileInfo> findAllByOrderByLastModifiedDesc();

    List<FileInfo> findAllByOrderByNameAsc();

}
