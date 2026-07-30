package be.cytomine.domain.security;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class SecUserSecRole extends CytomineDomain implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sec_user_id", nullable = false)
    private User secUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sec_role_id", nullable = false)
    private SecRole secRole;

    Long getUserId() {
        return (secUser != null ? secUser.getId() : null);
    }

    Long getSecRoleId() {
        return (secRole != null ? secRole.getId() : null);
    }

    String getSecRoleAuthority() {
        return (secRole != null ? secRole.getAuthority() : null);
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        SecUserSecRole secSecUserSecRole = this;
        secSecUserSecRole.id = json.getJSONAttrLong("id", null);
        secSecUserSecRole.secUser = (User) json.getJSONAttrDomain(entityManager, "user", new User(), true);
        secSecUserSecRole.secRole = (SecRole) json.getJSONAttrDomain(entityManager, "role", new SecRole(), true);
        secSecUserSecRole.created = json.getJSONAttrDate("created");
        secSecUserSecRole.updated = json.getJSONAttrDate("updated");
        return secSecUserSecRole;
    }

    /**
     * Define fields available for JSON response
     *
     * @param domain Domain source for json value
     *
     * @return Map with fields (keys) and their values
     */
    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        SecUserSecRole secSecUserSecRole = (SecUserSecRole) domain;
        returnArray.put("user", secSecUserSecRole.getUserId());
        returnArray.put("role", secSecUserSecRole.getSecRoleId());
        returnArray.put("authority", secSecUserSecRole.getSecRoleAuthority());
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
