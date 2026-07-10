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

    String CREATE_UPLOADED_FILE = "be.cytomine.AddUploadedFileCommand";
    String UPDATE_UPLOADED_FILE = "be.cytomine.EditUploadedFileCommand";
    String DELETE_UPLOADED_FILE = "be.cytomine.DeleteUploadedFileCommand";

    String CREATE_TAG_DOMAIN_ASSOCIATION = "be.cytomine.AddTagDomainAssociationCommand";
    String UPDATE_TAG_DOMAIN_ASSOCIATION = "be.cytomine.EditTagDomainAssociationCommand";
    String DELETE_TAG_DOMAIN_ASSOCIATION = "be.cytomine.DeleteTagDomainAssociationCommand";

    String CREATE_ROLE = "be.cytomine.AddSecRoleCommand";
    String UPDATE_ROLE = "be.cytomine.EditSecRoleCommand";
    String DELETE_ROLE = "be.cytomine.DeleteSecRoleCommand";

    String CREATE_USER_ROLE = "be.cytomine.AddSecUserSecRoleCommand";
    String UPDATE_USER_ROLE = "be.cytomine.EditSecUserSecRoleCommand";
    String DELETE_USER_ROLE = "be.cytomine.DeleteSecUserSecRoleCommand";

    String UNDO_CREATE_COMMAND = "be.cytomine.UndoCreateCommand";
    String UNDO_DELETE_COMMAND = "be.cytomine.UndoDeleteCommand";
    String UNDO_UPDATE_COMMAND = "be.cytomine.UndoUpdateCommand";
}
