package be.cytomine.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * A task provide info about a command. The main info is the progress status THIS CLASS CANNOT BE A DOMAIN! Because it
 * cannot works with hibernate transaction.
 */
@Getter
@Setter
public class Task {

    private Long id;

    /**
     * Request progress between 0 and 100
     */
    private int progress = 0;

    /**
     * Project updated by the command task
     */
    private Long projectIdent = -1L;

    /**
     * User that ask the task
     */
    private Long userIdent;

    private boolean printInActivity = false;


    public JsonObject toJsonObject() {
        JsonObject map = new JsonObject();
        map.put("id", id);
        map.put("progress", progress);
        map.put("project", projectIdent);
        map.put("user", userIdent);
        map.put("printInActivity", printInActivity);
        return map;
    }

}
