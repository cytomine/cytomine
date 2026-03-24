package org.cytomine.repository.service;

import be.cytomine.common.repository.model.command.delete.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.insert.InsertCommandRequest;
import be.cytomine.common.repository.model.command.update.UpdateCommandRequest;
import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CommandService {
    private final CommandRepository commandRepository;

    public CommandEntity delete(DeleteCommandRequest deleteCommandRequest) {
        return null;
    }

    public CommandEntity update(UpdateCommandRequest updateCommandRequest) {
        return null;
    }

    public CommandEntity insert(InsertCommandRequest insertCommandRequest) {
        return null;
    }
}
