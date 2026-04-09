package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.TermCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;


@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class TermController implements TermHttpContract {
    private final OntologyMapper ontologyMapper;
    private final TermRepository termRepository;
    private final TermCommandService termCommandService;
    private final ACLService aclService;

    @Override
    @GetMapping("/{id}")
    public Optional<TermResponse> findTermByID(@PathVariable long id, @RequestParam long userId) {
        return termRepository.findById(id)
            .filter(termEntity -> aclService.canReadOntology(userId, termEntity.getOntologyId()))
            .map(ontologyMapper::map);
    }

    @Override
    @PostMapping
    public Optional<HttpCommandResponse> create(@RequestParam long userId, @RequestBody CreateTerm createTerm) {
        return termCommandService.createTerm(userId, createTerm, LocalDateTime.now());
    }

    @Override
    @PutMapping("/{id}")
    public Optional<HttpCommandResponse> update(@PathVariable long id, @RequestParam long userId,
                                                @RequestBody UpdateTerm updateTerm) {
        return termCommandService.updateTerm(id, userId, updateTerm, LocalDateTime.now());
    }

    @Override
    @DeleteMapping("/{id}")
    @Transactional
    public Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId) {
        return termCommandService.deleteTerm(id, userId, LocalDateTime.now());
    }

    @Override
    @GetMapping("/project/{id}")
    public Page<TermResponse> findTermsByProject(@PathVariable long id, @RequestParam long userId, Pageable pageable) {
        if (!aclService.canReadProject(userId, id)) {
            return Page.empty();
        }
        return termRepository.findAllByProjectId(id,  pageable).map(ontologyMapper::map);
    }

    @Override
    @GetMapping("/ontology/{id}")
    public Page<TermResponse> findTermsByOntology(@PathVariable long id, @RequestParam long userId, Pageable pageable) {
        if (!aclService.canReadOntology(userId, id)) {
            return Page.empty();
        }
        return termRepository.findAllByOntologyIdAndDeletedNull(id, pageable).map(ontologyMapper::map);
    }
}
