package be.cytomine.controller.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.UploadedFileHttpContract;
import be.cytomine.controller.RestCytomineController;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.image.UploadedFileService;
import be.cytomine.utils.RequestParams;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestUploadedFileController extends RestCytomineController {

    private final UploadedFileService uploadedFileService;

    private final CurrentUserService currentUserService;

    @GetMapping("/uploadedfile.json")
    public ResponseEntity<String> list(
        @RequestParam(defaultValue = "false") Boolean onlyRootsWithDetails,
        @RequestParam(defaultValue = "true") Boolean withTreeDetails,
        @RequestParam(required = false) Long parent,
        @RequestParam(required = false) Long root,
        @RequestParam(defaultValue = "false") Boolean all
    ) {
        log.debug("REST request to list uploaded files");

        RequestParams requestParams = retrievePageableParameters();
        if (root != null) {
            return responseSuccess(uploadedFileService.listHierarchicalTree(currentUserService.getCurrentUser(), root));
        } else if (onlyRootsWithDetails) {
            return responseSuccess(uploadedFileService.list(
                    retrieveSearchParameters(),
                    requestParams.getSort(),
                    requestParams.getOrder(),
                    withTreeDetails
                )
            );
        } else if (all) {
            return responseSuccess(uploadedFileService.list(retrievePageable()));
        } else {
            return responseSuccess(uploadedFileService.list(
                currentUserService.getCurrentUser(),
                parent,
                onlyRoots,
                retrievePageable()
            ));
        }
    }
}
