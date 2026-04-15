package be.cytomine.common.repository.model.command;

public interface Commands {
    String CREATE_TERM = "be.cytomine.AddTermCommand";
    String CREATE_TERM_RELATION = "be.cytomine.AddTermRelationCommand";
    String CREATE_ANNOTATION_TERM = "be.cytomine.AddAnnotationTermCommand";
    String CREATE_USER_ANNOTATION = "be.cytomine.AddUserAnnotationCommand";
    String CREATE_REVIEWED_ANNOTATION = "be.cytomine.AddReviewedAnnotationCommand";

    String UPDATE_TERM = "be.cytomine.EditTermCommand";
    String UPDATE_TERM_RELATION = "be.cytomine.EditTermRelationCommand";
    String UPDATE_USER_ANNOTATION = "be.cytomine.EditUserAnnotationCommand";
    String UPDATE_REVIEWED_ANNOTATION = "be.cytomine.EditReviewedAnnotationCommand";

    String DELETE_TERM = "be.cytomine.DeleteTermCommand";
    String DELETE_TERM_RELATION = "be.cytomine.DeleteTermRelationCommand";
    String DELETE_ANNOTATION_TERM = "be.cytomine.DeleteAnnotationTermCommand";
    String DELETE_USER_ANNOTATION = "be.cytomine.DeleteUserAnnotationCommand";
    String DELETE_REVIEWED_ANNOTATION = "be.cytomine.DeleteReviewedAnnotationCommand";
}
