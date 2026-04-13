package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.AnnotationTermMapper;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.AnnotationTermRepository;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.UserAnnotationRepository;
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
    private final UserAnnotationRepository userAnnotationRepository;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final AnnotationTermMapper annotationTermMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse> createAnnotationTerm(Long userId, CreateAnnotationTerm payload,
                                                              LocalDateTime now) {
        return userAnnotationRepository.findById(payload.userAnnotationId())
            .filter(ua -> aclService.canReadProject(userId, ua.getProjectId()))
            .map(ua -> {
                AnnotationTermEntity saved = annotationTermRepository.save(
                    annotationTermMapper.mapToEntity(payload, now));
                AnnotationTermCommandPayload commandPayload = annotationTermMapper.mapToCommandPayload(saved);
                CreateAnnotationTermCommand command = new CreateAnnotationTermCommand(commandPayload, userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                return new HttpCommandResponse(
                    true,
                    annotationTermMapper.mapToResponse(saved),
                    commandV2Entity.getId(),
                    Commands.CREATE_ANNOTATION_TERM
                );
            });
    }

    @Transactional
    public Optional<HttpCommandResponse> deleteAnnotationTerm(Long id, Long userId, LocalDateTime now) {
        return annotationTermRepository.findById(id)
            .filter(entity -> userAnnotationRepository.findById(entity.getUserAnnotationId())
                .map(ua -> aclService.canEditForOwner(userId, ua.getProjectId(), ua.getUserId()))
                .orElse(false))
            .map(entity -> {
                AnnotationTermCommandPayload before = annotationTermMapper.mapToCommandPayload(entity);
                DeleteAnnotationTermCommand command = new DeleteAnnotationTermCommand(id, before, userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                entity.setDeleted(now);
                annotationTermRepository.save(entity);
                return new HttpCommandResponse(true, annotationTermMapper.mapToResponse(entity),
                    commandV2Entity.getId(), Commands.DELETE_ANNOTATION_TERM);
            });
    }

    public Optional<HttpCommandResponse> undoCreateAnnotationTerm(UUID commandId,
                                                                  CreateAnnotationTermCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        return withEditAccess(userId, cmd.after().userAnnotationId(),
            () -> softDelete(commandId, cmd.after().id(), Commands.CREATE_ANNOTATION_TERM, now));
    }

    public Optional<HttpCommandResponse> redoCreateAnnotationTerm(UUID commandId,
                                                                  CreateAnnotationTermCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        return withReadAccess(userId, cmd.after().userAnnotationId(),
            () -> restore(commandId, cmd.after().id(), Commands.CREATE_ANNOTATION_TERM, now));
    }

    public Optional<HttpCommandResponse> undoDeleteAnnotationTerm(UUID commandId,
                                                                  DeleteAnnotationTermCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        return withReadAccess(userId, cmd.before().userAnnotationId(),
            () -> restore(commandId, cmd.before().id(), Commands.DELETE_ANNOTATION_TERM, now));
    }

    public Optional<HttpCommandResponse> redoDeleteAnnotationTerm(UUID commandId,
                                                                  DeleteAnnotationTermCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        return withEditAccess(userId, cmd.before().userAnnotationId(),
            () -> softDelete(commandId, cmd.before().id(), Commands.DELETE_ANNOTATION_TERM, now));
    }

    private Optional<HttpCommandResponse> withReadAccess(Long userId, long userAnnotationId,
                                                         Supplier<Optional<HttpCommandResponse>> action) {
        return userAnnotationRepository.findById(userAnnotationId)
            .filter(ua -> aclService.canReadProject(userId, ua.getProjectId()))
            .flatMap(ua -> action.get());
    }

    private Optional<HttpCommandResponse> withEditAccess(Long userId, long userAnnotationId,
                                                         Supplier<Optional<HttpCommandResponse>> action) {
        return userAnnotationRepository.findById(userAnnotationId)
            .filter(ua -> aclService.canEditForOwner(userId, ua.getProjectId(), ua.getUserId()))
            .flatMap(ua -> action.get());
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
