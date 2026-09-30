package be.cytomine.service.annotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.annotation.AnnotationResult;
import be.cytomine.dto.annotation.ImageAnnotation;
import be.cytomine.repository.AnnotationListing;
import be.cytomine.service.AnnotationListingService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.JsonObject;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnnotationBundleService {

    private static final String ROOT = "dataset";

    private static final String ALIAS_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int ALIAS_LENGTH = 10;

    private final GeoJsonWriter geoJsonWriter;

    private final WKTReader wktReader;

    private final AnnotationListingBuilder annotationListingBuilder;

    private final AnnotationListingService annotationListingService;

    private final TermHttpContract termHttpContract;

    private final UserHttpContract userHttpContract;

    public byte[] buildBundle(Project project) {
        Long projectId = project.getId();

        List<TermResponse> terms = termHttpContract.findTermsByProject(projectId, Pageable.unpaged()).stream().toList();
        Map<Long, String> termNames = terms.stream()
            .collect(Collectors.toMap(TermResponse::id, TermResponse::name, (a, b) -> a));

        List<AnnotationResult> annotations = fetchAnnotations(projectId);

        // Group annotations by image: one GeoJSON file (and one ANNOTATION entry) per image.
        Map<Long, List<AnnotationResult>> annotationsByImage = new LinkedHashMap<>();
        for (AnnotationResult annotation : annotations) {
            Long imageId = asLong(annotation.get("image"));
            if (imageId == null) {
                continue;
            }
            annotationsByImage.computeIfAbsent(imageId, key -> new ArrayList<>()).add(annotation);
        }

        String datasetAlias = alias("DATASET");
        String ontologyAlias = alias("ONTOLOGY");
        String taskAlias = alias("ANNOTATION_TASK");
        String policyAlias = alias("POLICY");

        Map<Long, String> annotatorAliasByUser = new LinkedHashMap<>();
        List<ImageAnnotation> imageAnnotations = new ArrayList<>();

        for (Map.Entry<Long, List<AnnotationResult>> entry : annotationsByImage.entrySet()) {
            List<AnnotationResult> imageAnnotationResults = entry.getValue();
            AnnotationResult representative = imageAnnotationResults.getFirst();

            Long userId = asLong(representative.get("user"));
            String annotatorAlias = annotatorAliasByUser.computeIfAbsent(
                userId == null ? -1L : userId,
                key -> alias("ANNOTATOR")
            );

            LocalDateTime created = toLocalDateTime(representative.get("created"));
            String imageAlias = alias("IMAGE");
            String annotationAlias = alias("ANNOTATION");
            String filename = imageAlias + "_" + created.format(DateTimeFormatter.BASIC_ISO_DATE) + ".geojson";

            byte[] geojson = buildFeatureCollection(
                imageAnnotationResults,
                termNames,
                created.format(DateTimeFormatter.BASIC_ISO_DATE)
            );

            imageAnnotations.add(new ImageAnnotation(
                imageAlias,
                annotationAlias,
                annotatorAlias,
                filename,
                geojson,
                sha256(geojson),
                created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
        }

        Map<Long, String> annotatorDescriptions = resolveAnnotatorDescriptions(annotatorAliasByUser.keySet());

        String ontologyName = project.getOntology() != null ? project.getOntology().getName() : "ontology";
        String ontologyFileName = sanitizeFilename(ontologyName) + ".json";
        byte[] ontologyJson = buildOntologyJson(terms);

        String taskText = buildTaskText();
        byte[] taskBytes = taskText.getBytes(StandardCharsets.UTF_8);

        String annotationXml = buildAnnotationXml(imageAnnotations, ontologyAlias, taskAlias);
        String datasetXml = buildDatasetXml(project, datasetAlias, imageAnnotations);
        String annotatorXml = buildAnnotatorXml(annotatorAliasByUser, annotatorDescriptions);
        String ontologyXml = buildOntologyXml(ontologyAlias, ontologyName, ontologyFileName, sha256(ontologyJson));
        String annotationTaskXml = buildAnnotationTaskXml(taskAlias, sha256(taskBytes));
        String policyXml = buildPolicyXml(policyAlias, datasetAlias);

        return zip(archive -> {
            writeEntry(archive, "METADATA/annotation.xml", annotationXml.getBytes(StandardCharsets.UTF_8));
            writeEntry(archive, "METADATA/dataset.xml", datasetXml.getBytes(StandardCharsets.UTF_8));
            writeEntry(archive, "METADATA/annotator.xml", annotatorXml.getBytes(StandardCharsets.UTF_8));
            writeEntry(archive, "METADATA/ontology.xml", ontologyXml.getBytes(StandardCharsets.UTF_8));
            writeEntry(archive, "METADATA/annotation_task.xml", annotationTaskXml.getBytes(StandardCharsets.UTF_8));
            writeEntry(archive, "METADATA/policy.xml", policyXml.getBytes(StandardCharsets.UTF_8));
            writeEntry(archive, "ONTOLOGIES/" + ontologyFileName, ontologyJson);
            writeEntry(archive, "ANNOTATION_TASKS/task.txt", taskBytes);
            for (ImageAnnotation imageAnnotation : imageAnnotations) {
                writeEntry(archive, "ANNOTATIONS/" + imageAnnotation.filename(), imageAnnotation.geojson());
            }
        });
    }

    private List<AnnotationResult> fetchAnnotations(Long projectId) {
        JsonObject params = JsonObject.of("project", projectId);
        params.put("showDefault", true);
        params.put("showWKT", true);
        params.put("showGIS", true);

        AnnotationListing userListing = annotationListingBuilder.buildAnnotationListing(params);
        userListing.getColumnsToPrint().add("image");
        userListing.getColumnsToPrint().add("user");
        List<AnnotationResult> userAnnotations = annotationListingService.listGeneric(userListing);

        JsonObject reviewedParams = new JsonObject(params);
        reviewedParams.put("reviewed", true);
        AnnotationListing reviewedListing = annotationListingBuilder.buildAnnotationListing(reviewedParams);
        reviewedListing.getColumnsToPrint().add("image");
        reviewedListing.getColumnsToPrint().add("user");
        List<AnnotationResult> reviewedAnnotations = annotationListingService.listGeneric(reviewedListing);

        List<AnnotationResult> result = new ArrayList<>(userAnnotations);
        result.addAll(reviewedAnnotations);
        return result;
    }

    private byte[] buildFeatureCollection(
        List<AnnotationResult> annotations,
        Map<Long, String> termNames,
        String name
    ) {
        List<Map<String, Object>> features = annotations.stream()
            .map(annotation -> toFeature(annotation, termNames))
            .flatMap(Optional::stream)
            .toList();

        Map<String, Object> featureCollection = new LinkedHashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("name", name);
        featureCollection.put("features", features);

        return JsonObject.toJsonString(featureCollection).getBytes(StandardCharsets.UTF_8);
    }

    private Optional<Map<String, Object>> toFeature(AnnotationResult annotation, Map<Long, String> termNames) {
        Object location = annotation.get("location");
        if (location == null) {
            return Optional.empty();
        }

        try {
            Geometry geometry = wktReader.read(location.toString());
            Map<String, Object> geometryJson = JsonObject.toMap(geoJsonWriter.write(geometry));
            if (geometryJson == null) {
                return Optional.empty();
            }

            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("type", "Feature");
            feature.put("properties", buildProperties(annotation, termNames));
            feature.put("geometry", geometryJson);
            return Optional.of(feature);
        } catch (ParseException e) {
            log.warn("Unable to parse WKT for annotation {}: {}", annotation.get("id"), e.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Object> buildProperties(AnnotationResult annotation, Map<Long, String> termNames) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("unit", "pixel");

        Object terms = annotation.get("term");
        if (terms instanceof List<?> termList && !termList.isEmpty()) {
            String termName = termNames.get(asLong(termList.getFirst()));
            if (termName != null) {
                properties.put("path_class_name", termName);
            }
        }

        return properties;
    }

    private Map<Long, String> resolveAnnotatorDescriptions(Set<Long> userIds) {
        Set<Long> validIds = userIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toSet());
        if (validIds.isEmpty()) {
            return Map.of();
        }

        try {
            return userHttpContract.findByIdsIn(validIds, Pageable.unpaged())
                .stream()
                .collect(Collectors.toMap(
                    UserResponse::id,
                    user -> user.fullName().orElse(user.username()),
                    (a, b) -> a
                ));
        } catch (RuntimeException e) {
            log.warn("Unable to resolve annotator descriptions: {}", e.getMessage());
            return Map.of();
        }
    }

    private byte[] buildOntologyJson(List<TermResponse> terms) {
        List<Map<String, Object>> entries = terms.stream().map(term -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("type", term.name());
            entry.put("color", term.color());
            return entry;
        }).collect(Collectors.toList());
        return JsonObject.toJsonString(entries).getBytes(StandardCharsets.UTF_8);
    }

    private String buildTaskText() {
        return """
            Annotation Process Documentation
            
            This dataset was exported from Cytomine. It gathers the annotations created on the project images.
            
            ## Annotation Compilation
            - Each GeoJSON file under ANNOTATIONS/ contains every annotation available for a single image.
            - Both user annotations and reviewed annotations are combined into the same file.
            
            ## Ontology
            - Annotation classes refer to the project ontology described under ONTOLOGIES/.
            - The `path_class_name` property of each feature holds the associated ontology term, when any.
            """;
    }

    private String buildAnnotationXml(List<ImageAnnotation> imageAnnotations, String ontologyAlias, String taskAlias) {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("ANNOTATION_SET");
        for (ImageAnnotation imageAnnotation : imageAnnotations) {
            xml.open("ANNOTATION", "alias", imageAnnotation.annotationAlias());
            xml.empty("IMAGE_REF", "alias", imageAnnotation.imageAlias());
            xml.open("FILES");
            xml.empty(
                "FILE",
                "filename",
                "ANNOTATIONS/" + imageAnnotation.filename(),
                "filetype",
                "geojson",
                "checksum_method",
                "SHA256",
                "checksum",
                imageAnnotation.checksum(),
                "unencrypted_checksum",
                imageAnnotation.checksum()
            );
            xml.close("FILES");
            xml.empty("ANNOTATOR_REF", "alias", imageAnnotation.annotatorAlias());
            xml.empty("ONTOLOGY_REF", "alias", ontologyAlias);
            xml.empty("TASK_REF", "alias", taskAlias);
            xml.open("ATTRIBUTES");
            xml.stringAttribute("date_of_creation", imageAnnotation.dateOfCreation());
            xml.close("ATTRIBUTES");
            xml.close("ANNOTATION");
        }
        xml.close("ANNOTATION_SET");
        return xml.build();
    }

    private String buildDatasetXml(Project project, String datasetAlias, List<ImageAnnotation> imageAnnotations) {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("DATASET_SET");
        xml.open("DATASET", "alias", datasetAlias);
        xml.leaf("TITLE", project.getName());
        xml.leaf("SHORT_NAME", project.getName());
        xml.leaf("DESCRIPTION", "Annotations exported from the Cytomine project \"" + project.getName() + "\".");
        xml.leaf("VERSION", "1.0.0");
        xml.leaf("METADATA_STANDARD", "3.0.0");
        xml.leaf("DATASET_TYPE", "Whole slide imaging");
        for (ImageAnnotation imageAnnotation : imageAnnotations) {
            xml.empty("ANNOTATION_REF", "alias", imageAnnotation.annotationAlias());
        }
        xml.open("ATTRIBUTES");
        xml.nilStringAttribute("tox_study_duration");
        xml.close("ATTRIBUTES");
        xml.close("DATASET");
        xml.close("DATASET_SET");
        return xml.build();
    }

    private String buildAnnotatorXml(Map<Long, String> annotatorAliasByUser, Map<Long, String> descriptions) {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("ANNOTATOR_SET");
        for (Map.Entry<Long, String> entry : annotatorAliasByUser.entrySet()) {
            xml.open("ANNOTATOR", "alias", entry.getValue());
            xml.open("ATTRIBUTES");
            xml.stringAttribute("annotator_type", "Human");
            String description = descriptions.getOrDefault(entry.getKey(), "Cytomine annotator");
            xml.stringAttribute("description", description);
            xml.close("ATTRIBUTES");
            xml.close("ANNOTATOR");
        }
        xml.close("ANNOTATOR_SET");
        return xml.build();
    }

    private String buildOntologyXml(
        String ontologyAlias,
        String ontologyName,
        String ontologyFileName,
        String checksum
    ) {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("ONTOLOGY_SET");
        xml.open("ONTOLOGY", "alias", ontologyAlias);
        xml.open("FILES");
        xml.empty(
            "FILE",
            "filename",
            "ONTOLOGIES/" + ontologyFileName,
            "filetype",
            "json",
            "checksum_method",
            "SHA256",
            "checksum",
            checksum,
            "unencrypted_checksum",
            checksum
        );
        xml.close("FILES");
        xml.open("ATTRIBUTES");
        xml.stringAttribute("description", ontologyName);
        xml.close("ATTRIBUTES");
        xml.close("ONTOLOGY");
        xml.close("ONTOLOGY_SET");
        return xml.build();
    }

    private String buildAnnotationTaskXml(String taskAlias, String checksum) {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("ANNOTATION_TASK_SET");
        xml.open("ANNOTATION_TASK", "alias", taskAlias);
        xml.open("FILES");
        xml.empty(
            "FILE",
            "filename",
            "ANNOTATION_TASKS/task.txt",
            "filetype",
            "txt",
            "checksum_method",
            "SHA256",
            "checksum",
            checksum,
            "unencrypted_checksum",
            checksum
        );
        xml.close("FILES");
        xml.open("ATTRIBUTES");
        xml.stringAttribute("description", "Annotations exported from Cytomine.");
        xml.close("ATTRIBUTES");
        xml.close("ANNOTATION_TASK");
        xml.close("ANNOTATION_TASK_SET");
        return xml.build();
    }

    private String buildPolicyXml(String policyAlias, String datasetAlias) {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("POLICY_SET");
        xml.open("POLICY", "alias", policyAlias);
        xml.empty("DATASET_REF", "alias", datasetAlias);
        xml.open("ATTRIBUTES");
        xml.stringAttribute("title", "N/A");
        xml.stringAttribute("policy_text", "N/A");
        xml.nilStringAttribute("type_of_dataset");
        xml.nilStringAttribute("legal_basis_for_sharing_the_data");
        xml.nilStringAttribute("custom_use_restrictions");
        xml.close("ATTRIBUTES");
        xml.close("POLICY");
        xml.close("POLICY_SET");
        return xml.build();
    }

    private void writeEntry(ZipOutputStream archive, String path, byte[] content) {
        try {
            archive.putNextEntry(new ZipEntry(ROOT + "/" + path));
            archive.write(content);
            archive.closeEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] zip(ZipContent content) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream archive = new ZipOutputStream(buffer)) {
            content.write(archive);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return buffer.toByteArray();
    }

    private static String alias(String prefix) {
        StringBuilder builder = new StringBuilder(prefix).append('_');
        for (int i = 0; i < ALIAS_LENGTH; i++) {
            builder.append(ALIAS_CHARS.charAt(ThreadLocalRandom.current().nextInt(ALIAS_CHARS.length())));
        }
        return builder.toString();
    }

    private static String sanitizeFilename(String name) {
        String sanitized = name == null ? "" : name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
        sanitized = sanitized.replaceAll("^_+|_+$", "");
        return sanitized.isEmpty() ? "ontology" : sanitized;
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            try {
                return Long.parseLong(string.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static LocalDateTime toLocalDateTime(Object epochMillis) {
        Long millis = asLong(epochMillis);
        if (millis == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    private static String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @FunctionalInterface
    private interface ZipContent {
        void write(ZipOutputStream archive) throws IOException;
    }
}
