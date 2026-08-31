package be.cytomine.service.image;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.CytomineException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.image.UploadedFileRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.UrlApi;
import be.cytomine.service.command.TransactionService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = SUPERADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class AbstractSliceServiceTests {

    @Autowired
    AbstractSliceService abstractSliceService;

    @Autowired
    UploadedFileRepository uploadedFileRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ImageInstanceService imageInstanceService;
    @Autowired
    private UrlApi urlApi;

    @Test
    void listAllImageByAbstractImage() {
        AbstractImage image1 = builder.givenAnAbstractImage();
        AbstractImage image2 = builder.givenAnAbstractImage();

        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        abstractSlice1.setImage(image1);
        builder.persistAndReturn(abstractSlice1);
        AbstractSlice abstractSlice2 = builder.givenAnAbstractSlice();
        abstractSlice2.setImage(image2);
        builder.persistAndReturn(abstractSlice2);

        assertThat(abstractSliceService.list(image1)).contains(abstractSlice1);
        assertThat(abstractSliceService.list(image1)).doesNotContain(abstractSlice2);
    }

    @Test
    void listAllImageByUploadedFile() {
        UploadedFile file1 = builder.givenAUploadedFile();
        UploadedFile file2 = builder.givenAUploadedFile();

        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        abstractSlice1.setUploadedFile(file1);
        builder.persistAndReturn(abstractSlice1);
        AbstractSlice abstractSlice2 = builder.givenAnAbstractSlice();
        abstractSlice2.setUploadedFile(file2);
        builder.persistAndReturn(abstractSlice2);

        assertThat(abstractSliceService.list(file1)).contains(abstractSlice1);
        assertThat(abstractSliceService.list(file1)).doesNotContain(abstractSlice2);
    }

    @Test
    void findAbstractSliceByImageAndCoordinates() {

        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        abstractSlice1.setChannel(1);
        abstractSlice1.setZStack(2);
        abstractSlice1.setTime(3);
        builder.persistAndReturn(abstractSlice1);
        AbstractSlice abstractSlice2 = builder.givenAnAbstractSlice();
        abstractSlice2.setImage(abstractSlice1.getImage());
        abstractSlice2.setChannel(1);
        abstractSlice2.setZStack(2);
        abstractSlice2.setTime(4);
        builder.persistAndReturn(abstractSlice2);

        assertThat(abstractSliceService.find(abstractSlice1.getImage(), 1, 2, 3)).isPresent();
        assertThat(abstractSliceService.find(abstractSlice1.getImage(), 1, 2, 4)).isPresent();
        assertThat(abstractSliceService.find(abstractSlice1.getImage(), 2, 2, 3)).isEmpty();
    }

    @Test
    void findAbstractSliceImageUploaded() {
        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        assertThat(abstractSliceService.findImageUploaded(abstractSlice1.getId()))
            .isEqualTo(abstractSlice1.getUploadedFile().getUser());
    }

    @Test
    void getUnexistingAbstractSliceReturnNull() {
        assertThat(abstractSliceService.get(0L)).isNull();
    }

    @Test
    void findAbstractSliceWithSuccess() {
        AbstractSlice abstractSlice = builder.givenAnAbstractSlice();
        assertThat(abstractSliceService.find(abstractSlice.getId()).isPresent());
        assertThat(abstractSlice).isEqualTo(abstractSliceService.find(abstractSlice.getId()).get());
    }

    @Test
    void findUnexistingAbstractSliceReturnEmpty() {
        assertThat(abstractSliceService.find(0L)).isEmpty();
    }

    @Test
    void addValidAbstractSliceWithSuccess() {
        AbstractSlice abstractSlice = builder.givenANotPersistedAbstractSlice();

        CommandResponse commandResponse = abstractSliceService.add(abstractSlice.toJsonObject(urlApi));

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(abstractSliceService.find(commandResponse.getObject().getId())).isPresent();
        AbstractSlice created = abstractSliceService.find(commandResponse.getObject().getId()).get();
    }

    @Test
    void addAlreadyExistingAbstractSlice() {
        AbstractSlice abstractSlice = builder.givenAnAbstractSlice();
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                abstractSliceService.add(abstractSlice.toJsonObject(urlApi).withChange("id", null));
            }
        );
    }

    @Test
    void addValidAbstractSliceWithNullAbstractImageFails() {
        AbstractSlice abstractSlice = builder.givenANotPersistedAbstractSlice();
        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                abstractSliceService.add(abstractSlice.toJsonObject(urlApi).withChange("image", null));
            }
        );
    }

    @Test
    void editAbstractSliceWithSuccess() {
        AbstractSlice abstractSlice = builder.givenANotPersistedAbstractSlice();
        abstractSlice.setChannel(1);
        abstractSlice.setZStack(10);
        abstractSlice.setTime(100);
        abstractSlice = builder.persistAndReturn(abstractSlice);

        JsonObject jsonObject = abstractSlice.toJsonObject(urlApi);
        jsonObject.put("channel", 2);
        jsonObject.put("zStack", 20);
        jsonObject.put("time", 200);

        CommandResponse commandResponse = abstractSliceService.edit(jsonObject, true);
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(abstractSliceService.find(commandResponse.getObject().getId())).isPresent();
        AbstractSlice updated = abstractSliceService.find(commandResponse.getObject().getId()).get();

        assertThat(updated.getChannel()).isEqualTo(2);
        assertThat(updated.getZStack()).isEqualTo(20);
        assertThat(updated.getTime()).isEqualTo(200);
    }

    @Test
    void deleteAbstractSliceWithSuccess() {
        AbstractSlice abstractSlice = builder.givenAnAbstractSlice();

        CommandResponse commandResponse = abstractSliceService.delete(abstractSlice, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(abstractSliceService.find(abstractSlice.getId()).isEmpty());
    }

    @Test
    void deleteAbstractSliceWithDependenciesWithSuccess() {
        SliceInstance sliceInstance = builder.givenASliceInstance();
        Assertions.assertThrows(
            CytomineException.class, () -> {
                abstractSliceService.delete(sliceInstance.getBaseSlice(), null, null, false);
            }
        );
    }

}
