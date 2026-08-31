package be.cytomine.appengine.unit.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import be.cytomine.appengine.dto.handlers.filestorage.Storage;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageDataEntry;
import be.cytomine.appengine.handlers.StorageDataType;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.handlers.storage.impl.FileSystemStorageHandler;
import be.cytomine.appengine.utils.FileHelper;

public class StorageTest {

    private static StorageHandler storageHandler;

    private static String basePath;

    @BeforeAll
    public static void init() {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties properties = yamlFactory.getObject();
        assert properties != null;
        basePath = properties.getProperty("storage.base-path");
        storageHandler = new FileSystemStorageHandler(basePath);
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        Path dirPath = Paths.get(basePath);
        Files.walk(dirPath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    @Test
    @DisplayName("Testing successful directory storage")
    public void successfulDirectoryStorageDataSave() throws FileStorageException {
        // creating directory
        Storage dir = new Storage("main");
        storageHandler.createStorage(dir);

        Path filePath = Paths.get(basePath, dir.id());
        Assertions.assertTrue(Files.exists(filePath));
    }

    @Test
    @DisplayName("Testing successful file storage")
    public void successfulFileStorageDataSave() throws IOException, FileStorageException {
        // Storing a primitive type integer file
        int value = 200;
        String valueString = new ObjectMapper().writeValueAsString(value);
        byte[] valueBytes = valueString.getBytes(StandardCharsets.UTF_8);

        StorageData integerStorageData = new StorageData(FileHelper.write("a", valueBytes), "a");
        Storage dir = new Storage("main");
        storageHandler.createStorage(dir);
        storageHandler.saveStorageData(dir, integerStorageData);

        Path filePath = Paths.get(basePath, dir.id(), "a");
        Assertions.assertTrue(Files.exists(filePath));
    }

    @Test
    @DisplayName("Testing successful nested directory structure")
    public void successfulNestDirectoriesStorageDataSave() throws IOException, FileStorageException {
        // Storing nested directories
        int value = 200;
        String valueString = new ObjectMapper().writeValueAsString(value);
        byte[] valueBytes = valueString.getBytes(StandardCharsets.UTF_8);

        StorageData nestedDirectory = new StorageData();
        StorageDataEntry mainDirectory = new StorageDataEntry(
            "folder",
            StorageDataType.DIRECTORY
        );
        StorageDataEntry subDirectory = new StorageDataEntry(
            "folder/subdir",
            StorageDataType.DIRECTORY
        );
        String randomText = "This is a random text";
        StorageDataEntry randomTextFile = new StorageDataEntry(
            FileHelper.write("random", randomText.getBytes(StandardCharsets.UTF_8)),
            "folder/subdir/random",
            StorageDataType.FILE
        );
        StorageDataEntry integerTextFile = new StorageDataEntry(
            FileHelper.write("integer", valueBytes),
            "integer",
            StorageDataType.FILE
        );

        nestedDirectory.add(integerTextFile);
        nestedDirectory.add(mainDirectory);
        nestedDirectory.add(subDirectory);
        nestedDirectory.add(randomTextFile);

        Storage dir = new Storage("main");
        storageHandler.saveStorageData(dir, nestedDirectory);

        Path integerPath = Paths.get(basePath + "/main/integer");
        Path folderPath = Paths.get(basePath + "/main/folder");
        Path subdirPath = Paths.get(basePath + "/main/folder/subdir");
        Path randomPath = Paths.get(basePath + "/main/folder/subdir/random");

        Assertions.assertTrue(Files.exists(integerPath));
        Assertions.assertTrue(Files.exists(folderPath));
        Assertions.assertTrue(Files.exists(subdirPath));
        Assertions.assertTrue(Files.exists(randomPath));
    }

    @Test
    @DisplayName("Testing successful nested directory structure merger")
    public void successfulNestDirectoriesStorageDataMergeSave() throws IOException, FileStorageException {
        // Storing nested directories
        int value = 200;
        String valueString = new ObjectMapper().writeValueAsString(value);
        byte[] valueBytes = valueString.getBytes(StandardCharsets.UTF_8);

        StorageData nestedDirectory = new StorageData();
        StorageDataEntry mainDirectory = new StorageDataEntry(
            "folder",
            StorageDataType.DIRECTORY
        );
        StorageDataEntry subDirectory = new StorageDataEntry(
            "folder/subdir",
            StorageDataType.DIRECTORY
        );
        String randomText = "This is a random text";
        StorageDataEntry randomTextFile = new StorageDataEntry(
            FileHelper.write("random", randomText.getBytes(StandardCharsets.UTF_8)),
            "folder/subdir/random",
            StorageDataType.FILE
        );
        StorageDataEntry integerTextFile = new StorageDataEntry(
            FileHelper.write("integer", valueBytes),
            "integer",
            StorageDataType.FILE
        );

        nestedDirectory.add(integerTextFile);
        nestedDirectory.add(mainDirectory);
        nestedDirectory.add(subDirectory);
        nestedDirectory.add(randomTextFile);

        StorageData secondNestedDirectory = new StorageData();
        String orderedText = "This is an ordered text";
        StorageDataEntry orderedTextFile = new StorageDataEntry(
            FileHelper.write("ordered", orderedText.getBytes(StandardCharsets.UTF_8)),
            "folder/subdir/ordered",
            StorageDataType.FILE
        );

        secondNestedDirectory.add(orderedTextFile);

        // merge
        nestedDirectory.merge(secondNestedDirectory);

        Storage dir = new Storage("main");
        storageHandler.saveStorageData(dir, nestedDirectory);

        Path integerPath = Paths.get(basePath + "/main/integer");
        Path folderPath = Paths.get(basePath + "/main/folder");
        Path subdirPath = Paths.get(basePath + "/main/folder/subdir");
        Path randomPath = Paths.get(basePath + "/main/folder/subdir/random");
        Path orderedPath = Paths.get(basePath + "/main/folder/subdir/ordered");

        Assertions.assertTrue(Files.exists(integerPath));
        Assertions.assertTrue(Files.exists(folderPath));
        Assertions.assertTrue(Files.exists(subdirPath));
        Assertions.assertTrue(Files.exists(randomPath));
        Assertions.assertTrue(Files.exists(orderedPath));
    }

    @Test
    @DisplayName("Should read the data successfully when data is a simple file")
    public void shouldReadDataSuccessfullyWhenDataIsSimpleFile() throws FileStorageException, JsonProcessingException {
        Storage testStorage = new Storage(UUID.randomUUID().toString());
        storageHandler.createStorage(testStorage);

        String parameterName = UUID.randomUUID().toString();
        int expectedValue = 200;
        String valueString = new ObjectMapper().writeValueAsString(expectedValue);
        byte[] valueBytes = valueString.getBytes(StandardCharsets.UTF_8);

        StorageData integerStorageData = new StorageData(FileHelper.write(parameterName, valueBytes), parameterName);
        storageHandler.saveStorageData(testStorage, integerStorageData);

        StorageData emptyFile = new StorageData(parameterName, testStorage.id());
        storageHandler.readStorageData(emptyFile);
        int actualValue = Integer.parseInt(FileHelper.read(emptyFile.peek().getData(), Charset.defaultCharset()));

        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    @DisplayName("Testing readStorageData with nested directory structure")
    public void testReadStorageDataWithNestedDirectoryStructure() throws FileStorageException {
        Storage storage = new Storage(UUID.randomUUID().toString());
        storageHandler.createStorage(storage);

        String arrayYmlContent = "size: 1";
        byte[] arrayYmlBytes = arrayYmlContent.getBytes(StandardCharsets.UTF_8);
        File arrayYmlFile = FileHelper.write("array.yml", arrayYmlBytes);
        StorageDataEntry arrayYmlEntry = new StorageDataEntry(
            arrayYmlFile,
            "nuclei/array.yml",
            StorageDataType.FILE
        );

        String expectedValue = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[0,1],[1,1],[1,0],[0,0]]]}";
        File subDirFile = FileHelper.write("0", expectedValue.getBytes(StandardCharsets.UTF_8));
        StorageDataEntry subDirFileEntry = new StorageDataEntry(subDirFile, "nuclei/0/0", StorageDataType.FILE);

        String subArrayYmlContent = "size: 1";
        byte[] subArrayYmlBytes = subArrayYmlContent.getBytes(StandardCharsets.UTF_8);
        File subArrayYmlFile = FileHelper.write("array.yml", subArrayYmlBytes);
        StorageDataEntry subArrayYmlEntry = new StorageDataEntry(
            subArrayYmlFile,
            "nuclei/0/array.yml",
            StorageDataType.FILE
        );

        StorageData nestedDirectory = new StorageData();
        nestedDirectory.add(arrayYmlEntry);
        nestedDirectory.add(subDirFileEntry);
        nestedDirectory.add(subArrayYmlEntry);

        storageHandler.saveStorageData(storage, nestedDirectory);

        StorageData emptyFile = new StorageData("nuclei", storage.id());
        storageHandler.readStorageData(emptyFile);

        List<String> expectedNames = List.of(
            "nuclei",
            "nuclei/array.yml",
            "nuclei/0",
            "nuclei/0/array.yml",
            "nuclei/0/0"
        );
        for (StorageDataEntry entry : emptyFile.getEntryList()) {
            Assertions.assertTrue(expectedNames.contains(entry.getName()), "Unexpected entry name: " + entry.getName());
        }
    }

