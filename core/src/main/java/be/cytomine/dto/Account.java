package be.cytomine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {
    @JsonProperty("user")
    private Long userId;
    private String reference;
    private String username;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("first_name")
    private String firstName;
    private String password;
    private String email;
    @JsonProperty("email_verified")
    private boolean emailVerified;
    @JsonProperty("is_developer")
    private boolean isDeveloper;
    @JsonProperty("locale")
    private String userLocale = "";
    @JsonProperty("created_at")
    private Long createdAt;
    private List<String> roles;
}
