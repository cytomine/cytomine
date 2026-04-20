package be.cytomine.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
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
