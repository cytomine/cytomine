package be.cytomine.dto.meilisearch;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeiliSearchImageResponse {
    private Image image;
    private SlideSummary slide;
    private Block block;
    private List<Specimen> specimens;
    private Dataset dataset;
    private Policy policy;
    private String id;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String identifier;
        private Long abstractImageId;
        private SlideDetail slide;
        private Scanner scanner;
        private ImagingSetting imagingSetting;
        private Size tileSize;
        private Size imageShape;
        private Boolean hasOverview;
        private Boolean hasLabel;
        private Boolean biologicalBeingIdentifierPresent;
        private Boolean tmaStatus;
        private String colorProfile;
        private String compressionStatus;
        private List<CompressionMethod> compressionMethods;
        private String acquisitionDateTime;
        private ImageOrigin imageOrigin;
        private ImageOrientation imageOrientation;
        private String imageType;
        private List<FileEntry> files;
        private Reference reference;
        private Map<String, Object> attributes;
        private String name;
        private String uid;
        private Map<String, Object> privateAttributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlideDetail {
        private String identifier;
        private StainingInformation stainingInformation;
        private Reference reference;
        private Map<String, Object> attributes;
        private String name;
        private String uid;
        private Map<String, Object> privateAttributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StainingInformation {
        private Compound procedure;
        private List<StainDetail> stains;
        private Reference reference;
        private Map<String, Object> attributes;
        private String uid;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StainDetail {
        private Compound compound;
        private Compound target;
        private String reporterType;
        private String reporterColor;
        private AntibodyInformation antibodyInformation;
        private Compound probe;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AntibodyInformation {
        private String commercialName;
        private String antibodyVendor;
        private Map<String, Object> attributes;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Compound {
        private String code;
        private String scheme;
        private String meaning;
        private String schemeVersion;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Compound(Object value) {
            if (value instanceof String string) {
                this.meaning = string;
            } else if (value instanceof Map<?, ?> map) {
                this.code = toStringOrNull(map.get("code"));
                this.scheme = toStringOrNull(map.get("scheme"));
                this.meaning = toStringOrNull(map.get("meaning"));
                this.schemeVersion = toStringOrNull(map.get("scheme_version"));
            }
        }

        private static String toStringOrNull(Object value) {
            return value == null ? null : value.toString();
        }
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reference {
        private String alias;
        private String accession;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scanner {
        private String scannerManufacturersName;
        private String manufacturersModelName;
        private String deviceSerialNumber;
        private List<String> softwareVersions;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImagingSetting {
        private PixelSpacing pixelSpacing;
        private List<OpticalPath> opticalPaths;
        private Focus focus;
        private Double depthOfField;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PixelSpacing {
        private Double vertical;
        private Double horizontal;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpticalPath {
        private String identifier;
        private String colorSpace;
        private Double colorDepth;
        private Illumination illumination;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Illumination {
        private String type;
        private Double wavelength;
        private String color;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Focus {
        private String focusMethod;
        private ExtendedDepthOfField extendedDepthOfField;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedDepthOfField {
        private Integer numberOfFocalPlanes;
        private Double distanceBetweenFocalPlanes;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Size {
        private Double width;
        private Double height;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageOrigin {
        private Double x;
        private Double y;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageOrientation {
        @JsonProperty("image_row_x_component")
        private Double imageRowXComponent;
        @JsonProperty("image_row_y_component")
        private Double imageRowYComponent;
        @JsonProperty("image_column_x_component")
        private Double imageColumnXComponent;
        @JsonProperty("image_column_y_component")
        private Double imageColumnYComponent;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompressionMethod {
        private String method;
        private Double ratio;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileEntry {
        private String filename;
        private String filetype;
        private String checksumMethod;
        private String checksum;
        private String unencryptedChecksum;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlideSummary {
        private String alias;
        private String identifier;
        private SlideStaining staining;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlideStaining {
        private Compound procedure;
        private List<SlideStain> stains;
        private Map<String, Object> raw;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlideStain {
        private String type;
        private Compound compound;
        private Compound target;
        private String reporterType;
        private String reporterColor;
        private AntibodyInformation antibody;
        private Compound probe;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Block {
        private String alias;
        private String identifier;
        private Compound blockPreparation;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Specimen {
        private String alias;
        private String identifier;
        private Compound specimenType;
        private Compound extractionMethod;
        private Compound fixationType;
        private Compound anatomicalSite;
        private List<Compound> anatomicalSites;
        private AgeAtExtraction ageAtExtraction;
        private BiologicalBeing biologicalBeing;
        private List<Observation> observations;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgeAtExtraction {
        private String intervalStart;
        private String intervalLength;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BiologicalBeing {
        private String alias;
        private String identifier;
        private String sex;
        private Compound animalSpecies;
        private Compound strain;
        private Compound disposition;
        private Compound controlStatus;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Observation {
        private String observationAlias;
        private String identifier;
        private String statementType;
        private String statementStatus;
        private Map<String, Compound> codeAttributes;
        private Map<String, Object> customAttributes;
        private String freetext;
        private List<Observer> observers;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Observer {
        private String alias;
        private String identifier;
        private String observerType;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dataset {
        private String alias;
        private String accession;
        private String title;
        private String description;
        private Double studyDurationDays;
        private String metadataStandard;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Policy {
        private String identifier;
        private String title;
        private String policyText;
        private String typeOfDataset;
        private String termsOfUseVersion;
        private List<String> allowedUses;
        private String allowedGeographicalDistribution;
        private String durationOfUse;
        private Boolean definedResearchQuestionRequired;
        private String typeOfAccess;
        private String requiredBigpictureAcknowledgements;
        private List<String> requiredCustomAcknowledgements;
        private List<String> requiredCitations;
        private List<String> licenses;
        private String legalBasisForSharingTheData;
        private String informedConsentFormDefinedUseRestrictions;
        private String customUseRestrictions;
        private Reference reference;
        private Map<String, Object> attributes;
        private String uid;
    }
}