package org.cytomine.repository.service;

import java.util.Date;

import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.repository.model.command.delete.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.insert.InsertCommandRequest;
import be.cytomine.common.repository.model.command.update.UpdateCommandRequest;

@AllArgsConstructor
@Component
public class CommandService {

    private static final String DELETE_COMMAND = "be.cytomine.domain.command.DeleteCommand";
    private static final String ADD_COMMAND = "be.cytomine.domain.command.AddCommand";
    private static final String EDIT_COMMAND = "be.cytomine.domain.command.EditCommand";
    private final ObjectMapper objectMapper;

    private final CommandRepository commandRepository;

    public CommandEntity delete(DeleteCommandRequest<?> request) {
        return createCommandEntity(
            DELETE_COMMAND, request.data(), request.userId(),
            request.projectId().orElse(null), request.getActionMessage(), request.serviceName());
    }

    public CommandEntity update(UpdateCommandRequest<?> request) {
        return createCommandEntity(
            EDIT_COMMAND, request.data(), request.userId(),
            request.projectId().orElse(null), request.getActionMessage(), request.serviceName());
    }

    public CommandEntity insert(InsertCommandRequest<?> request) {
        return createCommandEntity(
            ADD_COMMAND, request.data(), request.userId(),
            request.projectId().orElse(null), request.getActionMessage(), request.serviceName());
    }

    private CommandEntity createCommandEntity(String commandType, Object data, Long userId,
                                              Long projectId, String actionMessage, String serviceName) {
        Date now = new Date();
        return commandRepository.save(new CommandEntity(null, null, now, now, commandType,
            objectMapper.writeValueAsString(data), userId, null, projectId,
            true, actionMessage, true, serviceName, false));
    }
}
