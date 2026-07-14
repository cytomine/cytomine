package be.cytomine.controller.repository;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.CommandHttpContract;
import be.cytomine.common.repository.model.command.payload.response.CommandV2Response;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class CommandV2Controller {
    private final CommandHttpContract commandHttpContract;
    private final CurrentUserService currentUserService;
    private final PageMapper pageMapper;

    @GetMapping("/commands")
    public CollectionResponse<CommandV2Response<?>> getAll(
        @SortDefault(sort = "created", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        long userId = currentUserService.getCurrentUser().getId();
        log.debug("GET /commands for user {}", userId);

        return pageMapper.toCollectionResponse(commandHttpContract.getAllForUser(userId, pageable));
    }

    @PostMapping("/commands/undo/{commandId}")
    public Optional<HttpCommandResponse> undo(@PathVariable UUID commandId) {
        long userId = currentUserService.getCurrentUser().getId();
        log.debug("POST /commands/undo/{} for user {}", commandId, userId);

        return commandHttpContract.undo(commandId, userId);
    }
}
