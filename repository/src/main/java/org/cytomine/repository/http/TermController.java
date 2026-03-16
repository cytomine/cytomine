package org.cytomine.repository.http;

import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.Term;
import be.cytomine.common.repository.model.UpdateTerm;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;
import static java.util.stream.Collectors.toSet;


@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class TermController implements TermHttpContract {
    private final OntologyMapper ontologyMapper;
    private final TermRepository termRepository;

    @Override
    @GetMapping("/{id}")
    public Optional<Term> findTermByID(@PathVariable Long id) {
        return termRepository.findById(id).map(ontologyMapper::map);
    }

    @Override
    public Set<Term> findAll() {
        return termRepository.findAll().stream().map(ontologyMapper::map).collect(toSet());
    }

    @Override
    @PostMapping
    public Term update(@RequestBody CreateTerm createTerm) {
        return ontologyMapper.map(termRepository.save(ontologyMapper.map(createTerm)));
    }

    @Override
    @PutMapping("/{id}")
    public Term update(@PathVariable Long id, @RequestBody UpdateTerm updateTerm) {
        return termRepository.findById(id).map(entity -> {
            updateTerm.name().ifPresent(entity::setName);
            updateTerm.color().ifPresent(entity::setColor);
            return ontologyMapper.map(termRepository.save(entity));
        }).orElseThrow(() -> new RuntimeException("Term not found: " + id));
    }

    @Override
    @DeleteMapping("/{id}")
    public Optional<Term> delete(@PathVariable Long id) {
        Optional<Term> term = termRepository.findById(id).map(ontologyMapper::map);
        termRepository.deleteById(id);
        return term;
    }

    @Override
    public Set<Term> findTermsByProject(Long id) {
        return termRepository.findAllByOntologyId(id).stream().map(ontologyMapper::map).collect(toSet());
    }

    @Override
    public Set<Term> findTermsByOntology(Long id) {
        return termRepository.findAllByProjectId(id).stream().map(ontologyMapper::map).collect(toSet());
    }
}
