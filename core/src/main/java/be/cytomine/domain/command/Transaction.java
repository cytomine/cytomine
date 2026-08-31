package be.cytomine.domain.command;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class Transaction extends CytomineDomain {

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return CytomineDomain.getDataFromDomain(this);
    }
}
