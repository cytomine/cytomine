package be.cytomine.service;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;

@Component
public class UrlApi {

    @Value("${application.serverURL}")
    private String serverUrl;

    private static final Map<String, Set<String>> ASSOCIATED_PER_FORMAT_HINTS = Map.of(
        "macro", Set.of("SVS", "NDPI", "VMS", "SCN", "MRXS", "BIF", "VENTANA", "PHILIPS"),
        "label", Set.of("SVS", "MRXS", "PHILIPS"),
        "thumb", Set.of("SVS", "MRXS", "BIF", "VENTANA")
    );

    public String getServerUrl() {
        return serverUrl;
    }


    public String getAbstractImageThumbUrl(long idImage, String format) {
        return serverUrl + "/api/abstractimage/" + idImage + "/thumb." + format;
    }

    public String getImageInstanceThumbUrl(long idImage) {
        return serverUrl + "/api/imageinstance/" + idImage + "/thumb.png";
    }

    public String getImageInstanceThumbUrlWithMaxSize(long idImage) {
        return (getImageInstanceThumbUrlWithMaxSize(idImage, 256, "png"));
    }

    public String getImageInstanceThumbUrlWithMaxSize(long idImage, Integer maxSize, String format) {
        return serverUrl + "/api/imageinstance/" + idImage + "/thumb." + format + "?maxSize=" + maxSize;
    }

    public String getAbstractImageThumbUrlWithMaxSize(long idAbstractImage, Integer maxSize, String format) {
        return serverUrl + "/api/abstractimage/" + idAbstractImage + "/thumb." + format + "?maxSize=" + maxSize;
    }

    public String getAssociatedImage(
        long id,
        String imageType,
        String label,
        String contentType,
        Integer maxSize,
        String format
    ) {
        if (contentType != null && !ASSOCIATED_PER_FORMAT_HINTS.getOrDefault(label, Set.of()).contains(contentType)) {
            return null;
        }
        String size = maxSize != null && maxSize != 0 ? "?maxWidth=" + maxSize : "";
        return serverUrl + "/api/" + imageType + "/" + id + "/associated/" + label + "." + format + size;
    }

    public String getAssociatedImage(
        AbstractImage image,
        String label,
        String contentType,
        Integer maxSize,
        String format
    ) {
        return getAssociatedImage(image.getId(), "abstractimage", label, contentType, maxSize, format);
    }

    public String getAssociatedImage(
        ImageInstance image,
        String label,
        String contentType,
        Integer maxSize,
        String format
    ) {
        return getAssociatedImage(image.getId(), "imageinstance", label, contentType, maxSize, format);
    }

    public String getAnnotationURL(long idProject, long idImage, long idAnnotation) {
        return serverUrl + "/#/project/" + idProject + "/image/" + idImage + "/annotation/" + idAnnotation;
    }

    public String getAbstractSliceThumbUrl(long idSlice, String format) {
        return serverUrl + "/api/abstractslice/" + idSlice + "/thumb." + format;
    }

    public String getUserAnnotationCropWithAnnotationId(long idAnnotation, String format) {
        return serverUrl + "/api/userannotation/" + idAnnotation + "/crop." + format;
    }

    public String getUserAnnotationCropWithAnnotationIdWithMaxSize(
        long idAnnotation,
        int maxSize,
        String format
    ) {
        return serverUrl + "/api/userannotation/" + idAnnotation + "/crop." + format + "?maxSize=" + maxSize;
    }

    public String getReviewedAnnotationCropWithAnnotationId(long idAnnotation, String format) {
        return serverUrl + "/api/reviewedannotation/" + idAnnotation + "/crop." + format;
    }

    public String getReviewedAnnotationCropWithAnnotationIdWithMaxSize(
        long idAnnotation,
        int maxSize,
        String format
    ) {
        return serverUrl + "api/reviewedannotation/" + idAnnotation + "/crop." + format + "?maxSize=" + maxSize;
    }
}
