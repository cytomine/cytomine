package org.cytomine.repository.persistence.entity;


import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import be.cytomine.common.repository.model.command.CommandV2Request;

@Data
@Entity(name = "command_v2")
public class CommandV2Entity {
    @Id
    @Generated
    @Column
    private UUID id;

    @Column
    private ZonedDateTime created;

    @Column
    private ZonedDateTime updated;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private CommandV2Request<?> data;

    @Column
    private long userId;
}
