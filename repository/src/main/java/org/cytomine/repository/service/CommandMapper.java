package org.cytomine.repository.service;

import org.cytomine.repository.persistence.entity.CommandEntity;

import be.cytomine.common.repository.model.command.HttpCommandResponse;

public interface CommandMapper {


    HttpCommandResponse<?> map(CommandEntity commandEntity);

}
