package org.cytomine.repository.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.OntologyRepository;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.UserCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.repository.model.user.payload.UpdateUser;

@RequiredArgsConstructor
@Component
@Getter
public class UserCommandService implements CRUDCommandService<CreateUser, UpdateUser, UserCommandPayload, UserEntity,
    UserResponse> {
    private final OntologyRepository ontologyRepository;
    private final ACLService aclService;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final UserRepository userRepository;
    private final CommandMapper commandMapper;
}
