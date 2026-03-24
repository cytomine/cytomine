package org.cytomine.repository.service;

import javax.smartcardio.CommandAPDU;
import java.util.Optional;

import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final CommandRepository commandRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandService commandService;

    public Optional<HttpCommandResponse<TermResponse>> deleteTerm(Long id) {

        CommandEntity commandEntity = commandService.delete(null);
        Optional<TermResponse> maybeTermResponse = termRepository.deleteTermById(id).map(ontologyMapper::map);
        return maybeTermResponse.map(
            termResponse -> new HttpCommandResponse<>(null, null, false, termResponse, commandEntity.getId()));
    }


}
