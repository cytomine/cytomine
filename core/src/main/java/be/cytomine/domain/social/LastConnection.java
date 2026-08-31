package be.cytomine.domain.social;

import java.util.Date;

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
/**
 * Info on last user connection on Cytomine
 * User x connect to poject y the 2013/01/01 at xxhyymin
 */
public class LastConnection extends CytomineSocialDomain {

    protected Long id;

    @CreatedDate
    protected Date created;

    @LastModifiedDate
    protected Date updated;

    protected Date date;

    protected Long user;

    private Long project;

    private Integer version = 0;

    @Override
    public JsonObject toJsonObjectSocial() {
        throw new WrongArgumentException("getDataFromDomain is not implemented for this class");
    }
}
