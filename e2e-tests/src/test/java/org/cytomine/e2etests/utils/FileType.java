package org.cytomine.e2etests.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
    PDF("pdf"),
    CSV("csv"),
    Excel("xls");

    private final String label;
}
