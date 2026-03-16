package org.cytomine.repository.http;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.Term;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;


@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class TermController implements TermHttpContract {
    private final OntologyMapper ontologyMapper;
    private final TermRepository termRepository;

    @Override
    public Optional<Term> findTermByID(Long id) {
        return termRepository.findById(id).map(ontologyMapper::map);
    }
}
