package be.cytomine.appengine.models.task.image;

import java.awt.Dimension;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import be.cytomine.appengine.dto.inputs.task.types.image.ImageTypeConstraint;
import be.cytomine.appengine.dto.inputs.task.types.image.ImageValue;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
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
import be.cytomine.appengine.models.task.formats.FileFormat;
import be.cytomine.appengine.repositories.image.ImagePersistenceRepository;
import be.cytomine.appengine.utils.AppEngineApplicationContext;
import be.cytomine.appengine.utils.units.Unit;

@SuppressWarnings("checkstyle:LineLength")
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ImageType extends Type {

    @Column(nullable = true)
    private String maxFileSize;

    @Column(nullable = true)
    private Integer maxWidth;

    @Column(nullable = true)
    private Integer maxHeight;

    @Column(nullable = true)
    private List<String> formats;

    @Transient
    private FileFormat format;

    public void setConstraint(ImageTypeConstraint constraint, JsonNode value) {
        switch (constraint) {
            case FORMATS:
                this.setFormats(parse(value.toString()));
                break;
            case MAX_FILE_SIZE:
                this.setMaxFileSize(value.asText());
                break;
            case MAX_WIDTH:
                this.setMaxWidth(value.asInt());
                break;
            case MAX_HEIGHT:
                this.setMaxHeight(value.asInt());
                break;
            default:
        }
    }

    public boolean hasConstraint(ImageTypeConstraint constraint) {
        return switch (constraint) {
            case FORMATS -> this.formats != null;
            case MAX_FILE_SIZE -> this.maxFileSize != null;
            case MAX_WIDTH -> this.maxWidth != null;
            case MAX_HEIGHT -> this.maxHeight != null;
            default -> false;
        };
    }

    private void validateImageFormat(File file) throws TypeValidationException {
        if (formats == null || formats.isEmpty()) {
            this.format = ImageFormatFactory.getGenericFormat();
            return;
        }

        List<FileFormat> checkers = formats
            .stream()
            .map(ImageFormatFactory::getFormat)
            .toList();

        this.format = checkers
            .stream()
            .filter(checker -> checker.checkSignature(file))
            .findFirst()
            .orElse(null);

        if (this.format == null) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_FORMAT);
        }
    }

    private void validateImageDimension(File file) throws TypeValidationException {
        if (maxWidth == null && maxHeight == null) {
            return;
        }

        Dimension dimension = format.getDimensions(file);
        if (dimension == null) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_DIMENSION);
        }

        if (maxWidth != null && dimension.getWidth() > maxWidth) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_WIDTH);
        }

        if (maxHeight != null && dimension.getHeight() > maxHeight) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_HEIGHT);
        }
    }

    private void validateImageSize(File file) throws TypeValidationException {
        if (maxFileSize == null) {
            return;
        }

        if (!Unit.isValid(maxFileSize)) {
            throw new TypeValidationException(
                ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_SIZE_FORMAT
            );
        }

        Unit unit = new Unit(maxFileSize);
        if (file.length() > unit.getBytes()) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_SIZE);
        }
    }

    @Override
    public void validate(Object valueObject) throws TypeValidationException {
        if (!(valueObject instanceof File) && !(valueObject instanceof String)) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR);
        }

        File file;
        if (valueObject instanceof String reference) {
            if (!Paths.get(reference).isAbsolute() || !new File(reference).isFile()) {
                throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR);
            }
            file = new File(reference);
        } else {
            file = (File) valueObject;
        }

        validateImageFormat(file);

        validateImageDimension(file);

        validateImageSize(file);

        /* Additional specific type validation */
        if (!format.validate(file)) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE);
        }
    }

    @Override
    public void validateFiles(
        Run run,
        Parameter currentOutput,
        StorageData currentOutputStorageData)
        throws TypeValidationException {

        // validate file structure
        File outputFile = getFileIfStructureIsValid(currentOutputStorageData);

        validate(outputFile);

    }

    @Override
    public void persistProvision(JsonNode provision, UUID runId) {
        String parameterName = provision.get("param_name").asText();
        ImagePersistenceRepository imagePersistenceRepository = AppEngineApplicationContext.getBean(ImagePersistenceRepository.class);
        ImagePersistence persistedProvision = imagePersistenceRepository.findImagePersistenceByParameterNameAndRunIdAndParameterType(parameterName, runId, ParameterType.INPUT);
        if (persistedProvision != null) {
            return;
        }

        persistedProvision = new ImagePersistence();
        persistedProvision.setParameterName(parameterName);
        persistedProvision.setParameterType(ParameterType.INPUT);
        persistedProvision.setRunId(runId);
        persistedProvision.setValueType(ValueType.IMAGE);
        persistedProvision.setProvisioned(true);
        imagePersistenceRepository.save(persistedProvision);
    }

    @Override
    public void persistResult(Run run, Parameter currentOutput, StorageData outputValue) {
        ImagePersistenceRepository imagePersistenceRepository = AppEngineApplicationContext.getBean(ImagePersistenceRepository.class);
        ImagePersistence result = imagePersistenceRepository.findImagePersistenceByParameterNameAndRunIdAndParameterType(currentOutput.getName(), run.getId(), ParameterType.OUTPUT);
        if (result != null) {
            return;
        }
        result = new ImagePersistence();
        result.setParameterType(ParameterType.OUTPUT);
        result.setParameterName(currentOutput.getName());
        result.setRunId(run.getId());
        result.setValueType(ValueType.IMAGE);

        imagePersistenceRepository.save(result);
    }

    @Override
    public StorageData mapToStorageFileData(JsonNode provision, Run run) {
        String parameterName = provision.get("param_name").asText();
        File data = new File(provision.get("value").asText());
        StorageDataEntry entry = new StorageDataEntry(data, parameterName, StorageDataType.FILE);
        return new StorageData(entry);
    }

    @Override
    public JsonNode createInputProvisioningEndpointResponse(JsonNode provision, Run run) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode provisionedParameter = mapper.createObjectNode();
        provisionedParameter.put("param_name", provision.get("param_name").asText());
        provisionedParameter.put("task_run_id", String.valueOf(run.getId()));
        return provisionedParameter;
    }

    @Override
    public ImageValue createOutputProvisioningEndpointResponse(StorageData output, UUID id, String outputName) {
        ImageValue imageValue = new ImageValue();
        imageValue.setParameterName(outputName);
        imageValue.setTaskRunId(id);
        imageValue.setType(ValueType.IMAGE);
        return imageValue;
    }

    @Override
    public ImageValue createOutputProvisioningEndpointResponse(TypePersistence typePersistence) {
        ImagePersistence imagePersistence = (ImagePersistence) typePersistence;
        ImageValue imageValue = new ImageValue();
        imageValue.setParameterName(imagePersistence.getParameterName());
        imageValue.setTaskRunId(imagePersistence.getRunId());
        imageValue.setType(ValueType.IMAGE);
        return imageValue;
    }
}
