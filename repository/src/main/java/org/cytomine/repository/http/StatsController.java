package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.StatsMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.model.stat.payload.FlatStatUserTerm;
import be.cytomine.common.repository.model.stat.payload.StatPerTermAndImage;
import be.cytomine.common.repository.model.stat.payload.StatTerm;

import static be.cytomine.common.repository.http.StatsHttpContract.ROOT_PATH;

@RestController
@RequestMapping(ROOT_PATH)
@RequiredArgsConstructor
public class StatsController implements StatsHttpContract {

    private final StatsMapper statsMapper;

    private final TermRepository termRepository;

    @Override
    public Page<StatTerm> findTermsByProject(
        long projectId,
        long userId,
        Optional<LocalDateTime> startDate,
        Optional<LocalDateTime> endDate,
        Pageable pageable
    ) {
        return termRepository.findAllByProjectForStats(
            projectId,
            startDate.orElse(null),
            endDate.orElse(null),
            pageable
        ).map(statsMapper::map);
    }

    @Override
    public Page<FlatStatUserTerm> findUserTermsByProject(long projectId, long userId, Pageable pageable) {
        return termRepository.findAllByUsersByProjectForStats(projectId, pageable).map(statsMapper::map);
    }

    @Override
    public Page<StatPerTermAndImage> findPerTermAndImageByProject(
        long projectId,
        Optional<LocalDateTime> startDate,
        Optional<LocalDateTime> endDate,
        Pageable pageable
    ) {
        return termRepository.findAllPerTermAndImageByProjectForStats(
            projectId,
            startDate.orElse(null),
            endDate.orElse(null),
            pageable
        ).map(statsMapper::map);
    }
}
