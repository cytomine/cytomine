package be.cytomine.service.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.utils.filters.SearchOperation;
import be.cytomine.utils.filters.SearchParameterEntry;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class UploadedFileServiceTests {

    @Autowired
    UploadedFileService uploadedFileService;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    EntityManager entityManager;

    @Test
    void listAllUploadedFileWithSuccess() {
        UploadedFile uploadedFile1 = builder.givenAUploadedFile();
        UploadedFile uploadedFile2 = builder.givenAUploadedFile();
        assertThat(uploadedFile1).isIn(uploadedFileService.list(Pageable.unpaged()));
        assertThat(uploadedFile2).isIn(uploadedFileService.list(Pageable.unpaged()));

        assertThat(uploadedFileService.list(Pageable.ofSize(1)).getContent()).asList().hasSize(1);
        assertThat(uploadedFileService.list(Pageable.ofSize(2)).getContent()).asList().hasSize(2);
    }

    @Test
    void listUserUploadedFileWithSuccess() {
        UploadedFile uploadedFile1 = builder.givenAUploadedFile();
        UploadedFile uploadedFileNotSameUser = builder.givenAUploadedFile();
        uploadedFileNotSameUser.setUser(builder.givenAUser());
        builder.persistAndReturn(uploadedFileNotSameUser);

        assertThat(uploadedFile1).isIn(uploadedFileService.list(
            builder.givenSuperAdmin(),
            null,
            false,
            Pageable.unpaged()
        ));
        assertThat(uploadedFileNotSameUser).isNotIn(uploadedFileService.list(
            builder.givenSuperAdmin(),
            null,
            false,
            Pageable.unpaged()
        ));

        assertThat(uploadedFileService.list(Pageable.ofSize(1)).getContent()).asList().hasSize(1);
        assertThat(uploadedFileService.list(Pageable.ofSize(2)).getContent()).asList().hasSize(2);
    }


    @Test
    void searchUploadedFileByUser() {
        UploadedFile uploadedFile1 = builder.givenAUploadedFile();
        UploadedFile uploadedFile2 = builder.givenAUploadedFile();
        UploadedFile uploadedFileNotSameUser = builder.givenAUploadedFile();
        uploadedFileNotSameUser.setUser(builder.givenAUser());
        builder.persistAndReturn(uploadedFileNotSameUser);

        List<SearchParameterEntry> searchParameter = new ArrayList<>();
        searchParameter.add(new SearchParameterEntry(
            "user",
            SearchOperation.in,
            List.of(builder.givenSuperAdmin().getId())
        ));

        List<Map<String, Object>> list = uploadedFileService.list(searchParameter, "created", "desc", false);

        assertThat(list.size()).isGreaterThanOrEqualTo(2);
        assertThat(list.stream().map(x -> x.get("id"))).contains(uploadedFile1.getId());
        assertThat(list.stream().map(x -> x.get("id"))).contains(uploadedFile2.getId());
        assertThat(list.stream().map(x -> x.get("id"))).doesNotContain(uploadedFileNotSameUser.getId());


        assertThat(list.get(0)).containsKey("originalFilename");
        assertThat(list.get(0)).containsKey("storageId");
        assertThat(list.get(0)).containsKey("thumbURL");
    }

    @Test
    void searchUploadedFileByOriginalFileName() {
        UploadedFile uploadedFile1 = builder.givenAUploadedFile();
        uploadedFile1.setOriginalFilename("redIsDead");
        builder.persistAndReturn(uploadedFile1);
        UploadedFile uploadedFile2 = builder.givenAUploadedFile();
        uploadedFile2.setOriginalFilename("deadline");
        builder.persistAndReturn(uploadedFile2);
        UploadedFile uploadedFileNoMatch = builder.givenAUploadedFile();
        uploadedFileNoMatch.setOriginalFilename("veracruz");
        builder.persistAndReturn(uploadedFileNoMatch);

        List<SearchParameterEntry> searchParameter = new ArrayList<>();
        searchParameter.add(new SearchParameterEntry("originalFilename", SearchOperation.ilike, "dead"));

        List<Map<String, Object>> list = uploadedFileService.list(searchParameter, "created", "desc", false);

        assertThat(list.size()).isGreaterThanOrEqualTo(2);
        assertThat(list.stream().map(x -> x.get("id"))).contains(uploadedFile1.getId());
        assertThat(list.stream().map(x -> x.get("id"))).contains(uploadedFile2.getId());
        assertThat(list.stream().map(x -> x.get("id"))).doesNotContain(uploadedFileNoMatch.getId());
    }

    @Test
    void listUploadedFileWithHierarchicalTree() {
        UploadedFile uploadedFileToAdd = builder.givenAUploadedFile();
        uploadedFileToAdd.setOriginalFilename("parent");
        builder.persistAndReturn(uploadedFileToAdd);

        UploadedFile uploadedfileChild = builder.givenAUploadedFile();
        uploadedfileChild.setOriginalFilename("child");
        uploadedfileChild.setParent(entityManager.find(UploadedFile.class, uploadedFileToAdd.getId()));
        builder.persistAndReturn(uploadedfileChild);

        UploadedFile uploadedfileSubChildToAdd = builder.givenAUploadedFile();
        uploadedfileSubChildToAdd.setParent(uploadedfileChild);
        builder.persistAndReturn(uploadedfileSubChildToAdd);


        Page<UploadedFile> list = uploadedFileService.list(builder.givenSuperAdmin(), null, true, Pageable.unpaged());
        assertThat(list.getContent()).contains(uploadedFileToAdd);
        assertThat(list.getContent()).doesNotContain(uploadedfileChild, uploadedfileSubChildToAdd);

        list = uploadedFileService.list(
            builder.givenSuperAdmin(),
            uploadedFileToAdd.getId(),
            false,
            Pageable.unpaged()
        );
        assertThat(list.getContent()).contains(uploadedfileChild);
        assertThat(list.getContent()).doesNotContain(uploadedFileToAdd, uploadedfileSubChildToAdd);


        List<Map<String, Object>> maps = uploadedFileService.listHierarchicalTree(
            builder.givenSuperAdmin(),
            uploadedFileToAdd.getId()
        );
        assertThat(maps.stream().filter(x -> x.get("id").equals(uploadedFileToAdd.getId()))).hasSize(1);
        assertThat(maps.stream().filter(x -> x.get("id").equals(uploadedfileChild.getId()))).hasSize(1);
        assertThat(maps.stream().filter(x -> x.get("id").equals(uploadedfileSubChildToAdd.getId()))).hasSize(1);

    }

    @Test
    void searchUploadedFileWithTreeDetails() {
        UploadedFile uploadedFile1 = builder.givenAUploadedFile();
        uploadedFile1.setOriginalFilename("redIsDead");
        builder.persistAndReturn(uploadedFile1);
        UploadedFile uploadedFile2 = builder.givenAUploadedFile();
        uploadedFile2.setOriginalFilename("deadline");
        builder.persistAndReturn(uploadedFile2);
        UploadedFile uploadedFileNoMatch = builder.givenAUploadedFile();
        uploadedFileNoMatch.setOriginalFilename("veracruz");
        builder.persistAndReturn(uploadedFileNoMatch);

        List<SearchParameterEntry> searchParameter = new ArrayList<>();
        searchParameter.add(new SearchParameterEntry("originalFilename", SearchOperation.ilike, "dead"));

        List<Map<String, Object>> list = uploadedFileService.list(searchParameter, "created", "desc", true);

        assertThat(list.size()).isGreaterThanOrEqualTo(2);
        assertThat(list.stream().map(x -> x.get("id"))).contains(uploadedFile1.getId());
        assertThat(list.stream().map(x -> x.get("id"))).contains(uploadedFile2.getId());
        assertThat(list.stream().map(x -> x.get("id"))).doesNotContain(uploadedFileNoMatch.getId());

        assertThat(list.get(0).get("nbChildren")).isNotNull();
        assertThat(list.get(0).get("globalSize")).isNotNull();
    }

    @Test
    void testLtree() {
        UploadedFile uploadedFileToAdd = builder.givenAUploadedFile();
        uploadedFileToAdd.setOriginalFilename("parent");
        builder.persistAndReturn(uploadedFileToAdd);

        entityManager.detach(uploadedFileToAdd);

        UploadedFile uploadedfileChildToAdd = builder.givenAUploadedFile();
        uploadedfileChildToAdd.setOriginalFilename("child");
        uploadedfileChildToAdd.setParent(entityManager.find(UploadedFile.class, uploadedFileToAdd.getId()));
        builder.persistAndReturn(uploadedfileChildToAdd);


        UploadedFile uploadedfileSubChildToAdd = builder.givenAUploadedFile();
        uploadedfileSubChildToAdd.setParent(uploadedfileChildToAdd);
        builder.persistAndReturn(uploadedfileSubChildToAdd);

        UploadedFile uploadedfileSubSubChildToAdd = builder.givenAUploadedFile();
        uploadedfileSubSubChildToAdd.setParent(uploadedfileSubChildToAdd);
        builder.persistAndReturn(uploadedfileSubSubChildToAdd);


        assertThat(uploadedfileSubSubChildToAdd.getLTree()).contains(uploadedfileSubChildToAdd.getLTree());
        assertThat(uploadedfileSubChildToAdd.getLTree()).contains(uploadedfileChildToAdd.getLTree());
        assertThat(uploadedfileChildToAdd.getLTree()).contains(uploadedFileToAdd.getLTree());

        assertThat(uploadedfileSubSubChildToAdd.getParent().getId()).isEqualTo(uploadedfileSubChildToAdd.getId());
    }
}
