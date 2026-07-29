package be.cytomine.dto.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

@Getter
@Setter
@AllArgsConstructor
public class SimplifiedAnnotation {

    Geometry newAnnotation;

    Double rate;
}
