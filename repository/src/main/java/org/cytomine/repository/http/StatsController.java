package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.StatsMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.model.stat.payload.StatTerm;

@Component
@RequiredArgsConstructor
public class StatsController implements StatsHttpContract {

    private final TermRepository termRepository;
    private final StatsMapper statsMapper;

    @Override
    @GetMapping("/project/{projectId}")
    public Page<StatTerm> findTermsByProject(long projectId, long userId, Optional<LocalDateTime> startDate,
                                             Optional<LocalDateTime> endDate, Pageable pageable) {
        return termRepository.findAllByProjectForStats(projectId, startDate.orElse(null), endDate.orElse(null),
            pageable).map(statsMapper::map);
    }
}
