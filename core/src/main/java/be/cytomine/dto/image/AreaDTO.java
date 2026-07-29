package be.cytomine.dto.image;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import be.cytomine.domain.social.MongodbLocation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AreaDTO {

    Point topLeft;
    Point topRight;
    Point bottomRight;
    Point bottomLeftX;

    public List<List<Double>> toList() {
        return List.of(topLeft.toList(), topRight.toList(), bottomRight.toList(), bottomLeftX.toList());
    }

    public List<Point> toPointList() {
        return List.of(topLeft, topRight, bottomRight, bottomLeftX);
    }

    public MongodbLocation toMongodbLocation() {
        MongodbLocation location = new MongodbLocation();
        location.setType("polygon");
        location.setCoordinates(toPointList().stream()
            .map(point -> List.of(point.getX(), point.getY()))
            .collect(Collectors.toList()));
        return location;
    }
}
