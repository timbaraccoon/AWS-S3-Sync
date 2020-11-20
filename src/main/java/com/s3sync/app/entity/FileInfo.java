package com.s3sync.app.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "storage_class", nullable = false)
    private String storageClass;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (size != fileInfo.size) return false;
        if (!name.equals(fileInfo.name)) return false;
        if (!type.equals(fileInfo.type)) return false;
        if (!lastModified.equals(fileInfo.lastModified)) return false;
        return storageClass.equals(fileInfo.storageClass);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + lastModified.hashCode();
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + storageClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", lastModified=" + lastModified +
                ", size=" + size +
                ", storageClass='" + storageClass + '\'' +
                '}';
    }
}
