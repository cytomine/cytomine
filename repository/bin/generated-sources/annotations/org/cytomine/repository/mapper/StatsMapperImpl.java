package org.cytomine.repository.mapper;

import be.cytomine.common.repository.model.stat.payload.FlatStatUserTerm;
import be.cytomine.common.repository.model.stat.payload.StatPerTermAndImage;
import be.cytomine.common.repository.model.stat.payload.StatTerm;
import javax.annotation.processing.Generated;
import org.cytomine.repository.persistence.projection.StatPerTermAndImageProjection;
import org.cytomine.repository.persistence.projection.StatTermProjection;
import org.cytomine.repository.persistence.projection.StatUserTermProjection;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-19T13:47:33+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.12 (N/A)"
)
@Component
public class StatsMapperImpl implements StatsMapper {

    @Override
    public StatTerm map(StatTermProjection statTermProjection) {
        if ( statTermProjection == null ) {
            return null;
        }

        long id = 0L;
        String name = null;
        String color = null;
        long count = 0L;

        id = statTermProjection.getId();
        name = statTermProjection.getName();
        color = statTermProjection.getColor();
        count = statTermProjection.getCount();

        StatTerm statTerm = new StatTerm( id, name, color, count );

        return statTerm;
    }

    @Override
    public FlatStatUserTerm map(StatUserTermProjection statUserTermProjection) {
        if ( statUserTermProjection == null ) {
            return null;
        }

        StatTerm term = null;
        long userId = 0L;
        String username = null;

        term = statUserTermProjectionToStatTerm( statUserTermProjection );
        userId = statUserTermProjection.getUserId();
        username = statUserTermProjection.getUsername();

        FlatStatUserTerm flatStatUserTerm = new FlatStatUserTerm( userId, username, term );

        return flatStatUserTerm;
    }

    @Override
    public StatPerTermAndImage map(StatPerTermAndImageProjection statPerTermAndImageProjection) {
        if ( statPerTermAndImageProjection == null ) {
            return null;
        }

        long imageId = 0L;
        long termId = 0L;
        long countAnnotations = 0L;

        imageId = statPerTermAndImageProjection.getImageId();
        termId = statPerTermAndImageProjection.getTermId();
        countAnnotations = statPerTermAndImageProjection.getCountAnnotations();

        StatPerTermAndImage statPerTermAndImage = new StatPerTermAndImage( imageId, termId, countAnnotations );

        return statPerTermAndImage;
    }

    protected StatTerm statUserTermProjectionToStatTerm(StatUserTermProjection statUserTermProjection) {
        if ( statUserTermProjection == null ) {
            return null;
        }

        long id = 0L;
        String name = null;
        String color = null;
        long count = 0L;

        id = statUserTermProjection.getTermId();
        name = statUserTermProjection.getTermName();
        color = statUserTermProjection.getTermColor();
        count = statUserTermProjection.getTermCount();

        StatTerm statTerm = new StatTerm( id, name, color, count );

        return statTerm;
    }
}
