package be.cytomine.domain.social;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongodbLocation {
    List<List<Double>> coordinates;
    String type;
}
