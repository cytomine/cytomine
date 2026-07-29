package be.cytomine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewedAnnotationStatsEntry {
    private Long user;
    private Long all;
    private Long reviewed;
}
