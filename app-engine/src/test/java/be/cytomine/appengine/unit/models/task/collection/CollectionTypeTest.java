package be.cytomine.appengine.unit.models.task.collection;

import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageDataEntry;
import be.cytomine.appengine.handlers.StorageDataType;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.models.task.collection.CollectionType;
import be.cytomine.appengine.models.task.image.ImageType;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionTypeTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private Run run;

    @BeforeEach
    void setUp() {
        run = new Run();
    }

    private boolean hasEntryEndingWith(StorageData sd, String suffix) {
        return sd.getEntryList().stream().anyMatch(e -> e.getName().endsWith(suffix));
    }

    private String readText(StorageDataEntry entry) throws IOException {
        return Files.readString(entry.getData().toPath());
    }

    @Test
    void shouldProvisionArrayOfAnnotationImageCorrectly() throws Exception {
        CollectionType ct = new CollectionType();
        ct.setMinSize(1);
        ct.setMaxSize(10);
        ct.setSubType(new ImageType());

        String json = """
            {"param_name":"images","value":{"ids":[243,224,220],"type":"annotation"}}
            """;
        JsonNode provision = mapper.readTree(json);

        StorageData result = ct.mapToStorageFileData(provision, run);

        assertThat(result.getEntryList()).isNotEmpty();

        StorageDataEntry imageFile = result.getEntryList().stream()
            .filter(e -> e.getStorageDataType() == StorageDataType.FILE && e.getName().endsWith("/images"))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                "Expected a FILE entry at /images but got: "
                    + result.getEntryList().stream().map(StorageDataEntry::getName).toList())
            );

        assertThat(readText(imageFile)).isEmpty();
        assertThat(hasEntryEndingWith(result, "array.yml")).isTrue();
        assertThat(imageFile.getName()).doesNotEndWith(".geojson");
    }
}
