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
@ToString
public class Account {
    private Long userId;
    private String reference;
    private String username;
    private String lastName;
    private String firstName;
    private String password;
    private String email;
    private boolean emailVerified;
    private boolean isDeveloper;
    private String userLocale = "";
    private Long createdAt;
    private List<String> roles;
}
