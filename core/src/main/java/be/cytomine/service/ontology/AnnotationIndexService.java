package be.cytomine.service.ontology;

import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.security.User;
import be.cytomine.dto.annotation.AnnotationIndexLightDTO;
import be.cytomine.repository.ontology.AnnotationIndexRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AnnotationIndexService {

    private final AnnotationIndexRepository annotationIndexRepository;

    public List<AnnotationIndexLightDTO> list(SliceInstance sliceInstance) {
        return annotationIndexRepository.findAllLightBySliceInstance(sliceInstance.getId());
    }

    /**
     * Return the number of annotation created by this user for this slice If user is null, return the number of
     * reviewed annotation for this slice
     */
    public Long count(SliceInstance slice, User user) {
        if (user != null) {
            return annotationIndexRepository.findOneBySliceAndUser(slice, user)
                .map(AnnotationIndexLightDTO::getCountAnnotation).orElse(0L);
        } else {
            return annotationIndexRepository.findAllBySlice(slice)
                .stream().mapToLong(AnnotationIndexLightDTO::getCountReviewedAnnotation).sum();
        }
    }

    public Long count(List<SliceInstance> slices, User user) {
        if (user != null) {
            return annotationIndexRepository.findOneBySliceInAndUser(slices, user)
                .map(AnnotationIndexLightDTO::getCountAnnotation).orElse(0L);
        } else {
            return annotationIndexRepository.findAllBySliceIn(slices)
                .stream().mapToLong(AnnotationIndexLightDTO::getCountReviewedAnnotation).sum();
        }
    }

}
