package be.cytomine.appengine.dto.inputs.task;

public record TaskAuthor(String firstName, String lastName, String organization, String email, boolean isContact) {}
