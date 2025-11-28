package be.cytomine.appengine.models.task.image;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;

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
import be.cytomine.appengine.models.task.formats.WSIDicomFormat;
import be.cytomine.appengine.models.task.formats.ZipFormat;
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

    @Transient
    @Value("${storage.base-path}")
    private static String storageBasePath;

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
        File toValidate = getBaselineImage(file);

        Dimension dimension = format.getDimensions(toValidate);
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

    private File getBaselineImage(File file) throws TypeValidationException {
        File toValidate;
        if (format instanceof WSIDicomFormat) {
            try {
                toValidate = Files.walk(file.toPath())
                    .filter(Files::isRegularFile)
                    .max(Comparator.comparingLong(p -> p.toFile().length()))
                    .map(Path::toFile)
                    .orElse(null);
            } catch (IOException e) {
                throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_BASELINE_IMAGE_FAILED);
            }
        } else {
            toValidate = file;
        }
        return toValidate;
    }

    private void validateImageSize(File file) throws TypeValidationException {
        if (maxFileSize == null) {
            return;
        }

        File toValidate = getBaselineImage(file);

        if (!Unit.isValid(maxFileSize)) {
            throw new TypeValidationException(
                ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_SIZE_FORMAT
            );
        }

        Unit unit = new Unit(maxFileSize);
        if (toValidate.length() > unit.getBytes()) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_SIZE);
        }
    }

    public static void unzip(File zipFile) throws IOException, TypeValidationException {
        Path targetDir = zipFile.toPath();

        Path tempSourcePath = targetDir.resolveSibling(targetDir.getFileName().toString() + ".tmp");

        try {
            Files.move(targetDir, tempSourcePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR_NO_MOVE);
        }

        Files.createDirectories(targetDir);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(tempSourcePath))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                // WARNING: No Zip Slip Protection is active here.
                Path newPath = targetDir.resolve(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null && !Files.exists(newPath.getParent())) {
                        Files.createDirectories(newPath.getParent());
                    }
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            // Re-throw exception indicating failure but allowing cleanup if necessary
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR_NO_UNZIP);
        }

        try {
            Files.delete(tempSourcePath);
        } catch (IOException e) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR_NO_DELETE);
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

        ZipFormat zipFormat = new ZipFormat();
        if (zipFormat.checkSignature(file)) {
            WSIDicomFormat dicomFormat = new WSIDicomFormat();
            dicomFormat.validateZippedWSIDicom(file);
            try {
                unzip(file);
            } catch (IOException e) {
                e.printStackTrace();
                throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR);
            }
        } else {
            validateImageFormat(file);
            validateImageDimension(file);
            validateImageSize(file);

            /* Additional specific type validation */
            if (!format.validate(file)) {
                throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE);
            }
        }

    }

    public File getDirectoryIfStructureIsValid(StorageData currentOutputStorageData)
        throws TypeValidationException {
        // validate file structure
        File outputFile = currentOutputStorageData.peek().getData();
        // if this is a directory-based image like wsi dicom
        if (currentOutputStorageData.peek().getStorageDataType().equals(StorageDataType.DIRECTORY)) {
            outputFile = new File(storageBasePath
                + "/"
                + currentOutputStorageData.peek().getStorageId()
                + "/"
                + currentOutputStorageData.peek().getName());
            // here we assume the format is always wsidicom until other directory-based formats are supported
            format = new WSIDicomFormat();
        } else {

            if (!outputFile.exists()) {
                throw new TypeValidationException(
                    ErrorCode.INTERNAL_MISSING_OUTPUT_FILE_FOR_PARAMETER
                );
            }

            if (outputFile.isDirectory()) {
                throw new TypeValidationException(
                    ErrorCode.INTERNAL_OUTPUT_FILE_FOR_PARAMETER_IS_DIRECTORY
                );
            }

            if (currentOutputStorageData.getEntryList().size() > 1) {
                throw new TypeValidationException(
                    ErrorCode.INTERNAL_EXTRA_OUTPUT_FILES_FOR_PARAMETER
                );
            }

        }

        return outputFile;
    }

    @Override
    public void validateFiles(
        Run run,
        Parameter currentOutput,
        StorageData currentOutputStorageData)
        throws TypeValidationException {

        // validate file structure
        File outputFile = getDirectoryIfStructureIsValid(currentOutputStorageData);

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
