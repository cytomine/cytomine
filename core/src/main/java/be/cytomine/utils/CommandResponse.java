package be.cytomine.utils;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;

@Getter
@Setter
public class CommandResponse {

    Integer status;

    CytomineDomain object;

    Map<String, Object> data;

    public org.cytomine.common.repository.utils.JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    public static org.cytomine.common.repository.utils.JsonObject getDataFromDomain(CommandResponse domain) {
       org.cytomine.common.repository.utils.JsonObject jsonObject = new org.cytomine.common.repository.utils.JsonObject();
       jsonObject.put("status", domain.getStatus());
       jsonObject.put("object", domain.getObject().toJsonObject());
       jsonObject.put("data", domain.getData());
       return jsonObject;
    }

}
