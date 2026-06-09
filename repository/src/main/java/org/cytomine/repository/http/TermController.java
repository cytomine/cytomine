package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.TermMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.TermCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final TermMapper termMapper;
    private final TermRepository termRepository;
    private final TermCommandService termCommandService;
    private final ACLService aclService;

    @Override
    public Optional<TermResponse> findTermByID(@PathVariable long id, @RequestParam long userId) {
        return termRepository.findByIdAndDeletedNull(id)
            .filter(termEntity -> aclService.canReadOntology(userId, termEntity.getOntologyId()))
            .map(termMapper::mapToTermResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(@RequestParam long userId, @RequestBody CreateTerm createTerm) {
        return termCommandService.create(userId, createTerm, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(@PathVariable long id, @RequestParam long userId,
        @RequestBody UpdateTerm updateTerm) {
        return termCommandService.update(userId, id, updateTerm, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    @Transactional
    public Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId) {
        return termCommandService.delete(userId, id, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    @Transactional
    public Set<HttpCommandResponse> deleteAll(Set<Long> ids, long userId) {
        // Later we may implement it in OntologyHttpContract
        return ids.stream()
            .map(id -> termCommandService.delete(userId, id, LocalDateTime.now()))
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Page<TermResponse> findTermsByProject(@PathVariable long id, @RequestParam long userId, Pageable pageable) {
        if (!aclService.canReadProject(userId, id)) {
            return Page.empty();
        }
        return termRepository.findAllByProjectId(id, pageable).map(termMapper::mapToTermResponse);
    }

    @Override
    public Page<TermResponse> findTermsByOntology(@PathVariable long id, @RequestParam long userId, Pageable pageable) {
        if (!aclService.canReadOntology(userId, id)) {
            return Page.empty();
        }
        return termRepository.findAllByOntologyIdAndDeletedNull(id, pageable).map(termMapper::mapToTermResponse);
    }

    @Override
    public Set<Long> findAllTermIdsByOntology(@PathVariable long id, @RequestParam long userId) {
        if (!aclService.canReadOntology(userId, id)) {
            return Set.of();
        }
        return termRepository.findAllIdsByOntologyId(id);
    }

    @Override
    public Set<Long> findAllTermIdsByProject(@PathVariable long id, @RequestParam long userId) {
        if (!aclService.canReadProject(userId, id)) {
            return Set.of();
        }
        return termRepository.findAllIdsByProjectId(id);
    }
}
