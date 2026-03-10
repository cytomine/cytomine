package org.cytomine.common.repository.http.payload;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Set;

public record AnnotationReportParams(String format,
                                     @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) Set<String> users,
                                     @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) Set<String> reviewUsers,
                                     boolean reviewed,
                                     @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) Set<String> terms,
                                     @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) Set<String> images,
                                     Long beforeThan, Long afterThan


) {
    public AnnotationReportParams {
        if (users == null) users = Set.of();
        if (reviewUsers == null) reviewUsers = Set.of();
        if (terms == null) terms = Set.of();
        if (images == null) images = Set.of();
    }
}
