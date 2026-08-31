package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import be.cytomine.common.repository.model.HasTimestampCUD;
import be.cytomine.common.repository.utils.LTreeType;
import be.cytomine.common.repository.utils.LongArrayToBytesConverter;

@Entity(name = "uploaded_file")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadedFileEntity implements HasTimestampCUD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Version
    private long version = 0;

    @Column
    private Long userId;

    @Column
    private Long storageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private UploadedFileEntity parent;

    @Column
    private String filename;

    @Column
    private String originalFilename;

    @Column
    private String ext;

    @Column
    private String contentType;

    @Column
    private Long size;

    @Column
    private int status = 0;

    @Column
    private Timestamp created;

    @Column
    private Timestamp updated;

    @Column
    private Timestamp deleted;

    @Type(LTreeType.class)
    private String lTree;

    @Convert(converter = LongArrayToBytesConverter.class)
    private Long[] projects;

    @PreUpdate
    public void beforeUpdate() {
        updateLtree();
    }

    public void updateLtree() {
        lTree = parent != null ? parent.getLTree() + "." : "";
        lTree += id;
    }
}
