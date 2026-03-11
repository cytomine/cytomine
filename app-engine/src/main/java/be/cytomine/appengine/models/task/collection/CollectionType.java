package be.cytomine.appengine.models.task.collection;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import be.cytomine.appengine.dto.inputs.task.TaskRunParameterValue;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionGenericTypeConstraint;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionItemValue;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionValue;
import be.cytomine.appengine.dto.inputs.task.types.collection.GeoCollectionValue;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.exceptions.ProvisioningException;
import be.cytomine.appengine.exceptions.TypeValidationException;
import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageDataEntry;
import be.cytomine.appengine.handlers.StorageDataType;
import be.cytomine.appengine.models.task.Parameter;
import be.cytomine.appengine.models.task.ParameterType;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.models.task.Type;
import be.cytomine.appengine.models.task.TypePersistence;
import be.cytomine.appengine.models.task.ValueType;
import be.cytomine.appengine.models.task.bool.BooleanPersistence;
import be.cytomine.appengine.models.task.datetime.DateTimePersistence;
import be.cytomine.appengine.models.task.enumeration.EnumerationPersistence;
import be.cytomine.appengine.models.task.file.FilePersistence;
import be.cytomine.appengine.models.task.file.FileType;
import be.cytomine.appengine.models.task.geometry.GeometryPersistence;
import be.cytomine.appengine.models.task.geometry.GeometryType;
import be.cytomine.appengine.models.task.image.ImagePersistence;
import be.cytomine.appengine.models.task.image.ImageType;
import be.cytomine.appengine.models.task.integer.IntegerPersistence;
import be.cytomine.appengine.models.task.number.NumberPersistence;
import be.cytomine.appengine.models.task.string.StringPersistence;
import be.cytomine.appengine.repositories.bool.BooleanPersistenceRepository;
import be.cytomine.appengine.repositories.collection.CollectionPersistenceRepository;
import be.cytomine.appengine.repositories.collection.ReferencePersistenceRepository;
import be.cytomine.appengine.repositories.datetime.DateTimePersistenceRepository;
import be.cytomine.appengine.repositories.enumeration.EnumerationPersistenceRepository;
import be.cytomine.appengine.repositories.geometry.GeometryPersistenceRepository;
import be.cytomine.appengine.repositories.integer.IntegerPersistenceRepository;
import be.cytomine.appengine.repositories.number.NumberPersistenceRepository;
import be.cytomine.appengine.repositories.string.StringPersistenceRepository;
import be.cytomine.appengine.utils.AppEngineApplicationContext;
import be.cytomine.appengine.utils.FileHelper;

