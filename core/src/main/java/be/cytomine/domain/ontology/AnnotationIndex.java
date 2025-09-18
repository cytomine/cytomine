package be.cytomine.domain.ontology;

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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.security.User;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class AnnotationIndex {

    @Version
    protected Integer version = 0;
    @Id
    @GeneratedValue(generator = "myGenerator")
    Long id;
    Long countAnnotation;
    Long countReviewedAnnotation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slice_id", nullable = true)
    private SliceInstance slice;

    public static JsonObject getDataFromDomain(AnnotationIndex index) {
        JsonObject returnArray = new JsonObject();
        returnArray.put("user", index.getUser() != null ? index.getUser().getId() : null);
        returnArray.put("slice", index.getSlice() != null ? index.getSlice().getId() : null);
        returnArray.put("countAnnotation", index.getCountAnnotation());
        returnArray.put("countReviewedAnnotation", index.getCountReviewedAnnotation());
        return returnArray;
    }
}
