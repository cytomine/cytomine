package be.cytomine.appengine.dto.responses;

public record PingResponse(String status,
                           String timestamp,
                           String service) {}
