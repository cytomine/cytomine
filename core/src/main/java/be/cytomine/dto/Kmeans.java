package be.cytomine.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Kmeans {

    Long id;

    String location;

    List term = new ArrayList();

    Long count;

    Double ratio;
}
