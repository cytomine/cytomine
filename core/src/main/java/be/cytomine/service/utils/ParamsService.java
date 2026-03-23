package be.cytomine.service.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.AnnotationListing;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.image.ImageInstanceService;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.security.UserService;
import be.cytomine.utils.JsonObject;

/**
 * This service simplify request parameters extraction in controller
 * E.g. thanks to "/api/annotation.json?users=1,5 => it will retrieve user object with 1 and 5
 */
@Service
@Transactional
@AllArgsConstructor
public class ParamsService {

    private final UserService userService;

    private final UserRepository userRepository;

    private final ImageInstanceService imageInstanceService;

    private final TermService termService;


    /**
     * Retrieve all user id from paramsUsers request string (format users=x,y,z or x_y_z) Just get user from project
     */
    public List<Long> getParamsUserList(String paramsUsers, Project project) {
        if (paramsUsers != null && !paramsUsers.equals("null")) {
            if (!paramsUsers.equals("")) {
                List<Long> userIdsFromParams = Arrays.stream(paramsUsers.split(paramsUsers.contains("_") ? "_" : ","))
                    .map(x -> Long.parseLong(x))
                    .collect(Collectors.toList());
                return userRepository.findAllAllowedUserIdList(project.getId())
                    .stream()
                    .distinct()
                    .filter(userIdsFromParams::contains)
                    .collect(Collectors.toList());
            } else {
                return new ArrayList<>();
            }
        } else {
            return userRepository.findAllAllowedUserIdList(project.getId());
        }
    }


    /**
     * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z) Just get images from
     * project
     */
    public List<Long> getParamsImageInstanceList(String paramsImages, Project project) {
        if (paramsImages != null && !paramsImages.equals("null")) {
            if (!paramsImages.equals("")) {
                List<Long> userIdsFromParams = Arrays.stream(paramsImages.split(paramsImages.contains("_") ? "_" : ","))
                    .map(x -> Long.parseLong(x))
                    .collect(Collectors.toList());
                return imageInstanceService.getAllImageId(project)
                    .stream()
                    .distinct()
                    .filter(userIdsFromParams::contains)
                    .collect(Collectors.toList());
            } else {
                return new ArrayList<>();
            }
        } else {
            return imageInstanceService.getAllImageId(project);
        }
    }

    /**
     * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z) Just get images from
     * project
     */
    public List<Long> getParamsTermList(String paramsTerms, Project project) {
        if (paramsTerms != null && !paramsTerms.equals("null")) {
            if (!paramsTerms.equals("")) {
                List<Long> termsIdsFromParams = Arrays.stream(paramsTerms.split(paramsTerms.contains("_") ? "_" : ","))
                    .map(x -> Long.parseLong(x))
                    .collect(Collectors.toList());
                return termService.getAllTermId(project)
                    .stream()
                    .distinct()
                    .filter(termsIdsFromParams::contains)
                    .collect(Collectors.toList());
            } else {
                return new ArrayList<>();
            }
        } else {
            return termService.getAllTermId(project);
        }
    }

    private static Map<String, String> PARAMETER_ASSOCIATION = Map.of(
        "showBasic", "basic",
        "showMeta", "meta",
        "showWKT", "wkt",
        "showGIS", "gis",
        "showTerm", "term",
        "showImage", "image",
        "showUser", "user",
        "showSlice", "slice",
        "showTrack", "track"
    );


    public List<String> getPropertyGroupToShow(JsonObject params) {
        List<String> propertiesToPrint = new ArrayList<>();

        for (Map.Entry<String, String> entry : PARAMETER_ASSOCIATION.entrySet()) {
            if (params.getJSONAttrBoolean(entry.getKey(), false)) {
                propertiesToPrint.add(entry.getValue());
            }
        }


        //if no specific show asked show default prop
        if (params.getJSONAttrBoolean("showDefault", false) || propertiesToPrint.isEmpty()) {
            for (String column : AnnotationListing.availableColumnsDefault) {
                propertiesToPrint.add(column);
            }
            propertiesToPrint = propertiesToPrint.stream().distinct().collect(Collectors.toList());
        }

        //hide if asked
        for (Map.Entry<String, String> entry : PARAMETER_ASSOCIATION.entrySet()) {
            if (params.getJSONAttrBoolean(entry.getKey().replaceAll("show", "hide"), false)) {
                propertiesToPrint.remove(entry.getValue());
            }
        }

        if (propertiesToPrint.isEmpty()) {
            throw new ObjectNotFoundException("You must ask at least one properties group for request.");
        }

        return propertiesToPrint;
    }
}
