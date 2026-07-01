package be.cytomine.common.repository.model.command.payload.response;

import java.time.Instant;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record UndoCommandResponse(ApplyCommandResponse subCommand) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.UNDO_COMMAND;
    }

    @Override
    public Optional<Instant> updated() {
        return subCommand.updated();
    }

    @Override
    public Optional<Instant> deleted() {
        return subCommand.deleted();
    }

    @Override
    public Instant created() {
        return subCommand.created();
    }

    @Override
    public long id() {
        return subCommand.id();
    }
}
