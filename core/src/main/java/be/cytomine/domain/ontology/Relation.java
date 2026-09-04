package be.cytomine.domain.ontology;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class Relation extends CytomineDomain {

    @NotNull
    @NotBlank
    String name;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        Term term = (Term) domain;
        returnArray.put("name", term.getName());
        return returnArray;
    }

    @Override
    public String toJSON(UrlApi urlApi) {
        return getDataFromDomain(this).toJsonString();
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }
}
