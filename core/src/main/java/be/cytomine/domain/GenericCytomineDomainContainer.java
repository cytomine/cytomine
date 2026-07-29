package be.cytomine.domain;

import lombok.Getter;
import lombok.Setter;

import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

/**
 * When a domain has not a real reference to its container (e.g. description.domainIdent + class), we fill this object
 * with id/class. When we perform an ACL, there is a special case to load the object from the database before calling
 * its .container(). This is a hack because we cannot load the object directly from the DOMAIN.container() method
 */
@Getter
@Setter
public class GenericCytomineDomainContainer extends CytomineDomain {

    private String containerClass;

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return null;
    }

    @Override
    public String toString() {
        return "GenericCytomineDomainContainer{"
            + "id=" + id
            + ", containerClass='" + containerClass + '\''
            + '}';
    }

    public CytomineDomain container() {
        return this;
    }


}
