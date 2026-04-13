package be.cytomine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Accounts {
    @JsonProperty("total")
    private int total;
    @JsonProperty("nbPages")
    private int nbPages;
    @JsonProperty("offset")
    private int offset;
    @JsonProperty("limit")
    private int limit;
    private List<Account> items;
}
