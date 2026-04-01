package org.cytomine.repository.http;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.service.ApplyCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.CommandHttpContract;
import be.cytomine.common.repository.model.CommandResponse;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static be.cytomine.common.repository.http.CommandHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class CommandController implements CommandHttpContract {

    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ApplyCommandService applyCommandService;

    @Override
    @PostMapping("/undo/{commandId}")
    public Optional<HttpCommandResponse<TermResponse>> undo(UUID commandId, long userId) {
        return applyCommandService.undoCommand(userId, commandId);
    }

    @Override
    public Optional<HttpCommandResponse<?>> redo(UUID commandId, long userId) {
        return Optional.empty();
    }

    @Override
    @GetMapping("/{commandId}")
    public Optional<CommandResponse<?>> get(@PathVariable UUID commandId, @RequestParam long userId) {
        return commandV2Repository.findById(commandId).map(commandMapper::map);
    }
}
