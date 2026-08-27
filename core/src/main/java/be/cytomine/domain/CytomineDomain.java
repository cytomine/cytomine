package be.cytomine.domain;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import be.cytomine.service.UrlApi;
import be.cytomine.utils.DateUtils;
import be.cytomine.utils.JsonObject;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CytomineDomain {

    @GenericGenerator(
        name = "myGenerator",
        type = be.cytomine.config.CustomIdentifierGenerator.class,
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "hibernate_sequence"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "myGenerator")
    @Id
    protected Long id;

    @CreatedDate
    protected Date created;

    @LastModifiedDate
    protected Date updated;

    @Version
    protected Integer version = 0;

    public CytomineDomain() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) { //we do not compare class type as hibernate proxy is a different class
            return false;
        }
        CytomineDomain that = (CytomineDomain) o;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject jsonObject = new JsonObject();
        if (domain != null) {
            jsonObject.put("class", domain.getClass());
            jsonObject.put("id", domain.getId());
            jsonObject.put("created", DateUtils.getTimeToString(domain.created));
            jsonObject.put("updated", DateUtils.getTimeToString(domain.updated));
        }
        return jsonObject;
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        return null;
    }

    public CytomineDomain container() {
        return null;
    }

    public Long userDomainCreator() {
        return null;
    }

    public abstract JsonObject toJsonObject(UrlApi urlApi);

    public Map<String, Object> getCallBack() {
        return Map.of();
    }

    public String toJSON(UrlApi urlApi) {
        return toJsonObject(urlApi).toJsonString();
    }

    public boolean canUpdateContent() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{id=" + id + "}";
    }
}
