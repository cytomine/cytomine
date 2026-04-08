package be.cytomine.common.repository.model.command;

public interface Commands {
    String CREATE_TERM = "be.cytomine.AddTermCommand";
    String CREATE_TERM_RELATION = "be.cytomine.AddTermRelationCommand";

    String UPDATE_TERM = "be.cytomine.EditTermCommand";
    String UPDATE_TERM_RELATION = "be.cytomine.EditTermRelationCommand";

    String DELETE_TERM = "be.cytomine.DeleteTermCommand";
    String DELETE_TERM_RELATION = "be.cytomine.DeleteTermRelationCommand";
}
