package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.model.stat.payload.StatTerm;

@Component
@RequiredArgsConstructor
public class StatsController implements StatsHttpContract {

    private final TermRepository termRepository;

    @Override
    public Page<StatTerm> findTermsByProject(long projectId, long userId, Optional<LocalDateTime> startDate,
                                             Optional<LocalDateTime> endDate, Pageable pageable) {
        return termRepository.findAllByProjectForStats(projectId, startDate.orElse(null), endDate.orElse(null),
            pageable).map(p -> new StatTerm(p.getId(), p.getKey(), p.getValue(), p.getColor()));
    }
}
