package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.CreateTermCommand;
import be.cytomine.common.repository.model.command.DeleteTermCommand;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.UpdateTermCommand;
import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyCommandServiceTest {

    private final long userId = 1L;
    private final long ontologyId = 2L;
    private final TermCommandPayload payload =
        new TermCommandPayload(Optional.empty(), 10L, "name", "#FF0000", "2024-01-01", "2024-01-01", null, ontologyId);
    @Mock
    private CommandV2Repository commandRepository;
    @Mock
    private ACLService aclService;
    @Mock
    private TermCommandService termCommandService;
    @Mock
    private OntologyMapper ontologyMapper;
    @InjectMocks
    private ApplyCommandService applyCommandService;

    @Test
    void undoCommandWithUnknownIdReturnsEmpty() {
        UUID id = UUID.randomUUID();
        when(commandRepository.findById(id)).thenReturn(Optional.empty());

        assertFalse(applyCommandService.undoCommand(userId, id, LocalDateTime.now()).isPresent());
    }

    @Test
    void undoDeleteTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DeleteTermCommand cmd = new DeleteTermCommand(10L, payload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.undoDeleteTerm(id, cmd, userId, now)).thenReturn(Optional.of(mockResponse()));

        applyCommandService.undoCommand(userId, id, now);

        verify(termCommandService).undoDeleteTerm(id, cmd, userId, now);
    }

    @Test
    void undoCreateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CreateTermCommand cmd = new CreateTermCommand(payload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.undoCreateTerm(id, cmd, userId, now)).thenReturn(Optional.of(mockResponse()));

        applyCommandService.undoCommand(userId, id, now);

        verify(termCommandService).undoCreateTerm(id, cmd, userId, now);
    }

    @Test
    void undoUpdateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateTermCommand cmd = new UpdateTermCommand(10L, payload, payload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.undoUpdateTerm(id, cmd, userId)).thenReturn(Optional.of(mockResponse()));

        applyCommandService.undoCommand(userId, id, now);

        verify(termCommandService).undoUpdateTerm(id, cmd, userId);
    }

    @Test
    void redoDeleteTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DeleteTermCommand cmd = new DeleteTermCommand(10L, payload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.redoDeleteTerm(id, cmd, userId, now)).thenReturn(Optional.of(mockResponse()));

        applyCommandService.redoCommand(userId, id, now);

        verify(termCommandService).redoDeleteTerm(id, cmd, userId, now);
    }

    @Test
    void redoCreateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CreateTermCommand cmd = new CreateTermCommand(payload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.redoCreateTerm(id, cmd, userId, now)).thenReturn(Optional.of(mockResponse()));

        applyCommandService.redoCommand(userId, id, now);

        verify(termCommandService).redoCreateTerm(id, cmd, userId, now);
    }

    @Test
    void redoUpdateTermCommandDelegatesToTermCommandService() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateTermCommand cmd = new UpdateTermCommand(10L, payload, payload, userId, ontologyId);
        when(commandRepository.findById(id)).thenReturn(Optional.of(new CommandV2Entity(id, null, null, cmd, 0L)));
        when(termCommandService.redoUpdateTerm(id, cmd, userId, now)).thenReturn(Optional.of(mockResponse()));

        applyCommandService.redoCommand(userId, id, now);

        verify(termCommandService).redoUpdateTerm(id, cmd, userId, now);
    }

    private HttpCommandResponse<TermResponse> mockResponse() {
        return new HttpCommandResponse<>(null, true, null, UUID.randomUUID());
    }
}
