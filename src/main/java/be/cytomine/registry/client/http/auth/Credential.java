package io.github.ya_b.registry.client.http.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credential {

    private String username;

    private String password;

}
