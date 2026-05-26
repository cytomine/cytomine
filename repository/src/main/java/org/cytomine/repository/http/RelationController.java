package org.cytomine.repository.http;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.RelationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.RelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.RelationResponse;

import static be.cytomine.common.repository.http.RelationHttpContract.ROOT_PATH;


@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class RelationController implements RelationHttpContract {
    private final RelationRepository relationRepository;
    private final OntologyMapper ontologyMapper;

    @Override
    @GetMapping("/parent")
    public RelationResponse findParentRelation() {
        return ontologyMapper.mapRelationResponse(relationRepository.findParent());
    }
}
