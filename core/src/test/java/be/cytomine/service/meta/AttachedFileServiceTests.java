package be.cytomine.service.meta;

import jakarta.transaction.Transactional;
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
import be.cytomine.domain.meta.AttachedFile;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ObjectNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class AttachedFileServiceTests {

    @Autowired
    AttachedFileService attachedFileService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    public void listAttachedFile() {
        AttachedFile attachedFile = builder.givenAnAttachedFile(builder.givenAProject());
        assertThat(attachedFileService.list()).contains(attachedFile);
    }

    @Test
    public void listAttachedFileForDomain() {
        Project project = builder.givenAProject();
        AttachedFile attachedFile = builder.givenAnAttachedFile(project);
        assertThat(attachedFileService.findAllByDomain(project)).contains(attachedFile);
    }

    @Test
    public void listAttachedFileForDomainThatDoNotExists() {
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                attachedFileService.findAllByDomain(Project.class.getName(), 0L);
            }
        );
    }

    @Test
    public void findById() {
        AttachedFile attachedFile = builder.givenAnAttachedFile(builder.givenAProject());
        assertThat(attachedFileService.findById(attachedFile.getId())).isPresent();
    }

    @Test
    public void findByIdThatDoNotExists() {
        assertThat(attachedFileService.findById(0L)).isEmpty();
    }

    @Test
    public void createAttachedFile() throws ClassNotFoundException {
        Project project = builder.givenAProject();
        AttachedFile attachedFile =
            attachedFileService.create(
                "test.txt",
                "hello".getBytes(),
                "test",
                project.getId(),
                project.getClass().getName()
            );
        assertThat(attachedFile).isNotNull();
    }

    @Test
    public void deleteAttachedFile() {
        AttachedFile attachedFile = builder.givenAnAttachedFile(builder.givenAProject());
        attachedFileService.delete(attachedFile, null, null, false);
        assertThat(attachedFileService.findById(attachedFile.getId())).isEmpty();
    }

}
