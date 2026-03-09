package be.cytomine.dto.annotation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record AnnotationReportParams(
    String format,
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<String> users,
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<String> reviewUsers,
    boolean reviewed,
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<String> terms,
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<String> images,
    Long beforeThan,
    Long afterThan
) {}