    @Test
    @DisplayName("Should throw FileStorageException when failing to read the data")
    public void shouldThrowFileStorageExceptionOnReadFailed() throws FileStorageException {
        Storage testStorage = new Storage(UUID.randomUUID().toString());
        storageHandler.createStorage(testStorage);
        String parameterName = UUID.randomUUID().toString();

        FileStorageException exception = Assertions.assertThrows(
            FileStorageException.class, () -> {
                StorageData emptyFile = new StorageData(parameterName, testStorage.id());
                storageHandler.readStorageData(emptyFile);
            }
        );
        Assertions.assertEquals("Failed to read file " + parameterName, exception.getMessage());
    }

    @Test
    @DisplayName("Should delete directory when storage is deleted")
    public void shouldDeleteDirectoryWhenStorageIsDeleted() throws FileStorageException {
        Storage testStorage = new Storage("test-storage");
        storageHandler.createStorage(testStorage);
        Path filePath = Paths.get(basePath, testStorage.id());
        Assertions.assertTrue(Files.exists(filePath));

        storageHandler.deleteStorage(testStorage);

        Assertions.assertFalse(Files.exists(filePath));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent storage")
    public void shouldThrowExceptionWhenDeletingNonExistentStorage() {
        Storage nonExistentStorage = new Storage("nonexistent");

        Assertions.assertThrows(FileStorageException.class, () -> storageHandler.deleteStorage(nonExistentStorage));
    }
}
