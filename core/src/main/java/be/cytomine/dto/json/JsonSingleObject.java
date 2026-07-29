package be.cytomine.dto.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

import be.cytomine.utils.JsonObject;

@Data
@EqualsAndHashCode(callSuper = false)
public class JsonSingleObject extends JsonObject implements JsonInput {

}
