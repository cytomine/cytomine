package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.service.ApplyCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.CommandHttpContract;
import be.cytomine.common.repository.model.command.payload.response.CommandV2Response;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;

import static be.cytomine.common.repository.http.CommandHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class CommandController implements CommandHttpContract {

    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ApplyCommandService applyCommandService;

    @Override
    public Optional<HttpCommandResponse> undo(UUID commandId, long userId) {
        return applyCommandService.undoCommand(userId, commandId, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<CommandV2Response<?>> get(UUID commandId, long userId) {
        return commandV2Repository.findById(commandId).map(commandMapper::map);
    }

    @Override
    public Page<CommandV2Response<?>> getAllForUser(long userId, Pageable pageable) {
        return commandV2Repository.findAllByUserId(userId, pageable).map(commandMapper::map);
    }
}
