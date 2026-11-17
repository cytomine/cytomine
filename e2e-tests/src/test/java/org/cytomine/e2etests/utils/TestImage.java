package org.cytomine.e2etests.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TestImage {
    SVS("images/CMU-1-Small-Region.svs", "svs");

    private final String resourcePath;
    private final String extension;
}
