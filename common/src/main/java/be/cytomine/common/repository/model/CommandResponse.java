package be.cytomine.common.repository.model;

import java.util.UUID;

import be.cytomine.common.repository.model.command.CommandV2Request;

public record CommandResponse<T>(UUID id, CommandV2Request<T> commandRequest) {
}
