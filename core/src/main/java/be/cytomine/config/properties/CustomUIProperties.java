package be.cytomine.config.properties;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CustomUIProperties {

    CustomUIGlobalProperties global;

    CustomUIProjectProperties project;
}
