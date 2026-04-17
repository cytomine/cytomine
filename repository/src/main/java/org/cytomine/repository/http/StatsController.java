package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.StatsMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.model.stat.payload.FlatStatUserTerm;
import be.cytomine.common.repository.model.stat.payload.StatTerm;

@Component
@RequiredArgsConstructor
public class StatsController implements StatsHttpContract {

    private final TermRepository termRepository;
    private final StatsMapper statsMapper;


    @Override
    @GetMapping("/project/{projectId}")
    public Page<StatTerm> findTermsByProject(@PathVariable long projectId, @RequestParam long userId,
                                             @RequestParam(required = false) Optional<LocalDateTime> startDate,
                                             @RequestParam(required = false) Optional<LocalDateTime> endDate,
                                             @RequestParam int page, @RequestParam int size) {
        return termRepository.findAllByProjectForStats(projectId, startDate.orElse(null), endDate.orElse(null),
            PageRequest.of(page, size)).map(statsMapper::map);
    }

    @Override
    @GetMapping("/per-user/project/{projectId}")
    public Page<FlatStatUserTerm> findUserTermsByProject(long projectId, long userId, int page, int size) {
        return termRepository.findAllByUsersByProjectForStats(projectId, PageRequest.of(page, size))
            .map(statsMapper::map);
    }
}
