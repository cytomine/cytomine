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
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.TermCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final CurrentUserService currentUserService;

    @Override
    public Optional<TermResponse> findTermByID(@PathVariable long id) {
        return termRepository.findByIdAndDeletedNull(id)
            .filter(termEntity ->
                aclService.canReadOntology(currentUserService.getCurrentUserId(), termEntity.getOntologyId()))
            .map(termMapper::mapToTermResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(@RequestBody CreateTerm createTerm) {
        return termCommandService.create(currentUserService.getCurrentUserId(), createTerm,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(@PathVariable long id,
        @RequestBody UpdateTerm updateTerm) {
        return termCommandService.update(currentUserService.getCurrentUserId(), id, updateTerm,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    @Transactional
    public Optional<HttpCommandResponse> delete(@PathVariable long id) {
        return termCommandService.delete(currentUserService.getCurrentUserId(), id,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    @Transactional
    public Set<HttpCommandResponse> deleteAll(Set<Long> ids) {
        // Later we may implement it in OntologyHttpContract
        return ids.stream()
            .map(id -> termCommandService.delete(currentUserService.getCurrentUserId(), id, LocalDateTime.now()))
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Page<TermResponse> findTermsByProject(@PathVariable long id, Pageable pageable) {
        if (!aclService.canReadProject(currentUserService.getCurrentUserId(), id)) {
            return Page.empty();
        }
        return termRepository.findAllByProjectId(id, pageable).map(termMapper::mapToTermResponse);
    }

    @Override
    public Page<TermResponse> findTermsByOntology(@PathVariable long id, Pageable pageable) {
        if (!aclService.canReadOntology(currentUserService.getCurrentUserId(), id)) {
            return Page.empty();
        }
        return termRepository.findAllByOntologyIdAndDeletedNull(id, pageable).map(termMapper::mapToTermResponse);
    }

    @Override
    public Set<Long> findAllTermIdsByOntology(@PathVariable long id) {
        if (!aclService.canReadOntology(currentUserService.getCurrentUserId(), id)) {
            return Set.of();
        }
        return termRepository.findAllIdsByOntologyId(id);
    }

    @Override
    public Set<Long> findAllTermIdsByProject(@PathVariable long id) {
        if (!aclService.canReadProject(currentUserService.getCurrentUserId(), id)) {
            return Set.of();
        }
        return termRepository.findAllIdsByProjectId(id);
    }
}
