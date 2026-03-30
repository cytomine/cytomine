package be.cytomine.common.repository.http;

import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import be.cytomine.common.repository.model.CommandResponse;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface CommandHttpContract {
    String ROOT_PATH = "/commands";

    @PostExchange("/undo/{commandId}")
    Optional<HttpCommandResponse<?>> undo(@PathVariable UUID commandId, @RequestParam long userId);

    @PostExchange("/redo/{commandId}")
    Optional<HttpCommandResponse<?>> redo(@PathVariable UUID commandId, @RequestParam long userId);

    @GetExchange("/{commandId}")
    Optional<CommandResponse<?>> get(@PathVariable UUID commandId, @RequestParam long userId);
}
