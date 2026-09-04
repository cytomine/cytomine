package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record UndoCommandResponse(ApplyCommandResponse subCommand) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.UNDO_COMMAND;
    }

    @Override
    public Optional<LocalDateTime> updated() {
        return subCommand.updated();
    }

    @Override
    public Optional<LocalDateTime> deleted() {
        return subCommand.deleted();
    }

    @Override
    public LocalDateTime created() {
        return subCommand.created();
    }

    @Override
    public long id() {
        return subCommand.id();
    }
}
