package org.cytomine.repository.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateOntologyCommand;
import be.cytomine.common.repository.model.command.request.CreateStorageCommand;
import be.cytomine.common.repository.model.command.request.CreateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.CreateUploadedFileCommand;
import be.cytomine.common.repository.model.command.request.DeleteOntologyCommand;
import be.cytomine.common.repository.model.command.request.DeleteStorageCommand;
import be.cytomine.common.repository.model.command.request.DeleteTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteUploadedFileCommand;
import be.cytomine.common.repository.model.command.request.UndoCreateCommand;
import be.cytomine.common.repository.model.command.request.UndoDeleteCommand;
import be.cytomine.common.repository.model.command.request.UndoUpdateCommand;
import be.cytomine.common.repository.model.command.request.UpdateOntologyCommand;
import be.cytomine.common.repository.model.command.request.UpdateStorageCommand;
import be.cytomine.common.repository.model.command.request.UpdateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateUploadedFileCommand;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
    private final StorageCommandService storageCommandService;
    private final TagDomainAssociationCommandService tagDomainAssociationCommandService;
    private final TermCommandService termCommandService;
    private final TermRelationCommandService termRelationCommandService;
    private final OntologyCommandService ontologyCommandService;
    private final UploadedFileCommandService uploadedFileCommandService;

    public Optional<HttpCommandResponse> undoCommand(long userId, UUID undoCommand, LocalDateTime now) {
        Set<HttpCommandResponse> subCommands = commandRepository.findByParentCommandId(undoCommand).stream()
            .map(rc -> undoCommand(userId, rc.getId(), now)).flatMap(Optional::stream).collect(Collectors.toSet());

        return commandRepository.findById(undoCommand)
            .flatMap(commandEntity -> switch (commandEntity.getData()) {
                case DeleteTermCommand dtc -> termCommandService.undoDelete(commandEntity.getId(), dtc, userId, now);
                case CreateTermCommand icr -> termCommandService.undoCreate(commandEntity.getId(), icr, userId, now);
                case UpdateTermCommand ucr -> termCommandService.undoUpdate(commandEntity.getId(), ucr, userId, now);
                case DeleteTermRelationCommand deleteTermRelationCommand ->
                    termRelationCommandService.undoDelete(commandEntity.getId(), deleteTermRelationCommand, userId,
                        now);
                case CreateTermRelationCommand ctrc ->
                    termRelationCommandService.undoCreate(commandEntity.getId(), ctrc, userId, now);
                case UpdateTermRelationCommand utrc ->
                    termRelationCommandService.undoUpdate(commandEntity.getId(), utrc, userId, now);
                case CreateOntologyCommand createOntologyCommand ->
                    ontologyCommandService.undoCreate(commandEntity.getId(), createOntologyCommand, userId, now);
                case DeleteOntologyCommand deleteOntologyCommand ->
                    ontologyCommandService.undoDelete(commandEntity.getId(), deleteOntologyCommand, userId, now);
                case UpdateOntologyCommand updateOntologyCommand ->
                    ontologyCommandService.undoUpdate(commandEntity.getId(), updateOntologyCommand, userId, now);
                case CreateStorageCommand csc ->
                    storageCommandService.undoCreate(commandEntity.getId(), csc, userId, now);
                case UpdateStorageCommand usc ->
                    storageCommandService.undoUpdate(commandEntity.getId(), usc, userId, now);
                case DeleteStorageCommand dsc ->
                    storageCommandService.undoDelete(commandEntity.getId(), dsc, userId, now);
                case CreateUploadedFileCommand cufc ->
                    uploadedFileCommandService.undoCreate(commandEntity.getId(), cufc, userId, now);
                case UpdateUploadedFileCommand uufc ->
                    uploadedFileCommandService.undoUpdate(commandEntity.getId(), uufc, userId, now);
                case DeleteUploadedFileCommand dufc ->
                    uploadedFileCommandService.undoDelete(commandEntity.getId(), dufc, userId, now);
                case CreateTagDomainAssociationCommand ctdac ->
                    tagDomainAssociationCommandService.undoCreate(commandEntity.getId(), ctdac, userId, now);
                case UpdateTagDomainAssociationCommand utdac ->
                    tagDomainAssociationCommandService.undoUpdate(commandEntity.getId(), utdac, userId, now);
                case DeleteTagDomainAssociationCommand dtdac ->
                    tagDomainAssociationCommandService.undoDelete(commandEntity.getId(), dtdac, userId, now);

                // Actually we undo an undo target here
                case UndoCreateCommand<?> v -> switch (v.target()) {
                    case CreateTermCommand ctc ->
                        termCommandService.undoDelete(v.commandId(), new DeleteTermCommand(ctc.after(), userId), userId,
                            now);
                    case CreateOntologyCommand coc ->
                        ontologyCommandService.undoDelete(v.commandId(), new DeleteOntologyCommand(coc.after(), userId),
                            userId, now);
                    case CreateStorageCommand csc ->
                        storageCommandService.undoDelete(v.commandId(), new DeleteStorageCommand(csc.after(), userId),
                            userId, now);
                    case CreateTagDomainAssociationCommand ctdac ->
                        tagDomainAssociationCommandService.undoDelete(v.commandId(),
                            new DeleteTagDomainAssociationCommand(ctdac.after(), userId), userId, now);
                    case CreateTermRelationCommand ctrc -> termRelationCommandService.undoDelete(v.commandId(),
                        new DeleteTermRelationCommand(ctrc.after(), userId), userId, now);
                    case CreateUploadedFileCommand cufc -> uploadedFileCommandService.undoDelete(v.commandId(),
                        new DeleteUploadedFileCommand(cufc.after(), userId), userId, now);
                };
                case UndoDeleteCommand<?> v -> switch (v.target()) {
                    case DeleteStorageCommand dsc ->
                        storageCommandService.undoCreate(v.commandId(), new CreateStorageCommand(dsc.before(), userId),
                            userId, now);
                    case DeleteTermCommand dtc ->
                        termCommandService.undoCreate(v.commandId(), new CreateTermCommand(dtc.before(), userId),
                            userId,
                            now);
                    case DeleteOntologyCommand doc -> ontologyCommandService.undoCreate(v.commandId(),
                        new CreateOntologyCommand(doc.before(), userId),
                        userId, now);
                    case DeleteTagDomainAssociationCommand dtdac ->
                        tagDomainAssociationCommandService.undoCreate(v.commandId(),
                            new CreateTagDomainAssociationCommand(dtdac.before(), userId), userId, now);
                    case DeleteTermRelationCommand dtrc -> termRelationCommandService.undoCreate(v.commandId(),
                        new CreateTermRelationCommand(dtrc.before(), userId), userId, now);
                    case DeleteUploadedFileCommand dufc -> uploadedFileCommandService.undoCreate(v.commandId(),
                        new CreateUploadedFileCommand(dufc.before(), userId), userId, now);
                };
                case UndoUpdateCommand<?> v -> switch (v.target()) {
                    case UpdateTermCommand utc -> termCommandService.undoUpdate(v.commandId(),
                        new UpdateTermCommand(utc.after(), utc.before(), userId), userId, now);
                    case UpdateOntologyCommand uoc -> ontologyCommandService.undoUpdate(v.commandId(),
                        new UpdateOntologyCommand(uoc.after(), uoc.before(), userId), userId, now);
                    case UpdateStorageCommand usc -> storageCommandService.undoUpdate(v.commandId(),
                        new UpdateStorageCommand(usc.after(), usc.before(), userId), userId, now);
                    case UpdateTagDomainAssociationCommand utdac ->
                        tagDomainAssociationCommandService.undoUpdate(v.commandId(),
                            new UpdateTagDomainAssociationCommand(utdac.after(), utdac.before(), userId), userId, now);
                    case UpdateTermRelationCommand utrc -> termRelationCommandService.undoUpdate(v.commandId(),
                        new UpdateTermRelationCommand(utrc.after(), utrc.before(), userId), userId, now);
                    case UpdateUploadedFileCommand uufc -> uploadedFileCommandService.undoUpdate(v.commandId(),
                        new UpdateUploadedFileCommand(uufc.after(), uufc.before(), userId), userId, now);
                };
            }).map(command -> new HttpCommandResponse(command.printMessage(), command.data(), command.commandId(),
                command.command(),
                Stream.concat(command.subCommands().stream(), subCommands.stream()).collect(Collectors.toSet())));
    }
}
