package be.cytomine.dto.annotation;

public record ImageAnnotation(
    String imageAlias,
    String annotationAlias,
    String annotatorAlias,
    String filename,
    byte[] geojson,
    String checksum,
    String dateOfCreation
) {}
