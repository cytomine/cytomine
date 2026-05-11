package be.cytomine.service.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.stereotype.Service;

import be.cytomine.domain.project.Project;
import be.cytomine.dto.annotation.AnnotationResult;
import be.cytomine.repository.AnnotationListing;
import be.cytomine.service.AnnotationListingService;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.JsonObject;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnnotationReportService {

    private final AnnotationListingBuilder annotationListingBuilder;

    private final AnnotationListingService annotationListingService;

    private final ProjectService projectService;

    private final TermService termService;

    public byte[] downloadDocumentByProject(JsonObject params, Project project) {

        Long idProject = params.getJSONAttrLong("project");
        boolean reviewed = params.getJSONAttrBoolean("reviewed", false);

        String usersParamName = reviewed ? "reviewUsers" : "users";
        Optional<String> requestedUsers = Optional.ofNullable(params.getJSONAttrStr(usersParamName));

        String terms = params.getJSONAttrStr("terms");
        String format = params.getJSONAttrStr("format");

        String userIds = requestedUsers
            .filter(s -> !s.isBlank())
            .orElseGet(() -> projectService.getUserIdsFromProject(project.getId()));

        terms = termService.fillEmptyTermIds(terms, project);

        if (reviewed) {
            params.put("reviewed", true);
        }

        log.info("Download report for project {} with users {} and terms {}", idProject, userIds, terms);

        return annotationListingBuilder.buildAnnotationReport(idProject, userIds, params, terms, format);
    }

    public Map<String, Object> exportAnnotations(Long projectId) {
        JsonObject params = JsonObject.of("project", projectId);
        params.put("showDefault", true);
        params.put("showWKT", true);
        params.put("showGIS", true);

        AnnotationListing userListing = annotationListingBuilder.buildAnnotationListing(params);
        List<AnnotationResult> userAnnotations = annotationListingService.listGeneric(userListing);

        JsonObject reviewedParams = new JsonObject(params);
        reviewedParams.put("reviewed", true);
        AnnotationListing reviewedListing = annotationListingBuilder.buildAnnotationListing(reviewedParams);
        List<AnnotationResult> reviewedAnnotations = annotationListingService.listGeneric(reviewedListing);

        GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
        geoJsonWriter.setEncodeCRS(false);
        WKTReader wktReader = new WKTReader();
        List<Map<String, Object>> features = new ArrayList<>();

        for (AnnotationResult annotation : userAnnotations) {
            toGeoJsonFeature(annotation, geoJsonWriter, wktReader).ifPresent(features::add);
        }
        for (AnnotationResult annotation : reviewedAnnotations) {
            toGeoJsonFeature(annotation, geoJsonWriter, wktReader).ifPresent(features::add);
        }

        return Map.of(
            "type", "FeatureCollection",
            "features", features
        );
    }

    private Optional<Map<String, Object>> toGeoJsonFeature(
        AnnotationResult annotation,
        GeoJsonWriter geoJsonWriter,
        WKTReader wktReader
    ) {
        Object location = annotation.get("location");
        if (location == null) {
            return Optional.empty();
        }

        String wkt = location.toString();
        try {
            Geometry geometry = wktReader.read(wkt);
            geometry.setSRID(0);
            Map<String, Object> geometryJson = JsonObject.toMap(geoJsonWriter.write(geometry));

            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            feature.put("geometry", geometryJson);

            return Optional.of(feature);
        } catch (ParseException e) {
            log.warn("Unable to parse WKT for annotation {}: {}", annotation.get("id"), e.getMessage());
            return Optional.empty();
        }
    }
}
