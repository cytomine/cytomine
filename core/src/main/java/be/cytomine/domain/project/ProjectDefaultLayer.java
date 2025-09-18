package be.cytomine.domain.project;

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

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.security.User;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class ProjectDefaultLayer extends CytomineDomain {

    boolean hideByDefault;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        ProjectDefaultLayer projectDefaultLayer = (ProjectDefaultLayer) domain;
        returnArray.put("project", projectDefaultLayer.getProject().getId());
        returnArray.put("user", projectDefaultLayer.getUser().getId());
        returnArray.put("hideByDefault", projectDefaultLayer.isHideByDefault());
        return returnArray;
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        ProjectDefaultLayer projectDefaultLayer = this;
        projectDefaultLayer.setId(json.getJSONAttrLong("id", null));
        projectDefaultLayer.setProject((Project) json.getJSONAttrDomain(entityManager, "project",
            new Project(), true));
        projectDefaultLayer.setUser((User) json.getJSONAttrDomain(entityManager, "user",
            new User(), true));
        projectDefaultLayer.setHideByDefault(json.getJSONAttrBoolean("hideByDefault", false));
        return projectDefaultLayer;
    }

    @Override
    public String toJSON() {
        return toJsonObject().toJsonString();
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return project.container();
    }
}
