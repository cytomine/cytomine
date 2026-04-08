package be.cytomine.common.repository.model.command.payload.response;

import java.util.UUID;

import be.cytomine.common.repository.model.command.request.CommandV2Request;

/**
 * This payload is used for the Command API only
 */
public record CommandV2Response<T>(UUID id, CommandV2Request<T> commandRequest) {
}
