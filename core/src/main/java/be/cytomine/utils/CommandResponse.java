package be.cytomine.utils;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.service.UrlApi;

@Getter
@Setter
public class CommandResponse {

    Integer status;

    CytomineDomain object;

    Map<String, Object> data;

    public static JsonObject getDataFromDomain(CommandResponse domain, UrlApi urlApi) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("status", domain.getStatus());
        jsonObject.put("object", domain.getObject().toJsonObject(urlApi));
        jsonObject.put("data", domain.getData());
        return jsonObject;
    }

    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this, urlApi);
    }

}
