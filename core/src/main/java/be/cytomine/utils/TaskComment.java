package be.cytomine.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskComment {

    private Long taskIdent;

    private String comment;

    private Long timestamp;
}
