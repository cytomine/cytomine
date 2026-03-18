package org.cytomine.repository.http;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.OntologyRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.OntologyResponse;

import static be.cytomine.common.repository.http.OntologyHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class OntologyController implements OntologyHttpContract {

    private final OntologyRepository ontologyRepository;
    private final OntologyMapper ontologyMapper;

    @Override
    public Optional<OntologyResponse> findOntologyByName(String name) {
        return ontologyRepository.findByName(name).map(ontologyMapper::map);
    }

    @Override
    public Optional<OntologyResponse> findOntologyById(long id) {
        return ontologyRepository.findById(id).map(ontologyMapper::map);
    }
}
