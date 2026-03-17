package be.cytomine.domain.security;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class SecRole extends CytomineDomain implements Serializable {

    @NotNull
    @NotBlank
    @Column(unique = true)
    private String authority;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        SecRole role = (SecRole)domain;
        returnArray.put("authority", role.authority);
        return returnArray;
    }

    @Override
    public String toJSON() {
        return getDataFromDomain(this).toJsonString();
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SecRole secRole = (SecRole) o;
        return Objects.equals(authority, secRole.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authority);
    }
}
