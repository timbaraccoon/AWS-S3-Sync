package com.s3sync.app.entity;

import com.amazonaws.services.s3.model.Grant;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "last_modified", nullable = false)
    private Date lastModified;

    @Column(name = "object_version", nullable = false, length = 50)
    private String objectVersion;

    @Column(name = "size", nullable = false)
    private long size;

    @ElementCollection
    @CollectionTable(name = "access")
    @OrderColumn
    @Column(name = "acl_grants", length = 2000)
    private List<Grant> aclGrants;

    @Column(name = "storage_class", nullable = false,  length = 50)
    private String storageClass;

    @Column(name = "confirmed")
    private boolean confirmed;

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

    public String getACLtoString() {
        StringBuilder sb = new StringBuilder();

        for (Grant grant : aclGrants) {
            sb.append(grant.getGrantee().getIdentifier())
                    .append(" : ")
                    .append(grant.getPermission())
                    .append("\n");
        }
        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }

}
