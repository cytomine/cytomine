package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.ReviewedAnnotationMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.ReviewedAnnotationRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.ReviewedAnnotationEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.ReviewedAnnotationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.DeleteReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.UpdateReviewedAnnotationCommand;
import be.cytomine.common.repository.model.reviewedannotation.payload.CreateReviewedAnnotation;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;

@Component
@AllArgsConstructor
public class ReviewedAnnotationCommandService {
    private final ReviewedAnnotationRepository reviewedAnnotationRepository;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ReviewedAnnotationMapper reviewedAnnotationMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse> createReviewedAnnotation(Long userId, CreateReviewedAnnotation payload,
                                                                  LocalDateTime now) {
        if (!aclService.canReadProject(userId, payload.projectId())) {
            return Optional.empty();
        }
        long newId = reviewedAnnotationRepository.insertWithGeometry(now, now, payload.userId(),
            payload.reviewUserId(), payload.imageId(), payload.sliceId(), payload.projectId(),
            payload.parentIdent(), payload.parentClassName(), payload.status(), payload.wktLocation(),
            payload.geometryCompression());
        ReviewedAnnotationEntity saved = reviewedAnnotationRepository.findById(newId).orElseThrow();

        if (payload.termIds() != null) {
            payload.termIds().forEach(termId ->
                reviewedAnnotationRepository.insertTermLink(newId, termId));
        }

        List<Long> termIds = reviewedAnnotationRepository.findTermIds(newId);
        ReviewedAnnotationCommandPayload commandPayload = reviewedAnnotationMapper.mapToCommandPayload(saved,
            termIds);
        CreateReviewedAnnotationCommand command = new CreateReviewedAnnotationCommand(commandPayload, userId);
        CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(command, now, now, userId));

