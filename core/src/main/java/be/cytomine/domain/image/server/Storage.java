package be.cytomine.domain.image.server;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.security.User;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class Storage extends CytomineDomain {

    @NotNull
    @NotBlank
    @Column(nullable = false)
    protected String name;

    @Column(name = "user_id")
    protected Long userId;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        Storage storage = (Storage) domain;
        returnArray.put("name", storage.getName());
        returnArray.put("user", (storage.getUserId()));
        return returnArray;
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        Storage storage = (Storage) this;
        storage.id = json.getJSONAttrLong("id", null);
        storage.name = json.getJSONAttrStr("name", true);
        storage.userId = json.getJSONAttrDomain(entityManager, "user", new User(), true).getId();
        storage.created = json.getJSONAttrDate("created");
        storage.updated = json.getJSONAttrDate("updated");
        return storage;
    }

    @Override
    public String toJSON(UrlApi urlApi) {
        return getDataFromDomain(this).toJsonString();
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return this;
    }
}
