package be.cytomine.appengine.unit.models.task.collection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import be.cytomine.appengine.dto.handlers.filestorage.Storage;
import be.cytomine.appengine.dto.inputs.task.GenericParameterCollectionItemProvision;
import be.cytomine.appengine.dto.inputs.task.GenericParameterProvision;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionItemValue;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageDataEntry;
import be.cytomine.appengine.handlers.StorageDataType;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.handlers.storage.impl.FileSystemStorageHandler;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.models.task.collection.CollectionType;
import be.cytomine.appengine.models.task.integer.IntegerType;
import be.cytomine.appengine.utils.AppEngineApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CollectionTypeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    private Path storageBasePath;

    private StorageHandler storageHandler;

    @BeforeEach
    void wireStorage() {
        storageHandler = new FileSystemStorageHandler(storageBasePath.toString());

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(StorageHandler.class)).thenReturn(storageHandler);
        new AppEngineApplicationContext().setApplicationContext(applicationContext);
    }

    private Run newRun() {
        Run run = new Run();
        run.setId(UUID.randomUUID());
        return run;
    }

    private void persist(Run run, StorageData storageData) throws FileStorageException {
        storageHandler.saveStorageData(new Storage("task-run-inputs-" + run.getId()), storageData);
    }

    private String readArrayYmlContent(StorageData storageData) throws IOException {
        StorageDataEntry arrayYml = storageData.getEntryList().stream()
            .filter(e -> e.getStorageDataType() == StorageDataType.FILE)
            .filter(e -> e.getName().endsWith("array.yml"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No array.yml entry found in: " + storageData.getEntryList()
                .stream()
                .map(StorageDataEntry::getName)
                .toList())
            );
        return Files.readString(arrayYml.getData().toPath(), StandardCharsets.UTF_8);
    }

    @Test
    void singleItemProvisionShouldWriteCorrectArrayYmlSize() throws FileStorageException, IOException {
        CollectionType ct = new CollectionType();
        ct.setMinSize(1);
        ct.setMaxSize(10);
        ct.setSubType(new IntegerType());

        GenericParameterCollectionItemProvision item = new GenericParameterCollectionItemProvision();
        item.setIndex("nuclei/0");
        item.setValue(42);
        JsonNode provision = mapper.valueToTree(item);

        StorageData result = ct.mapToStorageFileData(provision, newRun());
        String arrayYmlContent = readArrayYmlContent(result);

        assertThat(arrayYmlContent).isEqualTo("size: 1");
    }

    @Test
    void multipleSingleItemProvisionsShouldWriteCorrectArrayYmlSize() throws FileStorageException, IOException {
        CollectionType ct = new CollectionType();
        ct.setMinSize(1);
        ct.setMaxSize(10);
        ct.setSubType(new IntegerType());

        GenericParameterCollectionItemProvision item0 = new GenericParameterCollectionItemProvision();
        item0.setIndex("nuclei/0");
        item0.setValue(1);

        GenericParameterCollectionItemProvision item1 = new GenericParameterCollectionItemProvision();
        item1.setIndex("nuclei/1");
        item1.setValue(2);

        GenericParameterCollectionItemProvision item2 = new GenericParameterCollectionItemProvision();
        item2.setIndex("nuclei/2");
        item2.setValue(3);

        Run run = newRun();

        StorageData result0 = ct.mapToStorageFileData(mapper.valueToTree(item0), run);
        persist(run, result0);
        StorageData result1 = ct.mapToStorageFileData(mapper.valueToTree(item1), run);
        persist(run, result1);
        StorageData result2 = ct.mapToStorageFileData(mapper.valueToTree(item2), run);
        persist(run, result2);

        assertThat(readArrayYmlContent(result0)).isEqualTo("size: 1");
        assertThat(readArrayYmlContent(result1)).isEqualTo("size: 2");
        assertThat(readArrayYmlContent(result2)).isEqualTo("size: 3");
    }

    @Test
    void batchItemProvisionShouldWriteCorrectArrayYmlSize() throws FileStorageException, IOException {
        CollectionType ct = new CollectionType();
        ct.setMinSize(1);
        ct.setMaxSize(10);
        ct.setSubType(new IntegerType());

        GenericParameterProvision provision = new GenericParameterProvision();
        provision.setParameterName("nuclei");
        provision.setValue(List.of(
            new CollectionItemValue(0, 1),
            new CollectionItemValue(1, 2),
            new CollectionItemValue(2, 3)
        ));
        JsonNode provisionNode = mapper.valueToTree(provision);

        StorageData result = ct.mapToStorageFileData(provisionNode, newRun());

        String arrayYmlContent = readArrayYmlContent(result);
        assertThat(arrayYmlContent).isEqualTo("size: 3");
    }
}
