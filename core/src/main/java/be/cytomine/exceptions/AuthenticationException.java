package be.cytomine.exceptions;

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

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public AuthenticationException(String message) {
        this(message, new LinkedHashMap<Object, Object>());
    }

    public AuthenticationException(String message, Map<Object, Object> values) {
        super(message, 401, values);
        log.warn(message);
    }

}
