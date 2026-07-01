package be.cytomine.common.repository.model.command;

public interface Commands {
    String CREATE_TERM = "be.cytomine.AddTermCommand";
    String CREATE_TERM_RELATION = "be.cytomine.AddTermRelationCommand";
    String CREATE_ONTOLOGY = "be.cytomine.AddOntologyCommand";

    String UPDATE_TERM = "be.cytomine.EditTermCommand";
    String UPDATE_TERM_RELATION = "be.cytomine.EditTermRelationCommand";
    String UPDATE_ONTOLOGY = "be.cytomine.EditOntologyCommand";

    String DELETE_TERM = "be.cytomine.DeleteTermCommand";
    String DELETE_TERM_RELATION = "be.cytomine.DeleteTermRelationCommand";
    String DELETE_ONTOLOGY = "be.cytomine.DeleteOntologyCommand";

    String CREATE_STORAGE = "be.cytomine.AddStorageCommand";
    String UPDATE_STORAGE = "be.cytomine.EditStorageCommand";
    String DELETE_STORAGE = "be.cytomine.DeleteStorageCommand";
}
