package be.cytomine.appengine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.appengine.dto.inputs.task.TaskAuthor;
import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.models.task.Author;
import be.cytomine.appengine.models.task.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(source = "identifier", target = "id")
    TaskDescription toTaskDescription(Task task);

    @Mapping(source = "contact", target = "isContact")
    TaskAuthor toTaskAuthor(Author author);
}
