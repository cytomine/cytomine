package be.cytomine.dto.json;

import java.util.ArrayList;

import lombok.Data;
import lombok.EqualsAndHashCode;

import be.cytomine.utils.JsonObject;

@Data
@EqualsAndHashCode(callSuper = false)
public class JsonMultipleObject extends ArrayList<JsonObject> implements JsonInput {

}
