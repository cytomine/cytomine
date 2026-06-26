package org.cytomine.repository.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "sec_user")
@Data
public class UserEntity {
    @Id
    private Long id;

    @Column
    private String firstname;

    @Column
    private String lastname;
}
