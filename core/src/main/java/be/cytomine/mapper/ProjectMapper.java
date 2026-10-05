package be.cytomine.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import be.cytomine.common.mapper.BaseMapper;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;

@Mapper(componentModel = "spring", uses = BaseMapper.class)
public interface ProjectMapper {

    @Mapping(target = "ontology", ignore = true)
    @Mapping(target = "mode", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "callBack", ignore = true)
    @Mapping(target = "representativeUsers", ignore = true)
    @Mapping(target = "closed", source = "isClosed")
    @Mapping(target = "countImages", source = "numberOfImages")
    @Mapping(target = "countAnnotations", source = "numberOfAnnotations")
    @Mapping(target = "countJobAnnotations", source = "numberOfJobAnnotations")
    @Mapping(target = "countReviewedAnnotations", source = "numberOfReviewedAnnotations")
    @BeanMapping(ignoreUnmappedSourceProperties = {"dataType", "deleted", "ontology", "ontologyName",
        "isReadOnly", "isRestricted"})
    Project map(ProjectResponse projectResponse);

    default Date map(LocalDateTime value) {
        return value == null ? null : Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    @AfterMapping
    default void toLegacyProject(ProjectResponse projectResponse, @MappingTarget Project project) {
        project.setMode(projectResponse.isReadOnly() ? EditingMode.READ_ONLY
            : projectResponse.isRestricted() ? EditingMode.RESTRICTED
            : EditingMode.CLASSIC);
        projectResponse.ontology().ifPresent(ontologyId -> {
            Ontology ontology = new Ontology();
            ontology.setId(ontologyId);
            ontology.setName(projectResponse.ontologyName().orElse(null));
            project.setOntology(ontology);
        });
    }
}
