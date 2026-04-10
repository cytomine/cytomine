package org.cytomine.repository.service;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.ReviewedAnnotationMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.ReviewedAnnotationRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.ReviewedAnnotationEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.ReviewedAnnotationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.DeleteReviewedAnnotationCommand;
import be.cytomine.common.repository.model.command.request.UpdateReviewedAnnotationCommand;
import be.cytomine.common.repository.model.reviewedannotation.payload.CreateReviewedAnnotation;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;

@Component
@AllArgsConstructor
public class ReviewedAnnotationCommandService {
    private static final String INSERT_SQL = "INSERT INTO reviewed_annotation "
        + "(version, created, updated, user_id, review_user_id, image_id, slice_id, project_id, "
        + "parent_ident, parent_class_name, status, location, wkt_location, geometry_compression) "
        + "VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 0), ?, ?)";

    private static final String UPDATE_GEOMETRY_SQL = "UPDATE reviewed_annotation "
        + "SET location = ST_GeomFromText(?, 0), wkt_location = ?, geometry_compression = ?, updated = ? "
        + "WHERE id = ?";

    private final ReviewedAnnotationRepository reviewedAnnotationRepository;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ReviewedAnnotationMapper reviewedAnnotationMapper;
    private final ACLService aclService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Optional<HttpCommandResponse> createReviewedAnnotation(Long userId, CreateReviewedAnnotation payload,
                                                                   LocalDateTime now) {
        if (!aclService.canReadProject(userId, payload.projectId())) {
            return Optional.empty();
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, Timestamp.valueOf(now));
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setLong(3, payload.userId());
            ps.setLong(4, payload.reviewUserId());
            ps.setLong(5, payload.imageId());
            ps.setLong(6, payload.sliceId());
            ps.setLong(7, payload.projectId());
            ps.setLong(8, payload.parentIdent());
            ps.setString(9, payload.parentClassName());
            ps.setInt(10, payload.status());
            ps.setString(11, payload.wktLocation());
            ps.setString(12, payload.wktLocation());
            ps.setDouble(13, payload.geometryCompression());
            return ps;
        }, keyHolder);
        long newId = keyHolder.getKey().longValue();
        ReviewedAnnotationEntity saved = reviewedAnnotationRepository.findById(newId).orElseThrow();

        // insert term associations
        if (payload.termIds() != null) {
            insertTermLinks(newId, payload.termIds());
        }

        List<Long> termIds = getTermIds(newId);
        ReviewedAnnotationCommandPayload commandPayload = reviewedAnnotationMapper.mapToCommandPayload(saved,
            termIds);
        CreateReviewedAnnotationCommand command = new CreateReviewedAnnotationCommand(commandPayload, userId);
        CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(command, now, now, userId));

        return Optional.of(new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(saved, termIds),
            commandV2Entity.getId(), Commands.CREATE_REVIEWED_ANNOTATION));
    }

    @Transactional
    public Optional<HttpCommandResponse> updateReviewedAnnotation(Long id, Long userId,
                                                                   UpdateReviewedAnnotation payload,
                                                                   LocalDateTime now) {
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(entity -> {
                List<Long> termIds = getTermIds(id);
                ReviewedAnnotationCommandPayload before = reviewedAnnotationMapper.mapToCommandPayload(entity,
                    termIds);
                String newWkt = payload.wktLocation().orElse(entity.getWktLocation());
                double newCompression = payload.geometryCompression().orElse(entity.getGeometryCompression());
                jdbcTemplate.update(UPDATE_GEOMETRY_SQL, newWkt, newWkt, newCompression,
                    Timestamp.valueOf(now), id);
                ReviewedAnnotationEntity updated = reviewedAnnotationRepository.findById(id).orElseThrow();
                ReviewedAnnotationCommandPayload after = reviewedAnnotationMapper.mapToCommandPayload(updated,
                    termIds);
                UpdateReviewedAnnotationCommand command = new UpdateReviewedAnnotationCommand(id, before, after,
                    userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(updated, termIds),
                    commandV2Entity.getId(), Commands.UPDATE_REVIEWED_ANNOTATION);
            });
    }

    @Transactional
    public Optional<HttpCommandResponse> deleteReviewedAnnotation(Long id, Long userId, LocalDateTime now) {
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(entity -> {
                List<Long> termIds = getTermIds(id);
                ReviewedAnnotationCommandPayload before = reviewedAnnotationMapper.mapToCommandPayload(entity,
                    termIds);
                DeleteReviewedAnnotationCommand command = new DeleteReviewedAnnotationCommand(id, before, userId);
                CommandV2Entity commandV2Entity = commandV2Repository.save(
                    commandMapper.map(command, now, now, userId));
                entity.setDeleted(now);
                reviewedAnnotationRepository.save(entity);
                return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(entity, termIds),
                    commandV2Entity.getId(), Commands.DELETE_REVIEWED_ANNOTATION);
            });
    }

    public Optional<HttpCommandResponse> undoCreateReviewedAnnotation(UUID commandId,
                                                                       CreateReviewedAnnotationCommand cmd,
                                                                       Long userId, LocalDateTime now) {
        return softDelete(commandId, cmd.after().id(), Commands.CREATE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> redoCreateReviewedAnnotation(UUID commandId,
                                                                       CreateReviewedAnnotationCommand cmd,
                                                                       Long userId, LocalDateTime now) {
        return restore(commandId, cmd.after().id(), Commands.CREATE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> undoDeleteReviewedAnnotation(UUID commandId,
                                                                       DeleteReviewedAnnotationCommand cmd,
                                                                       Long userId, LocalDateTime now) {
        return restore(commandId, cmd.before().id(), Commands.DELETE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> redoDeleteReviewedAnnotation(UUID commandId,
                                                                       DeleteReviewedAnnotationCommand cmd,
                                                                       Long userId, LocalDateTime now) {
        return softDelete(commandId, cmd.before().id(), Commands.DELETE_REVIEWED_ANNOTATION, now);
    }

    public Optional<HttpCommandResponse> undoUpdateReviewedAnnotation(UUID commandId,
                                                                       UpdateReviewedAnnotationCommand cmd,
                                                                       Long userId, LocalDateTime now) {
        return reviewedAnnotationRepository.findById(cmd.before().id()).map(entity -> {
            jdbcTemplate.update(UPDATE_GEOMETRY_SQL, cmd.before().wktLocation(), cmd.before().wktLocation(),
                cmd.before().geometryCompression(), Timestamp.valueOf(now), entity.getId());
            ReviewedAnnotationEntity updated = reviewedAnnotationRepository.findById(entity.getId()).orElseThrow();
            List<Long> termIds = getTermIds(entity.getId());
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(updated, termIds),
                commandId, Commands.UPDATE_REVIEWED_ANNOTATION);
        });
    }

    public Optional<HttpCommandResponse> redoUpdateReviewedAnnotation(UUID commandId,
                                                                       UpdateReviewedAnnotationCommand cmd,
                                                                       Long userId, LocalDateTime now) {
        return reviewedAnnotationRepository.findById(cmd.after().id()).map(entity -> {
            jdbcTemplate.update(UPDATE_GEOMETRY_SQL, cmd.after().wktLocation(), cmd.after().wktLocation(),
                cmd.after().geometryCompression(), Timestamp.valueOf(now), entity.getId());
            ReviewedAnnotationEntity updated = reviewedAnnotationRepository.findById(entity.getId()).orElseThrow();
            List<Long> termIds = getTermIds(entity.getId());
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(updated, termIds),
                commandId, Commands.UPDATE_REVIEWED_ANNOTATION);
        });
    }

    private List<Long> getTermIds(long reviewedAnnotationId) {
        return jdbcTemplate.queryForList(
            "SELECT terms_id FROM reviewed_annotation_term WHERE reviewed_annotation_terms_id = ?",
            Long.class, reviewedAnnotationId);
    }

    private void insertTermLinks(long reviewedAnnotationId, List<Long> termIds) {
        for (Long termId : termIds) {
            jdbcTemplate.update(
                "INSERT INTO reviewed_annotation_term (reviewed_annotation_terms_id, terms_id) VALUES (?, ?)",
                reviewedAnnotationId, termId);
        }
    }

    private Optional<HttpCommandResponse> softDelete(UUID commandId, long entityId, String command,
                                                      LocalDateTime now) {
        return reviewedAnnotationRepository.findById(entityId).map(entity -> {
            entity.setDeleted(now);
            reviewedAnnotationRepository.save(entity);
            List<Long> termIds = getTermIds(entityId);
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(entity, termIds),
                commandId, command);
        });
    }

    private Optional<HttpCommandResponse> restore(UUID commandId, long entityId, String command,
                                                   LocalDateTime now) {
        return reviewedAnnotationRepository.findById(entityId).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(now);
            reviewedAnnotationRepository.save(entity);
            List<Long> termIds = getTermIds(entityId);
            return new HttpCommandResponse(true, reviewedAnnotationMapper.mapToResponse(entity, termIds),
                commandId, command);
        });
    }
}
