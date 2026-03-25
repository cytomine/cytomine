package org.cytomine.repository.service;

import java.util.Date;

import be.cytomine.common.repository.model.command.delete.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.insert.InsertCommandRequest;
import be.cytomine.common.repository.model.command.update.UpdateCommandRequest;
import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@AllArgsConstructor
@Component
public class CommandService {

    private static final String DELETE_COMMAND = "be.cytomine.domain.command.DeleteCommand";
    private static final String ADD_COMMAND = "be.cytomine.domain.command.AddCommand";
    private static final String EDIT_COMMAND = "be.cytomine.domain.command.EditCommand";
    private final ObjectMapper objectMapper;

    private final CommandRepository commandRepository;

    public CommandEntity delete(DeleteCommandRequest request) {
        Date now = new Date();
        return commandRepository.save(
            new CommandEntity(null, null, now, now, DELETE_COMMAND,
                objectMapper.writeValueAsString(request.data()),
                request.userId(), null, request.projectId(), true, null, true,
                request.serviceName(), false));
    }

    public CommandEntity update(UpdateCommandRequest request) {
        Date now = new Date();
        return commandRepository.save(
            new CommandEntity(null, null, now, now, EDIT_COMMAND,
                objectMapper.writeValueAsString(request.data()), request.userId(), null,
                request.projectId(), true, null, true, request.serviceName(), false));
    }

    public CommandEntity insert(InsertCommandRequest request) {
        Date now = new Date();
        return commandRepository.save(
            new CommandEntity(null, null, now, now, ADD_COMMAND,
                objectMapper.writeValueAsString(request.data()), request.userId(), null,
                request.projectId(), true, null, true, request.serviceName(), false));
    }
}
