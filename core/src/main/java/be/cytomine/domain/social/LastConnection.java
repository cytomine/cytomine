package be.cytomine.domain.social;

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

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import be.cytomine.domain.CytomineSocialDomain;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.utils.JsonObject;

@Getter
@Setter
@Document
/**
 * Info on last user connection on Cytomine
 * User x connect to poject y the 2013/01/01 at xxhyymin
 */
public class LastConnection extends CytomineSocialDomain {

    protected Long id;

    @CreatedDate
    protected Date created;

    @LastModifiedDate
    protected Date updated;

    protected Date date;

    protected Long user;

    private Long project;

    private Integer version = 0;

    @Override
    public JsonObject toJsonObject() {
        throw new WrongArgumentException("getDataFromDomain is not implemented for this class");
    }
}
