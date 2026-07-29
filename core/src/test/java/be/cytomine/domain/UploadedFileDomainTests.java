package be.cytomine.domain;

import java.util.Arrays;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.repository.image.UploadedFileRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class UploadedFileDomainTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    UploadedFileRepository uploadedFileRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void supportLongArrayRead() {
        for (UploadedFile uploadedFile : uploadedFileRepository.findAll()) {
            System.out.print(uploadedFile.getId() + " => ");
            if (uploadedFile.getProjects() != null) {
                Arrays.stream(uploadedFile.getProjects()).forEach(System.out::println);
            }
            System.out.println();
        }
    }

    @Test
    void supportLongArrayPersistence() {
        UploadedFile uploadedFile = builder.givenANotPersistedUploadedFile();
        uploadedFile.setProjects(new Long[] {1L, 2L, 3L});
        uploadedFile = uploadedFileRepository.save(uploadedFile);
        entityManager.flush();
        Long id = uploadedFile.getId();
        entityManager.refresh(uploadedFile);
        System.out.println("id = " + id);
        uploadedFile = entityManager.find(UploadedFile.class, id);
        assertThat(uploadedFile).isNotNull();
        assertThat(uploadedFile.getProjects()).isNotNull();
        assertThat(uploadedFile.getProjects()).contains(1L, 2L, 3L);
    }
}
