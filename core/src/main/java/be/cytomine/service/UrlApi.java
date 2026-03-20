package be.cytomine.service;

import java.util.Map;
import java.util.Set;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;

@SuppressWarnings("checkstyle:all") // This file will be refactored in https://github.com/cytomine/cytomine/issues/596
public class UrlApi {

    private static String serverUrl = "http://localhost:8090";

    private static final Map<String, Set<String>> ASSOCIATED_PER_FORMAT_HINTS = Map.of(
            "macro", Set.of("SVS", "NDPI", "VMS", "SCN", "MRXS", "BIF", "VENTANA", "PHILIPS"),
            "label", Set.of("SVS", "MRXS", "PHILIPS"),
            "thumb", Set.of("SVS", "MRXS", "BIF", "VENTANA")
    );

    public static void setServerURL(String url) {
        serverUrl = url;
    }

    public static String getServerUrl() {
        return serverUrl;
    }


    public static String getAbstractImageThumbUrl(Long idImage, String format) {
        return  serverUrl + "/api/abstractimage/" + idImage + "/thumb." + format;
    }

    public static String getImageInstanceThumbUrl(Long idImage) {
        return serverUrl + "/api/imageinstance/"+idImage+"/thumb.png";
    }

    public static String getImageInstanceThumbUrlWithMaxSize(Long idImage) {
        return (getImageInstanceThumbUrlWithMaxSize(idImage, 256, "png"));
    }

    public static String getImageInstanceThumbUrlWithMaxSize(Long idImage, Integer maxSize, String format) {
        return serverUrl + "/api/imageinstance/"+idImage+"/thumb."+format+"?maxSize=" + maxSize;
    }

    public static String getAbstractImageThumbUrlWithMaxSize(Long idAbstractImage, Integer maxSize, String format) {
        return serverUrl+"/api/abstractimage/"+idAbstractImage+"/thumb."+format+"?maxSize=" + maxSize;
    }

    public static String getAssociatedImage(Long id, String imageType, String label, String contentType, Integer maxSize, String format) {
        if(contentType!=null && !ASSOCIATED_PER_FORMAT_HINTS.getOrDefault(label, Set.of()).contains(contentType)) {
            return null;
        }
        String size = maxSize!=null && maxSize!=0 ? "?maxWidth=" + maxSize : "";
        return serverUrl + "/api/"+imageType+"/" + id + "/associated/" + label +"." + format + size;
    }

    public static String getAssociatedImage(AbstractImage image, String label, String contentType, Integer maxSize, String format) {
        return getAssociatedImage(image.getId(), "abstractimage", label, contentType, maxSize, format);
    }
    public static String getAssociatedImage(ImageInstance image, String label, String contentType, Integer maxSize, String format) {
        return getAssociatedImage(image.getId(), "imageinstance", label, contentType, maxSize, format);
    }

    public static String getAnnotationURL(Long idProject, Long idImage, Long idAnnotation) {
        return  serverUrl + "/#/project/"+idProject+"/image/"+idImage+"/annotation/" + idAnnotation;
    }

    public static String getAbstractSliceThumbUrl(Long idSlice, String format) {
        return serverUrl + "/api/abstractslice/" + idSlice + "/thumb." + format;
    }

    public static String getUserAnnotationCropWithAnnotationId(Long idAnnotation, String format) {
        return serverUrl + "/api/userannotation/"+idAnnotation+"/crop." + format;
    }

    public static String getUserAnnotationCropWithAnnotationIdWithMaxSize(Long idAnnotation, int maxSize, String format) {
        return serverUrl + "/api/userannotation/"+idAnnotation+"/crop." + format + "?maxSize=" + maxSize;
    }

    public static String getReviewedAnnotationCropWithAnnotationId(Long idAnnotation, String format) {
        return serverUrl + "/api/reviewedannotation/"+idAnnotation+"/crop." + format;
    }

    public static String getReviewedAnnotationCropWithAnnotationIdWithMaxSize(Long idAnnotation, int maxSize, String format) {
        return serverUrl + "api/reviewedannotation/"+idAnnotation+"/crop."+format+"?maxSize=" + maxSize;
    }

    public static String getAnnotationCropWithAnnotationId(Long idAnnotation, int maxSize, String format) {
        return serverUrl + "/api/annotation/"+idAnnotation+"/crop." + format + (maxSize!=0? "?maxSize=" + maxSize :"");
    }
}
