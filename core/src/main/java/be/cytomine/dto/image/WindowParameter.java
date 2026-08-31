package be.cytomine.dto.image;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WindowParameter {

    private String format;

    private int x;

    private int y;

    private int w;

    private int h;

    private boolean withExterior;

    private BoundariesCropParameter boundaries;

    private Boolean safe;

    private Integer maxSize;

    private Double gamma;

    private Integer bits;

    private Boolean maxBits;

    private Integer alpha;

    private Integer thickness;

    private Integer zoom;

    private String colormap;

    private Boolean inverse;

    private List<Map<String, Object>> geometries;

    private String color;

    private Boolean complete;

    private String type;

    private Boolean draw;

    private Boolean mask;

    private Boolean alphaMask;
}
