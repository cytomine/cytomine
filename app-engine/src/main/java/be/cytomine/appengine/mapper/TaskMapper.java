package be.cytomine.appengine.mapper;

import java.util.Optional;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.appengine.dto.inputs.task.TaskAuthor;
import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.models.task.Author;
import be.cytomine.appengine.models.task.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {
        "cpus",
        "createdDate",
        "descriptorFile",
        "gpus",
        "imageName",
        "inputFolder",
        "lastModifiedDate",
        "matches",
        "nameShort",
        "outputFolder",
        "parameters",
        "ram",
        "runs",
        "storageReference",
        "vrsn"
    })
    @Mapping(source = "identifier", target = "id")
    TaskDescription toTaskDescription(Task task);

    @BeanMapping(ignoreUnmappedSourceProperties = {"createdDate", "id", "lastModifiedDate", "vrsn"})
    @Mapping(source = "contact", target = "isContact")
    TaskAuthor toTaskAuthor(Author author);

    default Optional<String> mapDescription(String description) {
        return Optional.ofNullable(description);
    }
}
