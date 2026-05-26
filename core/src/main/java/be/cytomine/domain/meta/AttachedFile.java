package be.cytomine.domain.meta;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.GenericCytomineDomainContainer;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class AttachedFile extends CytomineDomain {

    byte[] data;

    @NotNull
    @NotBlank
    private String domainClassName;

    @NotNull
    private Long domainIdent;

    @NotBlank
    private String filename;

    private String key;

    /**
     * Set annotation (storing class + id) With groovy, you can do: this.annotation = ...
     *
     * @param domain to add
     */
    public void setDomain(CytomineDomain domain) {
        domainClassName = domain.getClass().getName();
        domainIdent = domain.getId();
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        AttachedFile property = (AttachedFile) domain;
        returnArray.put("domainIdent", property.getDomainIdent());
        returnArray.put("domainClassName", property.getDomainClassName());
        returnArray.put("url", "/api/attachedfile/" + domain.getId() + "/download");
        returnArray.put("filename", property.getFilename());
        returnArray.put("key", property.getKey());
        return returnArray;
    }

    @Override
    public String toJSON() {
        return toJsonObject().toJsonString();
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        GenericCytomineDomainContainer genericCytomineDomainContainer = new GenericCytomineDomainContainer();
        genericCytomineDomainContainer.setId(domainIdent);
        genericCytomineDomainContainer.setContainerClass(domainClassName);
        return genericCytomineDomainContainer;
    }
}