@SuppressWarnings("checkstyle:LineLength")
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class CollectionType extends Type {

    @Column
    private Integer minSize;

    @Column
    private Integer maxSize;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Type subType;

    @Transient
    private Type trackingType;

    @Transient
    private Type parentType;

    @Transient
    private boolean referenced;

    public CollectionType(CollectionType copy) {
        this.subType = copy.getSubType();
        this.maxSize = copy.getMaxSize();
        this.minSize = copy.getMinSize();
        this.parentType = copy.getParentType();
        this.trackingType = copy.getTrackingType();
        this.referenced = copy.isReferenced();
    }

    public void validateFeatureCollection(String json, GeometryType geometryType)
        throws TypeValidationException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new TypeValidationException(ErrorCode.INTERNAL_INVALID_FEATURE_COLLECTION);
        }

        // Validate "type"
        if (!rootNode.has("type") || !rootNode.get("type").asText().equals("FeatureCollection")) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_GEOJSON);
        }

        // Validate "features" array
        if (!rootNode.has("features") || !rootNode.get("features").isArray()) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_GEOJSON);
        }

        // validate against constraints
        int size = rootNode.get("features").size();
        if (size < minSize || size > maxSize) {
            throw new TypeValidationException(ErrorCode.INTERNAL_INVALID_COLLECTION_DIMENSIONS);
        }

        // Validate each feature
        for (JsonNode feature : rootNode.get("features")) {
            if (!feature.has("type") || !feature.get("type").asText().equals("Feature")) {
                throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_GEOJSON);
            }
            if (!feature.has("geometry")) {
                throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_GEOJSON);
            }

            // Validate the geometry using GeometryType
            JsonNode geometryNode = feature.get("geometry");
            try {
                geometryType.validate(objectMapper.writeValueAsString(geometryNode));
            } catch (JsonProcessingException e) {
                throw new TypeValidationException(ErrorCode.INTERNAL_INVALID_FEATURE_COLLECTION);
            }

        }
    }

    public void validateGeometryCollection(String json, GeometryType geometryType)
        throws TypeValidationException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new TypeValidationException("invalid geometry collection");
        }

        // Validate "type"
        if (!rootNode.has("type") || !rootNode.get("type").asText().equals("GeometryCollection")) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_GEOJSON);
        }

        // Validate "geometries" array
        if (!rootNode.has("geometries") || !rootNode.get("geometries").isArray()) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_GEOJSON);
        }

        // validate against constraints
        int size = rootNode.get("geometries").size();
        if (size < minSize || size > maxSize) {
            throw new TypeValidationException("invalid collection dimensions");
        }

        // Validate each geometry using GeometryType
        for (JsonNode geometryNode : rootNode.get("geometries")) {
            try {
                geometryType.validate(objectMapper.writeValueAsString(geometryNode));
            } catch (JsonProcessingException e) {
                throw new TypeValidationException("invalid geometry collection");
            }
        }
    }

    public void setConstraint(CollectionGenericTypeConstraint constraint, String value) {
        switch (constraint) {
            case MIN_SIZE:
                this.setMinSize(Integer.parseInt(value));
                break;
            case MAX_SIZE:
                this.setMaxSize(Integer.parseInt(value));
                break;
            default:
        }
    }

    @Override
    public void validateFiles(
        Run run,
        Parameter currentOutput,
        StorageData currentOutputStorageData)
        throws TypeValidationException {
        // make sure we have the right file structure
        Type currentType = new CollectionType(this);
        while (currentType instanceof CollectionType) {
            currentType = ((CollectionType) currentType).getSubType();
        }
        String leafType = currentType.getClass().getSimpleName();

        if (Objects.isNull(currentOutputStorageData)
            || Objects.isNull(currentOutputStorageData.getEntryList())
            || currentOutputStorageData.getEntryList().isEmpty()) {
            throw new RuntimeException("invalid collection dimensions");
        }

        Map<String, Object> lists = new LinkedHashMap<>();
        for (StorageDataEntry entry : currentOutputStorageData.getEntryList()) {
            String entryName = entry.getName();
            if (entry.getStorageDataType() == StorageDataType.DIRECTORY) {
                List<Object> nestedItems = new ArrayList<>();
                lists.put(entryName, nestedItems);
            } else { // when it is a file
                String parentListName = entryName.substring(0, entryName.lastIndexOf('/'));

                if (entryName.endsWith("array.yml")) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    String arrayDotYmlContent = FileHelper.read(entry.getData(), getStorageCharset());
                    item.put("array.yml", arrayDotYmlContent);
                    ((List<Object>) lists.get(parentListName)).add(item);
                    continue;
                }

                Map<String, Object> item = new LinkedHashMap<>(); // anyway we need to add it to this item
                String indexString = entryName.substring(entryName.lastIndexOf('/') + 1); // just the name of one dicom file

                try {
                    item.put("index", Integer.parseInt(indexString));
                } catch (NumberFormatException e) {
                    item.put(indexString, entry.getData());
                    continue;
                }

                Set<String> rawTypes = Set.of(FileType.class.getSimpleName(), ImageType.class.getSimpleName());
                Object value;
                if (rawTypes.contains(leafType)) {
                    value = entry.getData();
                } else {
                    String raw = FileHelper.read(entry.getData(), getStorageCharset());
                    value = switch (leafType) {
                        case "IntegerType"  -> Integer.parseInt(raw);
                        case "StringType", "GeometryType", "EnumerationType" -> raw;
                        case "NumberType"   -> Double.parseDouble(raw);
                        case "BooleanType"  -> Boolean.parseBoolean(raw);
                        case "DateTimeType" -> Instant.parse(raw);
                        default -> throw new TypeValidationException("unknown leaf type: " + leafType);
                    };
                }
                item.put("value", value);

                ((List<Object>) lists.get(parentListName)).add(item);
            }
        }
        // check that every list (collection or nested collection has array.yml)
        for (String key : lists.keySet()) {
            List<Object> collection = (List<Object>) lists.get(key);
            int arrayYmlFound = 0;
            for (Object item : collection) {
                Map<String, Object> itemMap = (Map<String, Object>) item;
                if (itemMap.containsKey("array.yml")) {
                    arrayYmlFound++;
                }
            }
            if (arrayYmlFound == 0) {
                throw new TypeValidationException(ErrorCode.INTERNAL_MISSING_METADATA);
            }
        }

        validate(lists.get(currentOutput.getName()));
    }

    @Override
    public void validate(Object valueObject) throws TypeValidationException {
        if (valueObject == null) {
            return;
        }

        Set<Class<?>> excludedTypes = Set.of(
            ArrayList.class,
            String.class,
            Integer.class,
            Double.class,
            Boolean.class,
            LinkedHashMap.class,
            File.class
        );

        if (!excludedTypes.contains(valueObject.getClass())) {
            throw new TypeValidationException(ErrorCode.INTERNAL_WRONG_PROVISION_STRUCTURE);
        }

        if (valueObject instanceof ArrayList) {
            validateNativeCollection((ArrayList<?>) valueObject);
        }

        Set<Class<?>> validTypes = Set.of(
            String.class,
            Integer.class,
            Double.class,
            Boolean.class,
            File.class
        );
        if (validTypes.contains(valueObject.getClass())) {

            ObjectMapper objectMapper = new ObjectMapper();
            // validate a GeoJSON collection like FeatureCollection or GeometryCollection
            if (valueObject instanceof String stringValueObject) {
                try {
                    JsonNode rootNode = objectMapper.readTree(stringValueObject);
                    if (rootNode.has("type")) {
                        validateGeoJsonCollection(stringValueObject);
                    } else {
                        validatePrimitiveCollectionItem(stringValueObject);
                    }
                } catch (JsonProcessingException e) {
                    throw new TypeValidationException(ErrorCode.INTERNAL_INVALID_FEATURE_COLLECTION);
                }

            } else {
                validatePrimitiveCollectionItem(valueObject);
            }
        }
    }

    private void validatePrimitiveCollectionItem(Object valueObject) throws TypeValidationException {
        Type currentType = new CollectionType(this);
        while (currentType instanceof CollectionType collectionType) {
            currentType = collectionType.getSubType();
            if (!(currentType instanceof CollectionType)) {
                currentType.validate(valueObject);
            }
        }
    }

    private void validateGeoJsonCollection(String valueObject)
        throws TypeValidationException {
        GeometryType geometryType = new GeometryType();
        try {
            validateFeatureCollection(valueObject, geometryType);
        } catch (TypeValidationException e) {
            validateGeometryCollection(valueObject, geometryType);
        }
    }

    private void validateNativeCollection(List<?> value) throws TypeValidationException {
        validateNode(value);
    }

    public void validateNode(Object obj) throws TypeValidationException {
        if (trackingType == null) {
            trackingType = new CollectionType(this);
            parentType = trackingType;
        } else if (trackingType instanceof CollectionType) {
            parentType = trackingType;
            trackingType = ((CollectionType) trackingType).getSubType();
        }

        if (obj instanceof List<?> elements && trackingType instanceof CollectionType currentType) {
            Integer minSize = currentType.getMinSize();
            Integer maxSize = currentType.getMaxSize();

            if (minSize != null && maxSize != null && (elements.size() < minSize || elements.size() > maxSize)) {
                throw new TypeValidationException(ErrorCode.INTERNAL_INVALID_COLLECTION_DIMENSIONS);
            }

            for (Object o : elements) {
                validateNode(o);
            }
        } else if (obj instanceof Map<?, ?> map) {
            Object value = map.get("value");

            if (!(trackingType instanceof CollectionType) && value instanceof List<?>) {
                throw new TypeValidationException(ErrorCode.INTERNAL_WRONG_PROVISION_STRUCTURE);
            }

            if (value instanceof List<?> elements) {
                CollectionType currentType = (CollectionType) trackingType;
                if (elements.size() < currentType.getMinSize() || elements.size() > currentType.getMaxSize()) {
                    throw new TypeValidationException(ErrorCode.INTERNAL_INVALID_COLLECTION_DIMENSIONS);
                }
                for (Object o : elements) {
                    validateNode(o);
                }
                trackingType = parentType;
            }

            if (value != null) {
                trackingType.validate(value);
                referenced = value instanceof String
                        && (trackingType instanceof FileType
                        || trackingType instanceof ImageType);
                trackingType = parentType;
            }
        }
    }

    @Transactional
    @Override
    public void persistProvision(JsonNode provision, UUID runId)
        throws ProvisioningException {
        if (provision.has("index")) {
            persistCollectionItem(provision, runId);
        } else {
            persistCollection(provision, runId);
        }
    }

    private void persistCollectionItem(JsonNode provision, UUID runId) throws ProvisioningException {
        Type currentType = new CollectionType(this);
        CollectionPersistenceRepository collectionRepo =
            AppEngineApplicationContext.getBean(CollectionPersistenceRepository.class);
        String leafType;
        CollectionType parentType = null;
        String[] indexes = provision.get("index").textValue().split("/");
        CollectionPersistence persistedProvision = new CollectionPersistence();
        for (int i = 0; i < indexes.length; i++) {
            if (currentType instanceof CollectionType) {
                if (i == 0) { // first item which is the parameter
                    persistedProvision =
                        collectionRepo.findCollectionPersistenceByParameterNameAndRunIdAndParameterType(
                            indexes[i], runId, ParameterType.INPUT);
                    if (Objects.isNull(persistedProvision)) {
                        persistedProvision = new CollectionPersistence();
                        persistedProvision.setRunId(runId);
                        persistedProvision.setParameterType(ParameterType.INPUT);
                        persistedProvision.setParameterName(indexes[i]);
                        persistedProvision.setValueType(ValueType.ARRAY);
                        persistedProvision.setProvisioned(true);
                        persistedProvision = collectionRepo.save(persistedProvision);
                    }
                    parentType = (CollectionType) currentType;
                    currentType = ((CollectionType) currentType).getSubType();
                    continue;
                } else {
                    if (i == indexes.length - 1) { // this is the last item, yet it is still a collection (nested)
                        while (currentType instanceof CollectionType) {
                            ((CollectionType) currentType).setParentType(currentType);
                            currentType = ((CollectionType) currentType).getSubType();
                        }
                        leafType = currentType.getClass().getSimpleName();
                        String parameterName = transform(indexes);
                        CollectionPersistence collectionAsItem = (CollectionPersistence) persistNode(provision, runId, parameterName, leafType);
                        String collectionIndex = transform(indexes);
                        collectionIndex = collectionIndex.substring(collectionIndex.indexOf("["));
                        collectionAsItem.setCollectionIndex(collectionIndex);
                        persistedProvision.getItems().add(collectionAsItem);
                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }
                        collectionRepo.saveAndFlush(persistedProvision);
                        break; // do nothing after this
                    } else {
                        List<TypePersistence> items = persistedProvision.getItems();
                        for (TypePersistence item : items) {
                            if (indexes[i].equalsIgnoreCase(getNumberAtPosition(item.getCollectionIndex(), i))) {
                                persistedProvision = (CollectionPersistence) item;
                                break;
                            }
                        }
                    }
                    parentType = (CollectionType) currentType;
                    currentType = ((CollectionType) currentType).getSubType();
                }

            } else { // reached the end of crawling the subtypes then insert into the items of persisted provision
                leafType = currentType.getClass().getSimpleName();
                String collectionItemParameterName;
                switch (leafType) {
                    case "IntegerType":
                        IntegerPersistence integerPersistence = new IntegerPersistence();
                        integerPersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        integerPersistence.setParameterName(collectionItemParameterName);
                        integerPersistence.setRunId(runId);
                        integerPersistence.setValueType(ValueType.INTEGER);
                        integerPersistence.setProvisioned(true);
                        integerPersistence.setValue(provision.get("value").asInt());
                        integerPersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(integerPersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());

                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }
                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "StringType":
                        StringPersistence stringPersistence = new StringPersistence();
                        stringPersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        stringPersistence.setParameterName(collectionItemParameterName);
                        stringPersistence.setRunId(runId);
                        stringPersistence.setValueType(ValueType.STRING);
                        stringPersistence.setProvisioned(true);
                        stringPersistence.setValue(provision.get("value").asText());
                        stringPersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(stringPersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());

                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }

                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "NumberType":
                        NumberPersistence numberPersistence = new NumberPersistence();
                        numberPersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        numberPersistence.setParameterName(collectionItemParameterName);
                        numberPersistence.setRunId(runId);
                        numberPersistence.setValueType(ValueType.NUMBER);
                        numberPersistence.setProvisioned(true);
                        numberPersistence.setValue(provision.get("value").asDouble());
                        numberPersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(numberPersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());
                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }

                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "BooleanType":
                        BooleanPersistence booleanPersistence = new BooleanPersistence();
                        booleanPersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        booleanPersistence.setParameterName(collectionItemParameterName);
                        booleanPersistence.setRunId(runId);
                        booleanPersistence.setValueType(ValueType.BOOLEAN);
                        booleanPersistence.setProvisioned(true);
                        booleanPersistence.setValue(provision.get("value").asBoolean());
                        booleanPersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(booleanPersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());
                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }

                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "EnumerationType":
                        EnumerationPersistence enumPersistence = new EnumerationPersistence();
                        enumPersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        enumPersistence.setParameterName(collectionItemParameterName);
                        enumPersistence.setRunId(runId);
                        enumPersistence.setValueType(ValueType.ENUMERATION);
                        enumPersistence.setValue(provision.get("value").asText());
                        enumPersistence.setProvisioned(true);
                        enumPersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(enumPersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());
                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }
                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "GeometryType":
                        GeometryPersistence geoPersistence = new GeometryPersistence();
                        geoPersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        geoPersistence.setParameterName(collectionItemParameterName);
                        geoPersistence.setRunId(runId);
                        geoPersistence.setValueType(ValueType.GEOMETRY);
                        geoPersistence.setProvisioned(true);
                        geoPersistence.setValue(provision.get("value").asText());
                        geoPersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(geoPersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());
                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }
                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "FileType":
                        FilePersistence filePersistence = new FilePersistence();
                        filePersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        filePersistence.setParameterName(collectionItemParameterName);
                        filePersistence.setRunId(runId);
                        filePersistence.setValueType(ValueType.FILE);
                        filePersistence.setProvisioned(true);
                        filePersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(filePersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());
                        // make collection provisioned
                        if (persistedProvision.getItems().size() >= parentType.minSize
                            && persistedProvision.getItems().size() <= parentType.maxSize) {
                            persistedProvision.setProvisioned(true);
                        }
                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    case "ImageType":
                        ImagePersistence imagePersistence = new ImagePersistence();
                        imagePersistence.setParameterType(ParameterType.INPUT);
                        collectionItemParameterName = transform(provision.get("index").textValue());
                        imagePersistence.setParameterName(collectionItemParameterName);
                        imagePersistence.setRunId(runId);
                        imagePersistence.setValueType(ValueType.IMAGE);
                        imagePersistence.setProvisioned(true);
                        imagePersistence.setCollectionIndex(collectionItemParameterName.substring(collectionItemParameterName.indexOf("[")));
                        persistedProvision.getItems().add(imagePersistence);
                        persistedProvision.setSize(persistedProvision.getItems().size());
                        // make collection provisioned
                        if (Objects.nonNull(parentType.maxSize) && Objects.nonNull(parentType.minSize)) {
                            if (persistedProvision.getItems().size() >= parentType.minSize
                                && persistedProvision.getItems().size() <= parentType.maxSize) {
                                persistedProvision.setProvisioned(true);
                            }
                        }
                        collectionRepo.saveAndFlush(persistedProvision);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private String transform(String[] indexes) {
        if (indexes == null || indexes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(indexes[0]);

        for (int i = 1; i < indexes.length; i++) {
            sb.append("[").append(indexes[i]).append("]");
        }
        return sb.toString();
    }

    public String transform(String input) {
        int firstSlashIndex = input.indexOf('/');

        // If there's no slash, return input as is
        if (firstSlashIndex == -1) {
            return input;
        }

        // Split the string into prefix and numeric parts
        String prefix = input.substring(0, firstSlashIndex); // "input"
        String[] parts = input.substring(firstSlashIndex + 1).split("/"); // ["0", "4"]

        // Reconstruct the transformed string
        StringBuilder result = new StringBuilder(prefix);
        for (String part : parts) {
            result.append("[").append(part).append("]");
        }

        return result.toString();
    }

    public String getNumberAtPosition(String input, int position) {
        Pattern pattern = Pattern.compile("\\d+"); // Regex to match numbers
        Matcher matcher = pattern.matcher(input);

        List<String> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(matcher.group()); // Extract and parse numbers
        }

        // Ensure the position is valid
        if (position > 0 && position <= numbers.size()) {
            return numbers.get(position - 1); // Return number at the requested position
        }
        return null; // Invalid position
    }

    private void persistCollection(JsonNode provision, UUID runId) throws ProvisioningException {
        Type currentType = new CollectionType(this);
        while (currentType instanceof CollectionType) {
            currentType = ((CollectionType) currentType).getSubType();
        }
        String leafType = currentType.getClass().getSimpleName();

        CollectionPersistenceRepository collectionRepo =
            AppEngineApplicationContext.getBean(CollectionPersistenceRepository.class);
        String parameterName = provision.get("param_name").asText();

        CollectionPersistence collectionPersistence = (CollectionPersistence) persistNode(provision,
            runId, parameterName, leafType);
        collectionRepo.save(collectionPersistence);
    }

    public TypePersistence persistNode(JsonNode node, UUID runId, String parameterName, String leafType)
        throws ProvisioningException {
        CollectionPersistenceRepository collectionRepo =
            AppEngineApplicationContext.getBean(CollectionPersistenceRepository.class);
        if (node.isArray()) {
            CollectionPersistence persistedProvision =
                collectionRepo.findCollectionPersistenceByParameterNameAndRunIdAndParameterType(
                parameterName, runId, ParameterType.INPUT);
            if (persistedProvision == null) {
                persistedProvision = new CollectionPersistence();
                persistedProvision.setValueType(ValueType.ARRAY);
                persistedProvision.setParameterType(ParameterType.INPUT);
                persistedProvision.setParameterName(parameterName);
                persistedProvision.setProvisioned(true);
                persistedProvision.setReferenced(referenced); // to later one tell if this is using refs
                persistedProvision.setRunId(runId);
            }
            List<TypePersistence> items = new ArrayList<>();
            for (JsonNode item : node) {
                items.add(persistNode(item, runId, parameterName, leafType));
            }
            persistedProvision.setItems(items);
            persistedProvision.setSize(items.size());
            return persistedProvision;
        }
        if (node.isValueNode()) {
            switch (leafType) {
                case "IntegerType":
                    return getIntegerPersistence(node, runId, parameterName);
                case "StringType":
                    return getStringPersistence(node, runId, parameterName);
                case "NumberType":
                    return getNumberPersistence(node, runId, parameterName);
                case "BooleanType":
                    return getBooleanPersistence(node, runId, parameterName);
                case "EnumerationType":
                    return getEnumerationPersistence(node, runId, parameterName);
                case "GeometryType":
                    return getGeometryPersistence(node, runId, parameterName);
                case "DateTimeType":
                    return getDateTimePersistence(node, runId, parameterName);
                case "FileType", "ImageType":
                    return getReferencePersistence(node, runId, parameterName);
                default:
                    return null;
            }
        }

        if (node.isObject()) {
            String paramName = "";
            if (Objects.nonNull(node.get("param_name"))) {
                paramName = node.get("param_name").asText();
            } else {
                String transformedIndex = transform(node.get("index").asText());
                if (parameterName.equalsIgnoreCase(transformedIndex)) {
                    paramName = parameterName;
                } else {
                    paramName = parameterName + "[" + node.get("index").asText() + "]";
                }
            }
            // check if this is GeoJSON collection
            if (node.has("type")
                && (node.get("type").asText().equals("GeometryCollection")
                || node.get("type").asText().equals("FeatureCollection"))) {
                CollectionPersistence persistedProvision =
                    collectionRepo.findCollectionPersistenceByParameterNameAndRunIdAndParameterType(
                    parameterName, runId, ParameterType.INPUT);
                if (persistedProvision == null) {
                    persistedProvision = new CollectionPersistence();
                    persistedProvision.setValueType(ValueType.ARRAY);
                    persistedProvision.setParameterType(ParameterType.INPUT);
                    persistedProvision.setParameterName(paramName);
                    persistedProvision.setProvisioned(true);
                    persistedProvision.setRunId(runId);
                    persistedProvision.setCompactValue(node.get("value").asText());
                    return persistedProvision;
                }
            } else {
                return persistNode(node.get("value"), runId, paramName, leafType);
            }
        }

        if (node.isNull()) {
            throw new ProvisioningException("invalid provision value");
        }
        return null;
    }

    private static IntegerPersistence getIntegerPersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        IntegerPersistenceRepository integerPersistenceRepository = AppEngineApplicationContext.getBean(IntegerPersistenceRepository.class);

        IntegerPersistence integerPersistence = integerPersistenceRepository.findIntegerPersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (integerPersistence == null) {
            integerPersistence = new IntegerPersistence();
            integerPersistence.setParameterType(ParameterType.INPUT);
            integerPersistence.setParameterName(parameterName);
            integerPersistence.setRunId(runId);
            integerPersistence.setValueType(ValueType.INTEGER);
            integerPersistence.setValue(node.asInt());
            integerPersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            integerPersistence.setValue(node.asInt());
        }
        return integerPersistence;
    }

    private static StringPersistence getStringPersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        StringPersistenceRepository stringPersistenceRepository = AppEngineApplicationContext.getBean(
            StringPersistenceRepository.class);

        StringPersistence stringPersistence = stringPersistenceRepository.findStringPersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (stringPersistence == null) {
            stringPersistence = new StringPersistence();
            stringPersistence.setParameterType(ParameterType.INPUT);
            stringPersistence.setParameterName(parameterName);
            stringPersistence.setRunId(runId);
            stringPersistence.setValueType(ValueType.STRING);
            stringPersistence.setValue(node.asText());
            stringPersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            stringPersistence.setValue(node.asText());
        }
        return stringPersistence;
    }

    private static ReferencePersistence getReferencePersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        ReferencePersistenceRepository referencePersistenceRepository = AppEngineApplicationContext.getBean(
            ReferencePersistenceRepository.class);

        ReferencePersistence referencePersistence = referencePersistenceRepository.findReferencePersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (referencePersistence == null) {
            referencePersistence = new ReferencePersistence();
            referencePersistence.setParameterType(ParameterType.INPUT);
            referencePersistence.setParameterName(parameterName);
            referencePersistence.setRunId(runId);
            referencePersistence.setValueType(ValueType.REFERENCE);
            referencePersistence.setValue(node.asText());
            referencePersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            referencePersistence.setValue(node.asText());
        }
        return referencePersistence;
    }

    private static NumberPersistence getNumberPersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        NumberPersistenceRepository numberPersistenceRepository = AppEngineApplicationContext.getBean(
            NumberPersistenceRepository.class);

        NumberPersistence numberPersistence = numberPersistenceRepository.findNumberPersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (numberPersistence == null) {
            numberPersistence = new NumberPersistence();
            numberPersistence.setParameterType(ParameterType.INPUT);
            numberPersistence.setParameterName(parameterName);
            numberPersistence.setRunId(runId);
            numberPersistence.setValueType(ValueType.NUMBER);
            numberPersistence.setValue(node.asDouble());
            numberPersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            numberPersistence.setValue(node.asDouble());
        }
        return numberPersistence;
    }

    private static BooleanPersistence getBooleanPersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        BooleanPersistenceRepository booleanPersistenceRepository = AppEngineApplicationContext.getBean(
            BooleanPersistenceRepository.class);

        BooleanPersistence booleanPersistence = booleanPersistenceRepository.findBooleanPersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (booleanPersistence == null) {
            booleanPersistence = new BooleanPersistence();
            booleanPersistence.setParameterType(ParameterType.INPUT);
            booleanPersistence.setParameterName(parameterName);
            booleanPersistence.setRunId(runId);
            booleanPersistence.setValueType(ValueType.NUMBER);
            booleanPersistence.setValue(node.asBoolean());
            booleanPersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            booleanPersistence.setValue(node.asBoolean());
        }
        return booleanPersistence;
    }

    private static EnumerationPersistence getEnumerationPersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        EnumerationPersistenceRepository enumerationPersistenceRepository = AppEngineApplicationContext.getBean(
            EnumerationPersistenceRepository.class);

        EnumerationPersistence enumerationPersistence = enumerationPersistenceRepository.findEnumerationPersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (enumerationPersistence == null) {
            enumerationPersistence = new EnumerationPersistence();
            enumerationPersistence.setParameterType(ParameterType.INPUT);
            enumerationPersistence.setParameterName(parameterName);
            enumerationPersistence.setRunId(runId);
            enumerationPersistence.setValueType(ValueType.ENUMERATION);
            enumerationPersistence.setValue(node.asText());
            enumerationPersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            enumerationPersistence.setValue(node.asText());
        }
        return enumerationPersistence;
    }

    private static GeometryPersistence getGeometryPersistence(
        JsonNode node,
        UUID runId,
        String parameterName) {

        GeometryPersistenceRepository geometryPersistenceRepository = AppEngineApplicationContext.getBean(
            GeometryPersistenceRepository.class);

        GeometryPersistence geometryPersistence = geometryPersistenceRepository.findGeometryPersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (geometryPersistence == null) {
            geometryPersistence = new GeometryPersistence();
            geometryPersistence.setParameterType(ParameterType.INPUT);
            geometryPersistence.setParameterName(parameterName);
            geometryPersistence.setRunId(runId);
            geometryPersistence.setValueType(ValueType.GEOMETRY);
            geometryPersistence.setValue(node.asText());
            geometryPersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            geometryPersistence.setValue(node.asText());
        }
        return geometryPersistence;
    }

    private static DateTimePersistence getDateTimePersistence(
        JsonNode node,
        UUID runId,
        String parameterName
    ) {
        DateTimePersistenceRepository datetimePersistenceRepository = AppEngineApplicationContext.getBean(DateTimePersistenceRepository.class);
        DateTimePersistence datetimePersistence = datetimePersistenceRepository.findDateTimePersistenceByParameterNameAndRunIdAndParameterType(
            parameterName, runId, ParameterType.INPUT
        );
        if (datetimePersistence == null) {
            datetimePersistence = new DateTimePersistence();
            datetimePersistence.setParameterType(ParameterType.INPUT);
            datetimePersistence.setParameterName(parameterName);
            datetimePersistence.setRunId(runId);
            datetimePersistence.setValueType(ValueType.DATETIME);
            datetimePersistence.setValue(Instant.parse(node.asText()));
            datetimePersistence.setCollectionIndex(parameterName.substring(parameterName.indexOf("[")));
        } else {
            datetimePersistence.setValue(Instant.parse(node.asText()));
        }
        return datetimePersistence;
    }

    private TypePersistence createPersistence(String leafType, String value) throws ProvisioningException {
        return switch (leafType) {
            case "IntegerType" -> {
                IntegerPersistence p = new IntegerPersistence();
                p.setValue(Integer.parseInt(value));
                p.setValueType(ValueType.INTEGER);
                yield p;
            }

            case "StringType" -> {
                StringPersistence p = new StringPersistence();
                p.setValue(value);
                p.setValueType(ValueType.STRING);
                yield p;
            }

            case "EnumerationType" -> {
                EnumerationPersistence p = new EnumerationPersistence();
                p.setValue(value);
                p.setValueType(ValueType.ENUMERATION);
                yield p;
            }

            case "GeometryType" -> {
                GeometryPersistence p = new GeometryPersistence();
                p.setValue(value);
                p.setValueType(ValueType.GEOMETRY);
                yield p;
            }

            case "NumberType" -> {
                NumberPersistence p = new NumberPersistence();
                p.setValue(Double.parseDouble(value));
                p.setValueType(ValueType.NUMBER);
                yield p;
            }

            case "BooleanType" -> {
                BooleanPersistence p = new BooleanPersistence();
                p.setValue(Boolean.parseBoolean(value));
                p.setValueType(ValueType.BOOLEAN);
                yield p;
            }

            case "FileType" -> {
                FilePersistence p = new FilePersistence();
                p.setValue(null);
                p.setValueType(ValueType.FILE);
                yield p;
            }

            case "ImageType" -> {
                ImagePersistence p = new ImagePersistence();
                p.setValue(null);
                p.setValueType(ValueType.IMAGE);
                yield p;
            }

            case "DateTimeType" -> {
                DateTimePersistence p = new DateTimePersistence();
                p.setValue(Instant.parse(value));
                p.setValueType(ValueType.DATETIME);
                yield p;
            }

            default ->  throw new ProvisioningException("unknown leaf type: " + leafType);
        };
    }

    @Transactional
    @Override
    public void persistResult(Run run, Parameter currentOutput, StorageData outputValue) throws ProvisioningException {
        CollectionPersistenceRepository collectionPersistenceRepository =
            AppEngineApplicationContext.getBean(CollectionPersistenceRepository.class);

        Type currentType = getSubType();
        while (currentType instanceof CollectionType ct) {
            currentType = ct.getSubType();
        }

        String leafType = currentType.getClass().getSimpleName();
        Map<String, TypePersistence> parameterNameToTypePersistence = new LinkedHashMap<>();
        outputValue.sortShallowToDeep();

        CollectionPersistence root = new CollectionPersistence();
        root.setValueType(ValueType.ARRAY);
        root.setParameterType(ParameterType.OUTPUT);
        root.setParameterName(currentOutput.getName());
        root.setRunId(run.getId());
        parameterNameToTypePersistence.put(currentOutput.getName(), root);

        List<StorageDataEntry> entries = outputValue.getEntryList();
        for (StorageDataEntry entry : entries.subList(1, entries.size())) {
            String parentName = entry.getName().substring(0, entry.getName().lastIndexOf("/"));

            if (entry.getStorageDataType() == StorageDataType.DIRECTORY) {
                String[] nameParts = entry.getName().trim().split("/");
                for (int i = 0; i < nameParts.length; i++) {
                    if (i != 0) {
                        nameParts[i] = "[" + nameParts[i] + "]";
                    }
                }
                // it is a subCollection if contains array.yml
                boolean containsArrayYml = outputValue.getEntryList().stream().anyMatch(
                    storage -> storage.getName()
                        .equalsIgnoreCase(entry.getName() + "/array.yml"));

                CollectionPersistence parentPersistence = (CollectionPersistence) parameterNameToTypePersistence.get(parentName);
                if (containsArrayYml) {
                    CollectionPersistence subCollection = new CollectionPersistence();
                    subCollection.setValueType(ValueType.ARRAY);
                    subCollection.setParameterName(entry.getName());
                    subCollection.setCollectionIndex(Arrays.stream(nameParts, 1, nameParts.length).collect(
                        Collectors.joining()));

                    parameterNameToTypePersistence.put(entry.getName(), subCollection);
                    parentPersistence.getItems().add(subCollection);
                } else {
                    DirectoryPersistence directory = new DirectoryPersistence();
                    directory.setParameterName(entry.getName());
                    parameterNameToTypePersistence.put(entry.getName(), directory);

                    parentPersistence.getItems().add(directory);
                }
            }

            if (entry.getStorageDataType().equals(StorageDataType.FILE)) {
                if (entry.getName().endsWith("array.yml")) {
                    CollectionPersistence parentPersistence = (CollectionPersistence) parameterNameToTypePersistence.get(parentName);
                    parentPersistence.setSize(getCollectionSize(entry));
                    continue;
                }

                String[] nameParts = entry.getName().trim().split("/");
                for (int i = 0; i < nameParts.length; i++) {
                    if (i != 0) {
                        nameParts[i] = "[" + nameParts[i] + "]";
                    }
                }
                CollectionPersistence parentCollection = (CollectionPersistence) parameterNameToTypePersistence.get(parentName);

                String entryValue = null;
                if (!(leafType.equals("FileType") || leafType.equals("ImageType"))) {
                    entryValue = FileHelper.read(entry.getData(), getStorageCharset());
                }

                if (entry.getName().endsWith(".geojson")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode geoJsonCollectionFileContent;
                    try {
                        geoJsonCollectionFileContent = objectMapper.readTree(entryValue);
                    } catch (JsonProcessingException e) {
                        throw new ProvisioningException("invalid format of geojson file " + entry.getName());
                    }
                    int geoJsonCollectionSize = 0;
                    if (geoJsonCollectionFileContent.has("features") && geoJsonCollectionFileContent.get("features").isArray()) {
                        geoJsonCollectionSize = geoJsonCollectionFileContent.get("features").size();
                    }
                    if (geoJsonCollectionFileContent.has("geometries") && geoJsonCollectionFileContent.get("geometries").isArray()) {
                        geoJsonCollectionSize = geoJsonCollectionFileContent.get("geometries").size();
                    }
                    CollectionPersistence geoJsonCollection = new CollectionPersistence();
                    geoJsonCollection.setValueType(ValueType.ARRAY);
                    geoJsonCollection.setParameterType(ParameterType.OUTPUT);
                    geoJsonCollection.setParameterName(currentOutput.getName());
                    geoJsonCollection.setRunId(run.getId());
                    geoJsonCollection.setCollectionIndex(Arrays.stream(nameParts, 1, nameParts.length).collect(Collectors.joining()));
                    geoJsonCollection.setSize(geoJsonCollectionSize);
                    geoJsonCollection.setCompactValue(entryValue);

                    parentCollection.getItems().add(geoJsonCollection);

                    continue;
                }

                TypePersistence persistence = createPersistence(leafType, entryValue);
                persistence.setParameterType(ParameterType.OUTPUT);
                persistence.setRunId(run.getId());
                persistence.setParameterName(String.join("", nameParts));
                persistence.setCollectionIndex(Arrays.stream(nameParts, 1, nameParts.length).collect(Collectors.joining()));

                if (leafType.equals("ImageType") && parentCollection == null) {
                    String parentDirectoryName = entry.getName().substring(0, entry.getName().lastIndexOf("/"));
                    DirectoryPersistence parentDirectory = (DirectoryPersistence) parameterNameToTypePersistence.get(parentDirectoryName);
                    parentDirectory.getItems().add(persistence);
                } else {
                    parentCollection.getItems().add(persistence);
                }
            }
        }

        collectionPersistenceRepository.save(root);
    }

    @Override
    public StorageData mapToStorageFileData(JsonNode provision, Run run) throws FileStorageException {
        String name = null;
        // if provision is an item
        if (provision.has("index")) {
            name = provision.get("index").asText();
        } else {
            name = provision.get("param_name").asText();
        }

        if (referenced) {
            return new StorageData(true);
        }

        // if provision is full collection
        return mapNode("/" + name, provision.get("value"),
            new StorageData(), run);
    }

    private StorageData mapNode(
        String path,
        JsonNode value,
        StorageData container,
        Run run)
        throws FileStorageException {

        Type currentType = new CollectionType(this);
        while (currentType instanceof CollectionType) {
            currentType = ((CollectionType) currentType).getSubType();
        }
        String leafType = currentType.getClass().getSimpleName();
        if (value.isNull()) {
            throw new FileStorageException(ErrorCode.INTERNAL_NULL_PROVISION);
        }
        if (value.isArray()) {
            container.add(new StorageDataEntry(path, StorageDataType.DIRECTORY));
            int size = value.size();
            String arrayDotYmpData = "size: " + size;
            container.add(new StorageDataEntry(FileHelper.write("array.yml", arrayDotYmpData.getBytes(StandardCharsets.UTF_8)),
                path + "/" + "array.yml",
                StorageDataType.FILE));
            for (JsonNode item : value) {
                container = mapNode(path + "/" + item.get("index").asText(), item.get("value"), container, run);
            }
        }
        if (value.isObject() || value.isValueNode()) {
            if (value.has("type")
                && (value.get("type").asText().equals("GeometryCollection")
                || value.get("type").asText().equals("FeatureCollection"))) {
                path += ".geojson";
            }

            StorageDataEntry itemFileEntry = null;
            if (leafType.equalsIgnoreCase("FileType")
                || leafType.equalsIgnoreCase("ImageType")) {

                itemFileEntry = new StorageDataEntry(new File(value.asText()), path, StorageDataType.FILE);
            } else {
                itemFileEntry = new StorageDataEntry(
                    FileHelper.write(path.substring(path.lastIndexOf("/") + 1), value.asText().getBytes(StandardCharsets.UTF_8)),
                    path,
                    StorageDataType.FILE
                );
            }

            // add the yml file to the storage data object
            String ymlPath = "";
            if (path.contains("/")) {
                ymlPath = path.substring(1, path.lastIndexOf("/"));
            }

            container.getEntryList().removeIf(tempYml -> tempYml.getName().endsWith("array.yml"));
            int minusParameterDirectory = container.getEntryList().size() - 1;
            container.add(itemFileEntry);
            String arrayDotYmpData = "size: " + (minusParameterDirectory + 1);
            container.add(new StorageDataEntry(
                FileHelper.write("array.yml", arrayDotYmpData.getBytes(StandardCharsets.UTF_8)),
                ymlPath + "/array.yml",
                StorageDataType.FILE
            ));
        }
        return container;
    }

    @Override
    public JsonNode createInputProvisioningEndpointResponse(JsonNode provision, Run run) {

        Type currentType = new CollectionType(this);
        while (currentType instanceof CollectionType) {
            currentType = ((CollectionType) currentType).getSubType();
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode provisionedParameter = mapper.createObjectNode();
        // if json has param_name then it's a collection
        if (provision.has("param_name")) {
            provisionedParameter.put("param_name", provision.get("param_name").asText());
        } else {
            provisionedParameter.put("index", provision.get("index").asText());
        }

        if (referenced && (currentType instanceof FileType || currentType instanceof ImageType)) {
            provisionedParameter.set("value", provision.get("value"));
        }

        if (!(currentType instanceof ImageType)
            && !(currentType instanceof GeometryType)
            && !(currentType instanceof FileType)) {
            provisionedParameter.set("value", provision.get("value"));
        }

        provisionedParameter.put("task_run_id", String.valueOf(run.getId()));
        return provisionedParameter;
    }

    @Override
    public CollectionValue createOutputProvisioningEndpointResponse(TypePersistence typePersistence)
        throws ProvisioningException {
        return (CollectionValue) buildNode(typePersistence);
    }

    @Override
    public TaskRunParameterValue createOutputProvisioningEndpointResponse(
        StorageData output, UUID id, String outputName) throws ProvisioningException {

        Type currentType = new CollectionType(this);
        while (currentType instanceof CollectionType) {
            currentType = ((CollectionType) currentType).getSubType();
        }
        CollectionValue collectionValue = new CollectionValue();
        String leafType = currentType.getClass().getSimpleName();
        Map<String, List<TaskRunParameterValue>> itemListsDictionary = new LinkedHashMap<>();

        output.sortShallowToDeep();
        for (StorageDataEntry entry : output.getEntryList()) {
            if (entry.getStorageDataType().equals(StorageDataType.DIRECTORY)) {
                String outerDirectoryName = outputName + "/";
                if (entry.getName().equals(outerDirectoryName)) {
                    collectionValue.setTaskRunId(id);
                    collectionValue.setType(ValueType.ARRAY);
                    collectionValue.setParameterName(outputName);

                    List<TaskRunParameterValue> items = new ArrayList<>();
                    itemListsDictionary.put(entry.getName(), items);
                    collectionValue.setValue(items);
                } else {
                    CollectionItemValue collectionItemValue = new CollectionItemValue();
                    collectionItemValue.setParameterName(entry.getName());
                    // prepare list for sub items
                    List<TaskRunParameterValue> items = new ArrayList<>();
                    itemListsDictionary.put(entry.getName(), items);
                    collectionItemValue.setValue(items);
                    // add this collection to parent collection
                    itemListsDictionary.get(entry.getName()).add(collectionItemValue);
                }
            }
            if (entry.getStorageDataType().equals(StorageDataType.FILE)) {
                if (entry.getName().endsWith("array.yml")) {
                    continue;
                }
                String entryValue = null;
                if (!(leafType.equals("FileType")
                    || leafType.equals("ImageType"))) {
                    entryValue = FileHelper.read(entry.getData(), getStorageCharset());
                }

                String fileName = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
                String fileNameWithoutExtension = null;
                if (fileName.contains(".")) {
                    fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
                } else {
                    fileNameWithoutExtension = fileName;
                }

                if (entry.getName().endsWith(".geojson")) {
                    String outerDirectoryName = outputName + "/";
                    if (entry.getName().equals(outerDirectoryName)) {
                        GeoCollectionValue geoJsonCollection = new GeoCollectionValue();
                        geoJsonCollection.setTaskRunId(id);
                        geoJsonCollection.setType(ValueType.ARRAY);
                        geoJsonCollection.setParameterName(outputName);
                        geoJsonCollection.setValue(entryValue);

                        return geoJsonCollection;
                    } else {
                        CollectionItemValue geoCollectionAsSubCollection = new CollectionItemValue();
                        geoCollectionAsSubCollection.setIndex(Integer.parseInt(fileNameWithoutExtension));
                        geoCollectionAsSubCollection.setValue(entryValue);

                        itemListsDictionary.get(entry.getName().substring(0,
                             entry.getName().lastIndexOf("/") + 1))
                            .add(geoCollectionAsSubCollection);
                    }
                    continue;
                }

                CollectionItemValue collectionItemValue = new CollectionItemValue();
                collectionItemValue.setIndex(Integer.parseInt(fileName));
                switch (leafType) {
                    case "IntegerType":
                        collectionItemValue.setValue(Integer.parseInt(entryValue));
                        collectionItemValue.setType(ValueType.INTEGER);
                        break;
                    case "StringType":
                        collectionItemValue.setValue(entryValue);
                        collectionItemValue.setType(ValueType.STRING);
                        break;
                    case "EnumerationType":
                        collectionItemValue.setValue(entryValue);
                        collectionItemValue.setType(ValueType.ENUMERATION);
                        break;
                    case "GeometryType":
                        collectionItemValue.setValue(entryValue);
                        collectionItemValue.setType(ValueType.GEOMETRY);
                        break;
                    case "NumberType":
                        collectionItemValue.setValue(Double.parseDouble(entryValue));
                        collectionItemValue.setType(ValueType.NUMBER);
                        break;
                    case "BooleanType":
                        collectionItemValue.setValue(Boolean.parseBoolean(entryValue));
                        collectionItemValue.setType(ValueType.BOOLEAN);
                        break;
                    case "DateTimeType":
                        collectionItemValue.setValue(Instant.parse(entryValue));
                        collectionItemValue.setType(ValueType.DATETIME);
                        break;
                    case "FileType":
                        collectionItemValue.setValue(null);
                        collectionItemValue.setType(ValueType.FILE);
                        break;
                    case "ImageType":
                        collectionItemValue.setValue(null);
                        collectionItemValue.setType(ValueType.IMAGE);
                        break;
                    default:
                        throw new ProvisioningException("unknown leaf type: " + leafType);
                }
                itemListsDictionary.get(entry.getName().substring(0, entry.getName().lastIndexOf("/") + 1)).add(collectionItemValue);

            }
        }
        return collectionValue;
    }

    private int getCollectionSize(StorageDataEntry output) throws ProvisioningException {
        String arrayYmlContent = FileHelper.read(output.getData(), getStorageCharset()).trim();
        if (arrayYmlContent.isEmpty()) {
            throw new ProvisioningException(ErrorCode.INTERNAL_MISSING_METADATA);
        }
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        JsonNode arrayYml;
        try {
            arrayYml = yamlReader.readTree(arrayYmlContent);
        } catch (JsonProcessingException e) {
            throw new ProvisioningException(ErrorCode.INTERNAL_INVALID_METADATA);
        }
        return arrayYml.get("size").asInt();
    }

    private TaskRunParameterValue buildNode(TypePersistence typePersistence) throws ProvisioningException {
        if (!(typePersistence instanceof CollectionPersistence collectionPersistence)) {
            return getCollectionItemValue(typePersistence);
        }

        // either items or compact value but not both
        boolean hasItems = !collectionPersistence.getItems().isEmpty();
        boolean isCompactValueMissing = collectionPersistence.getCompactValue() == null;
        if (hasItems && isCompactValueMissing) {
            CollectionValue collectionValue = new CollectionValue();
            collectionValue.setType(ValueType.ARRAY);
            collectionValue.setParameterName(collectionPersistence.getParameterName());
            collectionValue.setTaskRunId(collectionPersistence.getRunId());

            List<TaskRunParameterValue> items = new ArrayList<>();
            collectionValue.setValue(items);
            for (TypePersistence persistence : collectionPersistence.getItems()) {
                items.add(buildNode(persistence));
            }
            collectionValue.setSubType(items.get(0).getType());

            return collectionValue;
        }

        GeoCollectionValue geoCollectionValue = new GeoCollectionValue();
        geoCollectionValue.setType(ValueType.ARRAY);
        geoCollectionValue.setParameterName(collectionPersistence.getParameterName());
        geoCollectionValue.setTaskRunId(collectionPersistence.getRunId());
        geoCollectionValue.setValue(collectionPersistence.getCompactValue());

        return geoCollectionValue;
    }

    private CollectionItemValue getCollectionItemValue(TypePersistence typePersistence)
        throws ProvisioningException {

        CollectionItemValue collectionItemValue = new CollectionItemValue();
        String fileName = typePersistence.getParameterName().substring(
            typePersistence.getParameterName().lastIndexOf("[") + 1).replace("]", "");
        collectionItemValue.setIndex(Integer.parseInt(fileName));

        if (typePersistence instanceof IntegerPersistence ip) {
            collectionItemValue.setValue(ip.getValue());
            collectionItemValue.setType(ValueType.INTEGER);
        } else if (typePersistence instanceof StringPersistence sp) {
            collectionItemValue.setValue(sp.getValue());
            collectionItemValue.setType(ValueType.STRING);
        } else if (typePersistence instanceof GeometryPersistence gp) {
            collectionItemValue.setValue(gp.getValue());
            collectionItemValue.setType(ValueType.GEOMETRY);
        } else if (typePersistence instanceof NumberPersistence np) {
            collectionItemValue.setValue(np.getValue());
            collectionItemValue.setType(ValueType.NUMBER);
        } else if (typePersistence instanceof BooleanPersistence bp) {
            collectionItemValue.setValue(bp.isValue());
            collectionItemValue.setType(ValueType.BOOLEAN);
        } else if (typePersistence instanceof EnumerationPersistence ep) {
            collectionItemValue.setValue(ep.getValue());
            collectionItemValue.setType(ValueType.ENUMERATION);
        } else if (typePersistence instanceof DateTimePersistence dp) {
            collectionItemValue.setValue(dp.getValue());
            collectionItemValue.setType(ValueType.DATETIME);
        } else if (typePersistence instanceof ImagePersistence) {
            collectionItemValue.setType(ValueType.IMAGE);
        } else if (typePersistence instanceof FilePersistence) {
            collectionItemValue.setType(ValueType.FILE);
        } else if (typePersistence instanceof DirectoryPersistence) {
            collectionItemValue.setType(ValueType.IMAGE);
        } else {
            throw new ProvisioningException(ErrorCode.INTERNAL_UNKNOWN_SUBTYPE);
        }
        return collectionItemValue;
    }
}
