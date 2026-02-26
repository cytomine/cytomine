package be.cytomine.domain;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import be.cytomine.domain.security.User;
import be.cytomine.utils.DateUtils;
import be.cytomine.utils.JsonObject;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EntityManager;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;
import java.util.stream.Collectors;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CytomineDomain {

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
        if (this == o) return true;
        if (o == null) { //we do not compare class type as hibernate proxy is a different class
            return false;
        }
        CytomineDomain that = (CytomineDomain) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
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

    public List<ValidationError> validate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CytomineDomain>> violations = validator.validate(this);
        return violations.stream().map(ValidationError::new).collect(Collectors.toList());
    }

    /**
     * Get the container domain for this domain (usefull for security)
     *
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return null;
    }

    public User userDomainCreator() {
        return null;
    }

    public abstract JsonObject toJsonObject();

    public Map<String, Object> getCallBack() {
        return Map.of();
    }

    public String toJSON() {
        return toJsonObject().toJsonString();
    }

    public boolean canUpdateContent() {
        return true;
    }

    public abstract Long getId();

    public abstract void setId(Long id);

    public abstract String toString();
}
