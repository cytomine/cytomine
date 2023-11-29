package io.github.ya_b.registry.client.http.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DockerAuthResp {

    @SerializedName("token")
    private String token;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("expires_in")
    private Integer expiresIn;
    @SerializedName("issued_at")
    private String issuedAt;
}
