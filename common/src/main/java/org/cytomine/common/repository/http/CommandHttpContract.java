package org.cytomine.common.repository.http;

import java.util.Optional;
import java.util.UUID;

import org.cytomine.common.repository.model.command.payload.response.CommandV2Response;
import org.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(CommandHttpContract.ROOT_PATH)
public interface CommandHttpContract {
    String ROOT_PATH = "/commands";

    @PostExchange("/undo/{commandId}")
    Optional<HttpCommandResponse> undo(@PathVariable UUID commandId);

    @GetExchange("/{commandId}")
    Optional<CommandV2Response<?>> get(@PathVariable UUID commandId);

    @GetExchange("/all")
    Page<CommandV2Response<?>> getAllForUser(Pageable pageable);
}