        return Optional.of(new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(saved, termIds),
            commandV2Entity.getId(), Commands.CREATE_REVIEWED_ANNOTATION));
    }

    @Transactional
    public Optional<HttpCommandResponse> updateReviewedAnnotation(Long id, Long userId,
                                                                  UpdateReviewedAnnotation payload,
                                                                  LocalDateTime now) {
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .filter(e -> e.getReviewUserId() == userId || aclService.isAdmin(userId))
            .map(entity -> {
                List<Long> termIds = reviewedAnnotationRepository.findTermIds(id);
                ReviewedAnnotationCommandPayload before = reviewedAnnotationMapper.mapToCommandPayload(entity,
                    termIds);
                String newWkt = payload.wktLocation().orElse(entity.getWktLocation());
                double newCompression = payload.geometryCompression().orElse(entity.getGeometryCompression());
                reviewedAnnotationRepository.updateGeometry(id, newWkt, newCompression, now);
                ReviewedAnnotationEntity updated = reviewedAnnotationRepository.findById(id).orElseThrow();
                ReviewedAnnotationCommandPayload after = reviewedAnnotationMapper.mapToCommandPayload(updated,
                    termIds);
                UpdateReviewedAnnotationCommand command = new UpdateReviewedAnnotationCommand(id, before, after,
                    userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(updated, termIds),
                    commandV2Entity.getId(), Commands.UPDATE_REVIEWED_ANNOTATION);
            });
    }

    @Transactional
    public Optional<HttpCommandResponse> deleteReviewedAnnotation(Long id, Long userId, LocalDateTime now) {
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .filter(e -> e.getReviewUserId() == userId || aclService.isAdmin(userId))
            .map(entity -> {
                List<Long> termIds = reviewedAnnotationRepository.findTermIds(id);
                ReviewedAnnotationCommandPayload before = reviewedAnnotationMapper.mapToCommandPayload(entity,
                    termIds);
                DeleteReviewedAnnotationCommand command = new DeleteReviewedAnnotationCommand(id, before, userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                entity.setDeleted(now);
                reviewedAnnotationRepository.save(entity);
                return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(entity, termIds),
                    commandV2Entity.getId(), Commands.DELETE_REVIEWED_ANNOTATION);
            });
    }

    public Optional<HttpCommandResponse> undoCreateReviewedAnnotation(UUID commandId,
                                                                      CreateReviewedAnnotationCommand cmd,
                                                                      Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.after().projectId())) {
            return Optional.empty();
        }
        if (cmd.after().reviewUserId() != userId && !aclService.isAdmin(userId)) {
            return Optional.empty();
        }
        return softDelete(commandId, cmd.after().id(), Commands.CREATE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> redoCreateReviewedAnnotation(UUID commandId,
                                                                      CreateReviewedAnnotationCommand cmd,
                                                                      Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.after().projectId())) {
            return Optional.empty();
        }
        if (cmd.after().reviewUserId() != userId && !aclService.isAdmin(userId)) {
            return Optional.empty();
        }
        return restore(commandId, cmd.after().id(), Commands.CREATE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> undoDeleteReviewedAnnotation(UUID commandId,
                                                                      DeleteReviewedAnnotationCommand cmd,
                                                                      Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.before().projectId())) {
            return Optional.empty();
        }
        if (cmd.before().reviewUserId() != userId && !aclService.isAdmin(userId)) {
            return Optional.empty();
        }
        return restore(commandId, cmd.before().id(), Commands.DELETE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> redoDeleteReviewedAnnotation(UUID commandId,
                                                                      DeleteReviewedAnnotationCommand cmd,
                                                                      Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.before().projectId())) {
            return Optional.empty();
        }
        if (cmd.before().reviewUserId() != userId && !aclService.isAdmin(userId)) {
            return Optional.empty();
        }
        return softDelete(commandId, cmd.before().id(), Commands.DELETE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> undoUpdateReviewedAnnotation(UUID commandId,
                                                                      UpdateReviewedAnnotationCommand cmd,
                                                                      Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.before().projectId())) {
            return Optional.empty();
        }
        if (cmd.before().reviewUserId() != userId && !aclService.isAdmin(userId)) {
            return Optional.empty();
        }
        return reviewedAnnotationRepository.findById(cmd.before().id()).map(entity -> {
            reviewedAnnotationRepository.updateGeometry(entity.getId(), cmd.before().wktLocation(),
                cmd.before().geometryCompression(), now);
            ReviewedAnnotationEntity updated = reviewedAnnotationRepository.findById(entity.getId()).orElseThrow();
            List<Long> termIds = reviewedAnnotationRepository.findTermIds(entity.getId());
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(updated, termIds),
                commandId, Commands.UPDATE_REVIEWED_ANNOTATION);
        });
    }

    public Optional<HttpCommandResponse> redoUpdateReviewedAnnotation(UUID commandId,
                                                                      UpdateReviewedAnnotationCommand cmd,
                                                                      Long userId, LocalDateTime now) {
        if (!aclService.canReadProject(userId, cmd.after().projectId())) {
            return Optional.empty();
        }
        if (cmd.after().reviewUserId() != userId && !aclService.isAdmin(userId)) {
            return Optional.empty();
        }
        return reviewedAnnotationRepository.findById(cmd.after().id()).map(entity -> {
            reviewedAnnotationRepository.updateGeometry(entity.getId(), cmd.after().wktLocation(),
                cmd.after().geometryCompression(), now);
            ReviewedAnnotationEntity updated = reviewedAnnotationRepository.findById(entity.getId()).orElseThrow();
            List<Long> termIds = reviewedAnnotationRepository.findTermIds(entity.getId());
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(updated, termIds),
                commandId, Commands.UPDATE_REVIEWED_ANNOTATION);
        });
    }

    private Optional<HttpCommandResponse> softDelete(UUID commandId, long entityId, String command,
                                                     LocalDateTime now) {
        return reviewedAnnotationRepository.findById(entityId).map(entity -> {
            entity.setDeleted(now);
            reviewedAnnotationRepository.save(entity);
            List<Long> termIds = reviewedAnnotationRepository.findTermIds(entityId);
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(entity, termIds),
                commandId, command);
        });
    }

    private Optional<HttpCommandResponse> restore(UUID commandId, long entityId, String command,
                                                  LocalDateTime now) {
        return reviewedAnnotationRepository.findById(entityId).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(now);
            reviewedAnnotationRepository.save(entity);
            List<Long> termIds = reviewedAnnotationRepository.findTermIds(entityId);
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(entity, termIds),
                commandId, command);
        });
    }
}
