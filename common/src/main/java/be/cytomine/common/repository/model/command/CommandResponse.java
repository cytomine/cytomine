package be.cytomine.common.repository.model.command;

import java.util.UUID;

import be.cytomine.common.repository.model.command.request.CommandV2Request;

public record CommandResponse<T>(UUID id, CommandV2Request<T> commandRequest) {
}
