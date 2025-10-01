package be.cytomine.service.annotation;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.dto.annotation.AnnotationRequest;
import be.cytomine.repository.annotation.AnnotationLayerRepository;
import be.cytomine.repository.annotation.AnnotationRepository;

@Service
@RequiredArgsConstructor
public class AnnotationService {

    private final AnnotationRepository annotationRepository;

    private final AnnotationLayerRepository annotationLayerRepository;

    public Annotation add(AnnotationRequest request) {
        AnnotationLayer layer = annotationLayerRepository.findById(request.layerId())
            .orElseThrow(() -> new RuntimeException("Layer " + request.layerId() + " not found"));

        Annotation annotation = new Annotation(layer, request.location().getBytes());

        return annotationRepository.saveAndFlush(annotation);
    }

    public Annotation createAnnotation(AnnotationLayer layer, String geometry) {
        Annotation annotation = new Annotation();
        annotation.setAnnotationLayer(layer);
        annotation.setLocation(geometry.getBytes(StandardCharsets.UTF_8));

        return annotationRepository.saveAndFlush(annotation);
    }

    public Optional<Annotation> find(Long id) {
        return annotationRepository.findById(id);
    }
}
