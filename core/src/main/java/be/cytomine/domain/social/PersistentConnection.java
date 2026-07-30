package be.cytomine.domain.social;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import be.cytomine.domain.CytomineSocialDomain;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.utils.JsonObject;

@Getter
@Setter
@Document
public class PersistentConnection extends CytomineSocialDomain {

    protected Long id;

    @CreatedDate
    protected Date created;

    @LastModifiedDate
    protected Date updated;

    @NotNull
    protected Long user;

    private Long project;

    private String session;

    @Override
    public JsonObject toJsonObjectSocial() {
        throw new WrongArgumentException("getDataFromDomain is not implemented for this class");
    }

    @Override
    public String toString() {
        return "PersistentConnection{"
            + "id=" + id
            + ", created=" + created
            + ", updated=" + updated
            + ", user=" + user
            + ", project=" + project
            + ", session='" + session + '\''
            + '}';
    }
}
