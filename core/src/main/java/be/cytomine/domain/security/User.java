package be.cytomine.domain.security;

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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;

@Table(name = "sec_user", uniqueConstraints = @UniqueConstraint(name = "unique_reference", columnNames = {"reference"}))
@Entity
@Getter
@Setter
public class User extends CytomineDomain {

    @NotNull
    @NotBlank
    @Column(nullable = false)
    protected String username;

    @NotNull
    @Column(nullable = false)
    protected String reference;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    protected String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "sec_user_sec_role",
        joinColumns = {@JoinColumn(name = "sec_user_id")},
        inverseJoinColumns = {@JoinColumn(name = "sec_role_id")}
    )
    private Set<SecRole> roles = new HashSet<>();

    /** Deprecated attributes. Kept here for migration **/
    @Deprecated
    protected String firstname;

    @Deprecated
    protected String lastname;

    @Deprecated
    protected String email;

    @Deprecated
    protected Language language;

    @Deprecated
    protected Boolean isDeveloper = false;

    @Deprecated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    protected User creator;

    @Deprecated
    protected String password;

    @Deprecated
    protected Boolean enabled = true;

    @Deprecated
    protected Boolean accountExpired = false;

    @Deprecated
    protected Boolean accountLocked = false;

    @Deprecated
    protected Boolean passwordExpired = false;

    @Deprecated
    protected String origin;

    /** Deprecated API keys. Will be removed in a future release **/
    @Deprecated
    protected String publicKey;

    @Deprecated
    protected String privateKey;

    @Deprecated
    public void generateKeys() {
        String privateKey = UUID.randomUUID().toString();
        String publicKey = UUID.randomUUID().toString();
        this.setPrivateKey(privateKey);
        this.setPublicKey(publicKey);
    }

    /****************************************************/

    public String getFullName() {
        if (name.equals(getUsername())) {
            return getUsername();
        }
        return name + " (" + getUsername() + ")";
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public String getReference() {
        return reference.toString();
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        User user = (User) this;
        user.id = json.getJSONAttrLong("id", null);
        user.username = json.getJSONAttrStr("username");
        user.name = json.getJSONAttrStr("name");
        user.email = json.getJSONAttrStr("email");
        user.firstname = json.getJSONAttrStr("firstname");
        user.lastname = json.getJSONAttrStr("lastname");
        user.created = json.getJSONAttrDate("created");
        user.updated = json.getJSONAttrDate("updated");
        String languageCode = json.getJSONAttrStr("language");
        if (languageCode == null || languageCode.isBlank()) {
            languageCode = Language.ENGLISH.toString();
        }

        user.language = Language.findByCode(languageCode.toUpperCase());
        if (user.language == null) {
            user.language = Language.ENGLISH;
        }
        user.setIsDeveloper(json.getJSONAttrBoolean("isDeveloper", false));
        user.reference = json.getJSONAttrStr("reference");
        return user;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        User user = (User) domain;

        JsonObject json = new JsonObject();
        json.put("id", user.getId());
        json.put("username", user.getUsername());
        json.put("name", user.getName());
        json.put("email", user.getEmail());
        json.put("fullName", user.getFullName());
        json.put("firstname", user.getFirstname());
        json.put("lastname", user.getLastname());
        json.put("language", Objects.nonNull(user.language) ? user.language.toString() : "EN");
        json.put("isDeveloper", user.isDeveloper);
        json.put("created", user.getCreated());
        json.put("updated", user.getUpdated());
        return json;
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
    public User userDomainCreator() {
        return this;
    }
}
