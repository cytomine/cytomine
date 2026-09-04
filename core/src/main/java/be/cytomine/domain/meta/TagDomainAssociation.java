package be.cytomine.domain.meta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.GenericCytomineDomainContainer;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class TagDomainAssociation extends CytomineDomain {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id")
    protected Tag tag;

    @NotNull
    @NotBlank
    private String domainClassName;

    @NotNull
    @Column(name = "domain_id")
    private Long domainIdent;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        TagDomainAssociation tagDomainAssocitation = (TagDomainAssociation) domain;
        returnArray.put("domainIdent", tagDomainAssocitation.getDomainIdent());
        returnArray.put("domainClassName", tagDomainAssocitation.getDomainClassName());
        returnArray.put("tag", tagDomainAssocitation.getTag().getId());
        returnArray.put("tagName", tagDomainAssocitation.getTag().getName());
        return returnArray;
    }

    public void setDomain(CytomineDomain domain) {
        domainClassName = domain.getClass().getName();
        domainIdent = domain.getId();
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        TagDomainAssociation tagDomainAssocitation = (TagDomainAssociation) this;
        tagDomainAssocitation.setId(json.getJSONAttrLong("id", null));
        tagDomainAssocitation.setDomainIdent(json.getJSONAttrLong("domainIdent", -1L));
        tagDomainAssocitation.setDomainClassName(json.getJSONAttrStr("domainClassName"));
        tagDomainAssocitation.setTag((Tag) json.getJSONAttrDomain(entityManager, "tag", new Tag(), true));
        return tagDomainAssocitation;
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        GenericCytomineDomainContainer genericCytomineDomainContainer = new GenericCytomineDomainContainer();
        genericCytomineDomainContainer.setId(domainIdent);
        genericCytomineDomainContainer.setContainerClass(domainClassName);
        return genericCytomineDomainContainer;
    }

}
