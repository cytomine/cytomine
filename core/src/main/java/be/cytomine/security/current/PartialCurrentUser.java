package be.cytomine.security.current;

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

import lombok.Data;

import be.cytomine.domain.security.User;

@Data
public class PartialCurrentUser implements CurrentUser {

    String username;

    @Override
    public boolean isFullObjectProvided() {
        return false;
    }

    @Override
    public boolean isUsernameProvided() {
        return username != null;
    }

    @Override
    public User getUser() {
        User user = new User();
        user.setUsername(username);
        return user;
    }
}
