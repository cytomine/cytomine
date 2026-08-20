package be.cytomine.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SliceCoordinate {

    Integer channel;

    Integer zStack;

    Integer time;

}
