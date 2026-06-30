package be.cytomine.appengine.unit.models.task.collection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import be.cytomine.appengine.dto.inputs.task.GenericParameterCollectionItemProvision;
import be.cytomine.appengine.dto.inputs.task.GenericParameterProvision;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionItemValue;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageDataEntry;
import be.cytomine.appengine.handlers.StorageDataType;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.models.task.collection.CollectionType;
import be.cytomine.appengine.models.task.integer.IntegerType;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionTypeTest {

    private final ObjectMapper mapper = new ObjectMapper();

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

        StorageData result = ct.mapToStorageFileData(provision, new Run());
        String arrayYmlContent = readArrayYmlContent(result);

        assertThat(arrayYmlContent).isEqualTo("size: 1");
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

        StorageData result = ct.mapToStorageFileData(provisionNode, new Run());

        String arrayYmlContent = readArrayYmlContent(result);
        assertThat(arrayYmlContent).isEqualTo("size: 3");
    }
}
