package be.cytomine.dto.image;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Point {
    Double x;
    Double y;

    public List<Double> toList() {
        return List.of(x, y);
    }
}
