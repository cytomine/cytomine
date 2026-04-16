package be.cytomine.dto.appengine.task;

import java.util.Date;

import be.cytomine.dto.UserSummary;

public record TaskRunDetail(Long project, UserSummary user, Long image, String taskRunId, Date createdAt) {}
