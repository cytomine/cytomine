package be.cytomine.dto.meilisearch;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    public static class Image {
        private String identifier;
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
        private String imageOrigin;
        private String imageOrientation;
        private String imageType;
        private List<FileEntry> files;
        private Reference reference;
        private Map<String, Object> attributes;
        private String name;
        private String uid;
        private Object privateAttributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SlideDetail {
        private String identifier;
        private StainingInformation stainingInformation;
        private Reference reference;
        private Map<String, Object> attributes;
        private String name;
        private String uid;
        private Object privateAttributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StainingInformation {
        private List<StainDetail> stains;
        private Reference reference;
        private Map<String, Object> attributes;
        private String uid;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StainDetail {
        private Compound compound;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Compound {
        private String code;
        private String scheme;
        private String meaning;
        private String schemeVersion;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Reference {
        private String alias;
        private String accession;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Scanner {
        private String scannerManufacturersName;
        private String manufacturersModelName;
        private String deviceSerialNumber;
        private List<String> softwareVersions;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ImagingSetting {
        private PixelSpacing pixelSpacing;
        private List<OpticalPath> opticalPaths;
        private Focus focus;
        private Double depthOfField;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PixelSpacing {
        private Double vertical;
        private Double horizontal;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OpticalPath {
        private String identifier;
        private String colorSpace;
        private Double colorDepth;
        private Illumination illumination;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Illumination {
        private String type;
        private Double wavelength;
        private String color;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Focus {
        private String focusMethod;
        private Object extendedDepthOfField;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Size {
        private Double width;
        private Double height;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CompressionMethod {
        private String method;
        private Double ratio;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FileEntry {
        private String filename;
        private String filetype;
        private String checksumMethod;
        private String checksum;
        private String unencryptedChecksum;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SlideSummary {
        private String alias;
        private String identifier;
        private SlideStaining staining;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SlideStaining {
        private List<SlideStain> stains;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SlideStain {
        private String type;
        private Compound compound;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Block {
        private String alias;
        private String identifier;
        private Compound blockPreparation;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Specimen {
        private String alias;
        private String identifier;
        private Compound specimenType;
        private Compound extractionMethod;
        private Compound fixationType;
        private Compound anatomicalSite;
        private List<Object> anatomicalSites;
        private AgeAtExtraction ageAtExtraction;
        private BiologicalBeing biologicalBeing;
        private List<Observation> observations;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AgeAtExtraction {
        private String intervalStart;
        private String intervalLength;
        private Map<String, Object> attributes;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    public static class Observer {
        private String alias;
        private String identifier;
        private String observerType;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
        private List<Object> licenses;
        private String legalBasisForSharingTheData;
        private String informedConsentFormDefinedUseRestrictions;
        private String customUseRestrictions;
        private Reference reference;
        private Map<String, Object> attributes;
        private String uid;
    }
}