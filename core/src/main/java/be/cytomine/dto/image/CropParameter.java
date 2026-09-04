package be.cytomine.dto.image;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CropParameter {

    private String format;

    private String geometry;

    private String location;

    private Boolean complete;

    private Integer maxSize;

    private Integer zoom;

    private Double increaseArea;

    private Boolean safe;

    private Boolean square;

    private String type;

    private Boolean draw;

    private Boolean mask;

    private Boolean alphaMask;

    private Boolean drawScaleBar;

    private Double resolution;

    private Double magnification;

    private String colormap;

    private Boolean inverse;

    private Double contrast;

    private Double gamma;

    private Integer bits;

    private Boolean maxBits;

    private Integer alpha;

    private Integer thickness;

    private String color;

    private Integer jpegQuality;

    private BoundariesCropParameter boundaries;

}
