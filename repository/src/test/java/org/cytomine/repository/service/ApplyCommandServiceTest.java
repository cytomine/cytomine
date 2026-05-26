package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyCommandServiceTest {

    private final long userId = 1L;
    private final long ontologyId = 2L;
    private TermCommandPayload termPayload;
    private TermRelationCommandPayload relationPayload;
    @Mock
    private CommandV2Repository commandRepository;
    @Mock
    private ACLService aclService;
    @Mock
    private TermCommandService termCommandService;
    @Mock
    private TermRelationCommandService termRelationCommandService;
    @Mock
    private OntologyMapper ontologyMapper;
    @InjectMocks
    private ApplyCommandService applyCommandService;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        termPayload =
            new TermCommandPayload(Optional.empty(), 10L, "name", "#FF0000", now, now,
                Optional.empty(), null, ontologyId);
        relationPayload =
            new TermRelationCommandPayload(20L, 1L, 2L, ontologyId, 3L, null, Optional.empty(), null, "parent");

    }

    @Test
    void undoCommandWithUnknownIdReturnsEmpty() {
        UUID id = UUID.randomUUID();
        when(commandRepository.findById(id)).thenReturn(Optional.empty());

        assertFalse(applyCommandService.undoCommand(userId, id, LocalDateTime.now()).isPresent());
    }

    @Test
    void redoCommandWithUnknownIdReturnsEmpty() {
        UUID id = UUID.randomUUID();
        when(commandRepository.findById(id)).thenReturn(Optional.empty());

        assertFalse(applyCommandService.redoCommand(userId, id, LocalDateTime.now()).isPresent());
    }

    @Test
    void undoDeleteTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DeleteTermCommand cmd = new DeleteTermCommand(10L, termPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.undoDeleteTerm(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.DELETE_TERM)));

        applyCommandService.undoCommand(userId, id, now);

        verify(termCommandService).undoDeleteTerm(id, cmd, userId, now);
    }

    @Test
    void undoCreateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CreateTermCommand cmd = new CreateTermCommand(termPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.undoCreateTerm(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.CREATE_TERM)));

        applyCommandService.undoCommand(userId, id, now);

        verify(termCommandService).undoCreateTerm(id, cmd, userId, now);
    }

    @Test
    void undoUpdateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateTermCommand cmd = new UpdateTermCommand(10L, termPayload, termPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.undoUpdateTerm(id, cmd, userId)).thenReturn(
            Optional.of(mockResponse(Commands.UPDATE_TERM)));

        applyCommandService.undoCommand(userId, id, now);

        verify(termCommandService).undoUpdateTerm(id, cmd, userId);
    }

    @Test
    void redoDeleteTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DeleteTermCommand cmd = new DeleteTermCommand(10L, termPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.redoDeleteTerm(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.DELETE_TERM)));

        applyCommandService.redoCommand(userId, id, now);

        verify(termCommandService).redoDeleteTerm(id, cmd, userId, now);
    }

    @Test
    void redoCreateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CreateTermCommand cmd = new CreateTermCommand(termPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.redoCreateTerm(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.CREATE_TERM)));

        applyCommandService.redoCommand(userId, id, now);

        verify(termCommandService).redoCreateTerm(id, cmd, userId, now);
    }

    @Test
    void redoUpdateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateTermCommand cmd = new UpdateTermCommand(10L, termPayload, termPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.redoUpdateTerm(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.UPDATE_TERM)));

        applyCommandService.redoCommand(userId, id, now);

        verify(termCommandService).redoUpdateTerm(id, cmd, userId, now);
    }

    @Test
    void undoDeleteTermRelationCommandDelegatesToTermRelationCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DeleteTermRelationCommand cmd = new DeleteTermRelationCommand(20L, relationPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termRelationCommandService.undoDeleteTermRelation(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.DELETE_TERM_RELATION)));

        applyCommandService.undoCommand(userId, id, now);

        verify(termRelationCommandService).undoDeleteTermRelation(id, cmd, userId, now);
    }

    @Test
    void undoCreateTermRelationCommandDelegatesToTermRelationCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CreateTermRelationCommand cmd = new CreateTermRelationCommand(relationPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termRelationCommandService.undoCreateTermRelation(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.CREATE_TERM_RELATION)));

        applyCommandService.undoCommand(userId, id, now);

        verify(termRelationCommandService).undoCreateTermRelation(id, cmd, userId, now);
    }

    @Test
    void undoUpdateTermRelationCommandDelegatesToTermRelationCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateTermRelationCommand cmd =
            new UpdateTermRelationCommand(20L, relationPayload, relationPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termRelationCommandService.undoUpdateTermRelation(id, cmd, userId)).thenReturn(
            Optional.of(mockResponse(Commands.UPDATE_TERM_RELATION)));

        applyCommandService.undoCommand(userId, id, now);

        verify(termRelationCommandService).undoUpdateTermRelation(id, cmd, userId);
    }

    @Test
    void redoDeleteTermRelationCommandDelegatesToTermRelationCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DeleteTermRelationCommand cmd = new DeleteTermRelationCommand(20L, relationPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termRelationCommandService.redoDeleteTermRelation(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.DELETE_TERM_RELATION)));

        applyCommandService.redoCommand(userId, id, now);

        verify(termRelationCommandService).redoDeleteTermRelation(id, cmd, userId, now);
    }

    @Test
    void redoCreateTermRelationCommandDelegatesToTermRelationCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CreateTermRelationCommand cmd = new CreateTermRelationCommand(relationPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termRelationCommandService.redoCreateTermRelation(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.CREATE_TERM_RELATION)));

        applyCommandService.redoCommand(userId, id, now);

        verify(termRelationCommandService).redoCreateTermRelation(id, cmd, userId, now);
    }

    @Test
    void redoUpdateTermRelationCommandDelegatesToTermRelationCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateTermRelationCommand cmd =
            new UpdateTermRelationCommand(20L, relationPayload, relationPayload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termRelationCommandService.redoUpdateTermRelation(id, cmd, userId, now)).thenReturn(
            Optional.of(mockResponse(Commands.UPDATE_TERM_RELATION)));

        applyCommandService.redoCommand(userId, id, now);

        verify(termRelationCommandService).redoUpdateTermRelation(id, cmd, userId, now);
    }

    private HttpCommandResponse mockResponse(String command) {
        return new HttpCommandResponse(true, null, UUID.randomUUID(), command);
    }
}
