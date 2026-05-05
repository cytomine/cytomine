package org.cytomine.e2etests.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    PDF("pdf"),
    CSV("csv"),
    Excel("xls"),
    GEOJSON("geojson");

    private final String label;
}
