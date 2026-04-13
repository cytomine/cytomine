package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.UserAnnotationMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.UserAnnotationRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.UserAnnotationEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.UserAnnotationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateUserAnnotationCommand;
import be.cytomine.common.repository.model.command.request.DeleteUserAnnotationCommand;
import be.cytomine.common.repository.model.command.request.UpdateUserAnnotationCommand;
import be.cytomine.common.repository.model.userannotation.payload.CreateUserAnnotation;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;

@Component
@AllArgsConstructor
public class UserAnnotationCommandService {
    private final UserAnnotationRepository userAnnotationRepository;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final UserAnnotationMapper userAnnotationMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse> createUserAnnotation(Long userId, CreateUserAnnotation payload,
                                                              LocalDateTime now) {
        if (!aclService.canReadProject(userId, payload.projectId())) {
            return Optional.empty();
        }
        long newId = userAnnotationRepository.insertWithGeometry(now, now, payload.userId(), payload.imageId(),
            payload.sliceId(), payload.projectId(), payload.wktLocation(), payload.geometryCompression());
        UserAnnotationEntity saved = userAnnotationRepository.findById(newId).orElseThrow();

        UserAnnotationCommandPayload commandPayload = userAnnotationMapper.mapToCommandPayload(saved);
        CreateUserAnnotationCommand command = new CreateUserAnnotationCommand(commandPayload, userId);
        CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(command, now, now, userId));

        return Optional.of(new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(saved),
            commandV2Entity.getId(), Commands.CREATE_USER_ANNOTATION));
    }

    @Transactional
    public Optional<HttpCommandResponse> updateUserAnnotation(Long id, Long userId, UpdateUserAnnotation payload,
                                                              LocalDateTime now) {
        return userAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(entity -> {
                UserAnnotationCommandPayload before = userAnnotationMapper.mapToCommandPayload(entity);
                String newWkt = payload.wktLocation().orElse(entity.getWktLocation());
                double newCompression = payload.geometryCompression().orElse(entity.getGeometryCompression());
                userAnnotationRepository.updateGeometry(id, newWkt, newCompression, now);
                UserAnnotationEntity updated = userAnnotationRepository.findById(id).orElseThrow();
                UserAnnotationCommandPayload after = userAnnotationMapper.mapToCommandPayload(updated);
                UpdateUserAnnotationCommand command = new UpdateUserAnnotationCommand(id, before, after, userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                return new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(updated),
                    commandV2Entity.getId(), Commands.UPDATE_USER_ANNOTATION);
            });
    }

    @Transactional
    public Optional<HttpCommandResponse> deleteUserAnnotation(Long id, Long userId, LocalDateTime now) {
        return userAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(entity -> {
                UserAnnotationCommandPayload before = userAnnotationMapper.mapToCommandPayload(entity);
                DeleteUserAnnotationCommand command = new DeleteUserAnnotationCommand(id, before, userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                entity.setDeleted(now);
                userAnnotationRepository.save(entity);
                return new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(entity),
                    commandV2Entity.getId(), Commands.DELETE_USER_ANNOTATION);
            });
    }

    public Optional<HttpCommandResponse> undoCreateUserAnnotation(UUID commandId, CreateUserAnnotationCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.after().projectId())) {
            return Optional.empty();
        }
        return softDelete(commandId, cmd.after().id(), Commands.CREATE_USER_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> redoCreateUserAnnotation(UUID commandId, CreateUserAnnotationCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.after().projectId())) {
            return Optional.empty();
        }
        return restore(commandId, cmd.after().id(), Commands.CREATE_USER_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> undoDeleteUserAnnotation(UUID commandId, DeleteUserAnnotationCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.before().projectId())) {
            return Optional.empty();
        }
        return restore(commandId, cmd.before().id(), Commands.DELETE_USER_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> redoDeleteUserAnnotation(UUID commandId, DeleteUserAnnotationCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.before().projectId())) {
            return Optional.empty();
        }
        return softDelete(commandId, cmd.before().id(), Commands.DELETE_USER_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> undoUpdateUserAnnotation(UUID commandId, UpdateUserAnnotationCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.before().projectId())) {
            return Optional.empty();
        }
        return userAnnotationRepository.findById(cmd.before().id()).map(entity -> {
            userAnnotationRepository.updateGeometry(entity.getId(), cmd.before().wktLocation(),
                cmd.before().geometryCompression(), now);
            UserAnnotationEntity updated = userAnnotationRepository.findById(entity.getId()).orElseThrow();
            return new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(updated), commandId,
                Commands.UPDATE_USER_ANNOTATION);
        });
    }

    public Optional<HttpCommandResponse> redoUpdateUserAnnotation(UUID commandId, UpdateUserAnnotationCommand cmd,
                                                                  Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.after().projectId())) {
            return Optional.empty();
        }
        return userAnnotationRepository.findById(cmd.after().id()).map(entity -> {
            userAnnotationRepository.updateGeometry(entity.getId(), cmd.after().wktLocation(),
                cmd.after().geometryCompression(), now);
            UserAnnotationEntity updated = userAnnotationRepository.findById(entity.getId()).orElseThrow();
            return new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(updated), commandId,
                Commands.UPDATE_USER_ANNOTATION);
        });
    }

    private Optional<HttpCommandResponse> softDelete(UUID commandId, long entityId, String command,
                                                     LocalDateTime now) {
        return userAnnotationRepository.findById(entityId).map(entity -> {
            entity.setDeleted(now);
            userAnnotationRepository.save(entity);
            return new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(entity), commandId, command);
        });
    }

    private Optional<HttpCommandResponse> restore(UUID commandId, long entityId, String command,
                                                  LocalDateTime now) {
        return userAnnotationRepository.findById(entityId).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(now);
            userAnnotationRepository.save(entity);
            return new HttpCommandResponse(true, userAnnotationMapper.mapToResponse(entity), commandId, command);
        });
    }
}
