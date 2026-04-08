package be.cytomine.dto.appengine.task;

import java.util.Date;

public record TaskRunDetail(Long project, Long user, Long image, String taskRunId, Date createdAt) {}
