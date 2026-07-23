package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.request.CommandV2Request;

public record CommandV2Response<T extends HasLongId & HasAclId>(
    UUID id,
    CommandV2Request<T> commandRequest,
    LocalDateTime created
) {}
