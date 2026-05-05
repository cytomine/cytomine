package be.cytomine.utils;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    PDF("pdf"),
    CSV("csv"),
    EXCEL("xls");

    private final String label;

    public static ReportType fromLabel(String label) {
        return Arrays.stream(values())
            .filter(t -> t.label.equalsIgnoreCase(label))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unsupported format: " + label));
    }
}
