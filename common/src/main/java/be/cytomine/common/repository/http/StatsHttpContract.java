package be.cytomine.common.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import be.cytomine.common.repository.model.stat.payload.StatTerm;

import static be.cytomine.common.repository.http.StatsHttpContract.ROOT_PATH;


@HttpExchange(ROOT_PATH)

public interface StatsHttpContract {
    String ROOT_PATH = "/stats";

    @GetExchange("/project/{projectId}")
    Page<StatTerm> findTermsByProject(@PathVariable long projectId, @RequestParam long userId,
                                      @RequestParam(required = false) Optional<LocalDateTime> startDate,
                                      @RequestParam(required = false) Optional<LocalDateTime> endDate,
                                      @RequestParam int page, @RequestParam int size);
}
