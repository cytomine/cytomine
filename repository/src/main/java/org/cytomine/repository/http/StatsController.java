package org.cytomine.repository.http;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.model.stat.payload.StatTerm;

public class StatsController implements StatsHttpContract {
    @Override
    public Page<StatTerm> findTermsByOntology(long id, long userId, Pageable pageable) {
        return null;
    }
}
