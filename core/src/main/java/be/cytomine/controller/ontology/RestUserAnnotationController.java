package be.cytomine.controller.ontology;

import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.image.CropParameter;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.service.middleware.ImageServerService;
import be.cytomine.service.ontology.SharedAnnotationService;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.ontology.UserAnnotationService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.report.ReportService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.JsonObject;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestUserAnnotationController extends RestCytomineController {

    private final UserAnnotationService userAnnotationService;

    private final ProjectService projectService;

    private final TermService termService;

    private final ReportService reportService;

    private final ImageServerService imageServerService;

    private final SharedAnnotationService sharedAnnotationService;

    private final AnnotationListingBuilder annotationListingBuilder;

    @GetMapping("/userannotation.json")
    public ResponseEntity<String> listLight() {
        log.debug("REST request to list user annotation light");
        return responseSuccess(userAnnotationService.listLight());
    }


    /**
     * Download a report (pdf, xls,...) with user annotation data from a specific project
     */
    @GetMapping("/project/{idProject}/userannotation/download")
    public void downloadDocumentByProject(
        @PathVariable Long idProject,
        @RequestParam String format,
        @RequestParam String terms,
        @RequestParam Optional<String> users,
        @RequestParam String images,
        @RequestParam(required = false) Long beforeThan,
        @RequestParam(required = false) Long afterThan
    ) throws IOException {
        Project project = projectService.find(idProject)
            .orElseThrow(() -> new ObjectNotFoundException("Project", idProject));
        String userIds = users.filter(s -> !s.isBlank())
            .orElseGet(() -> projectService.getUserIdsFromProject(project.getId()));
        terms = termService.fillEmptyTermIds(terms, project);
        JsonObject params = mergeQueryParamsAndBodyParams();
        byte[] report = annotationListingBuilder.buildAnnotationReport(idProject, userIds, params, terms, format);
        responseReportFile(reportService.getAnnotationReportFileName(format, idProject), report, format);
    }

    /**
     * Add comment on an annotation to other user
     */
    @PostMapping("/userannotation/{annotation}/comment.json")
    public ResponseEntity<String> addComment(
        @PathVariable(value = "annotation") Long annotationId,
        @RequestBody JsonObject json
    ) {
        log.debug("REST request to create comment for annotation : " + json);
        UserAnnotation annotation = userAnnotationService.find(annotationId)
            .orElseThrow(() -> new ObjectNotFoundException("Annotation", annotationId));
        json.put("annotationIdent", annotation.getId());
        json.put("annotationClassName", annotation.getClass().getName());
        return responseSuccess(sharedAnnotationService.add(json));
    }


    /**
     * Show a single comment for an annotation
     */
    @GetMapping("/userannotation/{annotation}/comment/{id}.json")
    public ResponseEntity<String> showComment(
        @PathVariable(value = "annotation") Long annotationId,
        @PathVariable(value = "id") Long commentId
    ) {
        log.debug("REST request to read comment {} for annotation {}", commentId, annotationId);
        UserAnnotation annotation = userAnnotationService.find(annotationId)
            .orElseThrow(() -> new ObjectNotFoundException("Annotation", annotationId));
        return responseSuccess(sharedAnnotationService.find(commentId).orElseThrow(() ->
            new ObjectNotFoundException("SharedAnnotation", commentId)));
    }

    /**
     * List all comments for an annotation
     */
    @GetMapping("/userannotation/{annotation}/comment.json")
    public ResponseEntity<String> listComments(
        @PathVariable(value = "annotation") Long annotationId
    ) {
        log.debug("REST request to read comments for annotation {}", annotationId);
        UserAnnotation annotation = userAnnotationService.find(annotationId)
            .orElseThrow(() -> new ObjectNotFoundException("Annotation", annotationId));
        return responseSuccess(sharedAnnotationService.listComments(annotation));
    }


    @PostMapping("/userannotation/{id}/repeat.json")
    public ResponseEntity<String> repeat(
        @RequestBody JsonObject json,
        @PathVariable Long id
    ) {
        log.debug("REST request to repeat user annotation : {} ", id);

        UserAnnotation annotation = userAnnotationService.find(id)
            .orElseThrow(() -> new ObjectNotFoundException("Annotation", id));
        return responseSuccess(userAnnotationService.repeat(
            annotation, json.getJSONAttrLong("repeat", 1L), json.getJSONAttrInteger("slice", null)));
    }


    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @RequestMapping(value = "/userannotation/{id}/crop.{format}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> crop(
        @PathVariable Long id,
        @PathVariable String format,
        @RequestParam(required = false) Integer maxSize,
        @RequestParam(required = false) String geometry,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String boundaries,
        @RequestParam(defaultValue = "false") Boolean complete,
        @RequestParam(required = false) Integer zoom,
        @RequestParam(required = false) Double increaseArea,
        @RequestParam(required = false) Boolean safe,
        @RequestParam(required = false) Boolean square,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Boolean draw,
        @RequestParam(required = false) Boolean mask,
        @RequestParam(required = false) Boolean alphaMask,
        @RequestParam(required = false) Boolean drawScaleBar,
        @RequestParam(required = false) Double resolution,
        @RequestParam(required = false) Double magnification,
        @RequestParam(required = false) String colormap,
        @RequestParam(required = false) Boolean inverse,
        @RequestParam(required = false) Double contrast,
        @RequestParam(required = false) Double gamma,
        @RequestParam(required = false) String bits,
        @RequestParam(required = false) Integer alpha,
        @RequestParam(required = false) Integer thickness,
        @RequestParam(required = false) String color,
        @RequestParam(required = false) Integer jpegQuality,
        ProxyExchange<byte[]> proxy
    ) throws IOException, ParseException {
        log.debug("REST request to get associated image of a abstract image");
        UserAnnotation userAnnotation = userAnnotationService.find(id)
            .orElseThrow(() -> new ObjectNotFoundException("UserAnnotation", id));

        CropParameter cropParameter = new CropParameter();
        cropParameter.setGeometry(geometry);
        cropParameter.setLocation(location);
        cropParameter.setComplete(complete);
        cropParameter.setZoom(zoom);
        cropParameter.setIncreaseArea(increaseArea);
        cropParameter.setSafe(safe);
        cropParameter.setSquare(square);
        cropParameter.setType(type);
        cropParameter.setDraw(draw);
        cropParameter.setMask(mask);
        cropParameter.setAlphaMask(alphaMask);
        cropParameter.setDrawScaleBar(drawScaleBar);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setResolution(resolution);
        cropParameter.setMagnification(magnification);
        cropParameter.setColormap(colormap);
        cropParameter.setInverse(inverse);
        cropParameter.setGamma(gamma);
        cropParameter.setAlpha(alpha);
        cropParameter.setContrast(contrast);
        cropParameter.setThickness(thickness);
        cropParameter.setColor(color);
        cropParameter.setJpegQuality(jpegQuality);
        cropParameter.setMaxBits(bits != null && bits.equals("max"));
        cropParameter.setBits(bits != null && !bits.equals("max") ? Integer.parseInt(bits) : null);
        cropParameter.setFormat(format);
        String etag = getRequestETag();
        return imageServerService.crop(userAnnotation, cropParameter, etag, proxy);
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @RequestMapping(value = "/userannotation/{id}/mask.{format}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> cropMask(
        @PathVariable Long id,
        @PathVariable String format,
        @RequestParam(required = false) Integer maxSize,
        @RequestParam(required = false) String geometry,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String boundaries,
        @RequestParam(defaultValue = "false") Boolean complete,
        @RequestParam(required = false) Integer zoom,
        @RequestParam(required = false) Double increaseArea,
        @RequestParam(required = false) Boolean safe,
        @RequestParam(required = false) Boolean square,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Boolean draw,
        @RequestParam(required = false) Boolean drawScaleBar,
        @RequestParam(required = false) Double resolution,
        @RequestParam(required = false) Double magnification,
        @RequestParam(required = false) String colormap,
        @RequestParam(required = false) Boolean inverse,
        @RequestParam(required = false) Double contrast,
        @RequestParam(required = false) Double gamma,
        @RequestParam(required = false) String bits,
        @RequestParam(required = false) Integer alpha,
        @RequestParam(required = false) Integer thickness,
        @RequestParam(required = false) String color,
        @RequestParam(required = false) Integer jpegQuality,

        ProxyExchange<byte[]> proxy
    ) throws IOException, ParseException {
        log.debug("REST request to get associated image of a abstract image");
        UserAnnotation userAnnotation = userAnnotationService.find(id)
            .orElseThrow(() -> new ObjectNotFoundException("UserAnnotation", id));

        CropParameter cropParameter = new CropParameter();
        cropParameter.setGeometry(geometry);
        cropParameter.setLocation(location);
        cropParameter.setComplete(complete);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setZoom(zoom);
        cropParameter.setIncreaseArea(increaseArea);
        cropParameter.setSafe(safe);
        cropParameter.setSquare(square);
        cropParameter.setType(type);
        cropParameter.setDraw(draw);
        cropParameter.setMask(true);
        cropParameter.setAlphaMask(false);
        cropParameter.setDrawScaleBar(drawScaleBar);
        cropParameter.setResolution(resolution);
        cropParameter.setMagnification(magnification);
        cropParameter.setColormap(colormap);
        cropParameter.setInverse(inverse);
        cropParameter.setGamma(gamma);
        cropParameter.setAlpha(alpha);
        cropParameter.setContrast(contrast);
        cropParameter.setThickness(thickness);
        cropParameter.setColor(color);
        cropParameter.setJpegQuality(jpegQuality);
        cropParameter.setMaxBits(bits != null && bits.equals("max"));
        cropParameter.setBits(bits != null && !bits.equals("max") ? Integer.parseInt(bits) : null);
        cropParameter.setFormat(format);
        String etag = getRequestETag();
        return imageServerService.crop(userAnnotation, cropParameter, etag, proxy);
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @RequestMapping(value = "/userannotation/{id}/alphamask.{format}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> cropAlphaMask(
        @PathVariable Long id,
        @PathVariable String format,
        @RequestParam(required = false) Integer maxSize,
        @RequestParam(required = false) String geometry,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String boundaries,
        @RequestParam(defaultValue = "false") Boolean complete,
        @RequestParam(required = false) Integer zoom,
        @RequestParam(required = false) Double increaseArea,
        @RequestParam(required = false) Boolean safe,
        @RequestParam(required = false) Boolean square,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Boolean draw,
        @RequestParam(required = false) Boolean drawScaleBar,
        @RequestParam(required = false) Double resolution,
        @RequestParam(required = false) Double magnification,
        @RequestParam(required = false) String colormap,
        @RequestParam(required = false) Boolean inverse,
        @RequestParam(required = false) Double contrast,
        @RequestParam(required = false) Double gamma,
        @RequestParam(required = false) String bits,
        @RequestParam(required = false) Integer alpha,
        @RequestParam(required = false) Integer thickness,
        @RequestParam(required = false) String color,
        @RequestParam(required = false) Integer jpegQuality,

        ProxyExchange<byte[]> proxy
    ) throws IOException, ParseException {
        log.debug("REST request to get associated image of a abstract image");
        UserAnnotation userAnnotation = userAnnotationService.find(id)
            .orElseThrow(() -> new ObjectNotFoundException("UserAnnotation", id));

        CropParameter cropParameter = new CropParameter();
        cropParameter.setGeometry(geometry);
        cropParameter.setLocation(location);
        cropParameter.setComplete(complete);
        cropParameter.setZoom(zoom);
        cropParameter.setIncreaseArea(increaseArea);
        cropParameter.setSafe(safe);
        cropParameter.setSquare(square);
        cropParameter.setType(type);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setDraw(draw);
        cropParameter.setAlphaMask(true);
        cropParameter.setDrawScaleBar(drawScaleBar);
        cropParameter.setResolution(resolution);
        cropParameter.setMagnification(magnification);
        cropParameter.setColormap(colormap);
        cropParameter.setInverse(inverse);
        cropParameter.setGamma(gamma);
        cropParameter.setAlpha(alpha);
        cropParameter.setContrast(contrast);
        cropParameter.setThickness(thickness);
        cropParameter.setColor(color);
        cropParameter.setJpegQuality(jpegQuality);
        cropParameter.setMaxBits(bits != null && bits.equals("max"));
        cropParameter.setBits(bits != null && !bits.equals("max") ? Integer.parseInt(bits) : null);
        cropParameter.setFormat(format);
        String etag = getRequestETag();
        return imageServerService.crop(userAnnotation, cropParameter, etag, proxy);
    }
}
