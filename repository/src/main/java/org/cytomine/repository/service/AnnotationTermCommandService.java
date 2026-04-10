package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.AnnotationTermMapper;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.AnnotationTermRepository;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.AnnotationTermEntity;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.annotationterm.payload.CreateAnnotationTerm;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.AnnotationTermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateAnnotationTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteAnnotationTermCommand;

@Component
@AllArgsConstructor
public class AnnotationTermCommandService {
    private final AnnotationTermRepository annotationTermRepository;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final AnnotationTermMapper annotationTermMapper;

    @Transactional
    public Optional<HttpCommandResponse> createAnnotationTerm(Long userId, CreateAnnotationTerm payload,
                                                              LocalDateTime now) {
        AnnotationTermEntity entity = new AnnotationTermEntity();
        entity.setUserAnnotationId(payload.userAnnotationId());
        entity.setTermId(payload.termId());
        entity.setUserId(payload.userId());
        entity.setCreated(now);
        entity.setUpdated(now);
        entity.setVersion(0);
        AnnotationTermEntity saved = annotationTermRepository.save(entity);

        AnnotationTermCommandPayload commandPayload = annotationTermMapper.mapToCommandPayload(saved);
        CreateAnnotationTermCommand command = new CreateAnnotationTermCommand(commandPayload, userId);
        CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(command, now, now, userId));

        return Optional.of(new HttpCommandResponse(
            true,
            annotationTermMapper.mapToResponse(saved),
            commandV2Entity.getId(),
            Commands.CREATE_ANNOTATION_TERM
        ));
    }

    @Transactional
    public Optional<HttpCommandResponse> deleteAnnotationTerm(Long id, Long userId, LocalDateTime now) {
        return annotationTermRepository.findById(id).map(entity -> {
            AnnotationTermCommandPayload before = annotationTermMapper.mapToCommandPayload(entity);
            DeleteAnnotationTermCommand command = new DeleteAnnotationTermCommand(id, before, userId);
            CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(command, now, now, userId));
            entity.setDeleted(now);
            annotationTermRepository.save(entity);
            return new HttpCommandResponse(true, annotationTermMapper.mapToResponse(entity),
                commandV2Entity.getId(), Commands.DELETE_ANNOTATION_TERM);
        });
    }

    public Optional<HttpCommandResponse> undoCreateAnnotationTerm(UUID commandId,
                                                                   CreateAnnotationTermCommand cmd,
                                                                   Long userId, LocalDateTime now) {
        return softDelete(commandId, cmd.after().id(), Commands.CREATE_ANNOTATION_TERM, now);
    }

    public Optional<HttpCommandResponse> redoCreateAnnotationTerm(UUID commandId,
                                                                   CreateAnnotationTermCommand cmd,
                                                                   Long userId, LocalDateTime now) {
        return restore(commandId, cmd.after().id(), Commands.CREATE_ANNOTATION_TERM, now);
    }

    public Optional<HttpCommandResponse> undoDeleteAnnotationTerm(UUID commandId,
                                                                   DeleteAnnotationTermCommand cmd,
                                                                   Long userId, LocalDateTime now) {
        return restore(commandId, cmd.before().id(), Commands.DELETE_ANNOTATION_TERM, now);
    }

    public Optional<HttpCommandResponse> redoDeleteAnnotationTerm(UUID commandId,
                                                                   DeleteAnnotationTermCommand cmd,
                                                                   Long userId, LocalDateTime now) {
        return softDelete(commandId, cmd.before().id(), Commands.DELETE_ANNOTATION_TERM, now);
    }

    private Optional<HttpCommandResponse> softDelete(UUID commandId, long entityId, String command,
                                                      LocalDateTime now) {
        return annotationTermRepository.findById(entityId).map(entity -> {
            entity.setDeleted(now);
            annotationTermRepository.save(entity);
            return new HttpCommandResponse(true, annotationTermMapper.mapToResponse(entity), commandId, command);
        });
    }

    private Optional<HttpCommandResponse> restore(UUID commandId, long entityId, String command,
                                                   LocalDateTime now) {
        return annotationTermRepository.findById(entityId).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(now);
            annotationTermRepository.save(entity);
            return new HttpCommandResponse(true, annotationTermMapper.mapToResponse(entity), commandId, command);
        });
    }
}
