package be.cytomine.dto.image;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SliceCoordinates {

    List<Integer> channels;

    List<Integer> zStacks;

    List<Integer> times;

}
