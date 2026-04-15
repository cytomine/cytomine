package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
    private final TermCommandService termCommandService;
    private final TermRelationCommandService termRelationCommandService;
    private final ReviewedAnnotationCommandService reviewedAnnotationCommandService;


    @Transactional
    public Optional<HttpCommandResponse> undoCommand(long userId, UUID undoCommand, LocalDateTime now) {
        return commandRepository.findById(undoCommand).flatMap(commandEntity -> switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> termCommandService.undoDeleteTerm(commandEntity.getId(), dtc, userId, now);
            case CreateTermCommand icr -> termCommandService.undoCreateTerm(commandEntity.getId(), icr, userId, now);
            case UpdateTermCommand ucr -> termCommandService.undoUpdateTerm(commandEntity.getId(), ucr, userId);
            case DeleteTermRelationCommand deleteTermRelationCommand ->
                termRelationCommandService.undoDeleteTermRelation(commandEntity.getId(), deleteTermRelationCommand,
                    userId, now);
            case CreateTermRelationCommand ctrc ->
                termRelationCommandService.undoCreateTermRelation(commandEntity.getId(), ctrc, userId, now);
            case UpdateTermRelationCommand utrc ->
                termRelationCommandService.undoUpdateTermRelation(commandEntity.getId(), utrc, userId);
            case CreateReviewedAnnotationCommand crac ->
                reviewedAnnotationCommandService.undoCreateReviewedAnnotation(commandEntity.getId(), crac, userId,
                    now);
            case UpdateReviewedAnnotationCommand urac ->
                reviewedAnnotationCommandService.undoUpdateReviewedAnnotation(commandEntity.getId(), urac, userId,
                    now);
            case DeleteReviewedAnnotationCommand drac ->
                reviewedAnnotationCommandService.undoDeleteReviewedAnnotation(commandEntity.getId(), drac, userId,
                    now);
        });
    }

    public Optional<HttpCommandResponse> redoCommand(long userId, UUID redoCommand, LocalDateTime now) {
        return commandRepository.findById(redoCommand).flatMap(commandEntity -> switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> termCommandService.redoDeleteTerm(commandEntity.getId(), dtc, userId, now);
            case CreateTermCommand icr -> termCommandService.redoCreateTerm(commandEntity.getId(), icr, userId, now);
            case UpdateTermCommand ucr -> termCommandService.redoUpdateTerm(commandEntity.getId(), ucr, userId, now);
            case DeleteTermRelationCommand ucr ->
                termRelationCommandService.redoDeleteTermRelation(commandEntity.getId(), ucr, userId, now);
            case CreateTermRelationCommand ctrc ->
                termRelationCommandService.redoCreateTermRelation(commandEntity.getId(), ctrc, userId, now);
            case UpdateTermRelationCommand utrc ->
                termRelationCommandService.redoUpdateTermRelation(commandEntity.getId(), utrc, userId, now);
            case CreateReviewedAnnotationCommand crac ->
                reviewedAnnotationCommandService.redoCreateReviewedAnnotation(commandEntity.getId(), crac, userId,
                    now);
            case UpdateReviewedAnnotationCommand urac ->
                reviewedAnnotationCommandService.redoUpdateReviewedAnnotation(commandEntity.getId(), urac, userId,
                    now);
            case DeleteReviewedAnnotationCommand drac ->
                reviewedAnnotationCommandService.redoDeleteReviewedAnnotation(commandEntity.getId(), drac, userId,
                    now);
        });
    }


}
