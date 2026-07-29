package be.cytomine.dto.image;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageParameter {

    private String format;

    private Integer maxSize;

    private String colormap;

    private Boolean inverse;

    private Double contrast;

    private Double gamma;

    private Integer bits;

    private Boolean maxBits;

    private Boolean refresh;
}
