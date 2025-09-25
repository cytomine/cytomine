package be.cytomine.service.database;

import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.config.properties.ApplicationProperties;
import be.cytomine.domain.meta.ConfigurationReadingRole;
import be.cytomine.domain.processing.ImageFilter;
import be.cytomine.repository.processing.ImageFilterRepository;

import static be.cytomine.domain.ontology.RelationTerm.PARENT;

@RequiredArgsConstructor
@Transactional
@Service
public class BootstrapDataService {

    private final BootstrapUtilsService bootstrapUtilsService;

    private final ApplicationProperties applicationProperties;

    private final ImageFilterRepository imageFilterRepository;

    public void initData() {

        initImageFilters();

        bootstrapUtilsService.createMime("tif", "image/pyrtiff");
        bootstrapUtilsService.createMime("jp2", "image/jp2");
        bootstrapUtilsService.createMime("ndpi", "openslide/ndpi");
        bootstrapUtilsService.createMime("mrxs", "openslide/mrxs");
        bootstrapUtilsService.createMime("vms", "openslide/vms");
        bootstrapUtilsService.createMime("svs", "openslide/svs");
        bootstrapUtilsService.createMime("scn", "openslide/scn");
        bootstrapUtilsService.createMime("bif", "openslide/bif");
        bootstrapUtilsService.createMime("tif", "openslide/ventana");
        bootstrapUtilsService.createMime("tif", "philips/tif");

        bootstrapUtilsService.createRole("ROLE_USER");
        bootstrapUtilsService.createRole("ROLE_ADMIN");
        bootstrapUtilsService.createRole("ROLE_SUPER_ADMIN");
        bootstrapUtilsService.createRole("ROLE_GUEST");

        bootstrapUtilsService.createUser("ImageServer1", "Image", "Server", List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"));

        bootstrapUtilsService.createRelation(PARENT);

        bootstrapUtilsService.createConfigurations("WELCOME", "<p>Welcome to the Cytomine software.</p><p>This software is supported by the <a href='https://cytomine.coop'>Cytomine company</a></p>", ConfigurationReadingRole.ALL);
        bootstrapUtilsService.createConfigurations("admin_email", applicationProperties.getAdminEmail(), ConfigurationReadingRole.ADMIN);
    }

    public void initImageFilters() {

        List<Map<String, Object>> filters = List.of(
                Map.of("name", "Binary", "method","binary", "available", true),
                Map.of("name", "Huang Threshold", "method","huang", "available", false),
                Map.of("name", "Intermodes Threshold", "method","intermodes", "available", false),
                Map.of("name", "IsoData Threshold", "method","isodata", "available", true),
                Map.of("name", "Li Threshold", "method","li", "available", false),
                Map.of("name", "Max Entropy Threshold", "method","maxentropy", "available", false),
                Map.of("name", "Mean Threshold", "method","mean", "available", true),
                Map.of("name", "Minimum Threshold", "method","minimum", "available", true),
                Map.of("name", "MinError(I) Threshold", "method","minerror", "available", false),
                Map.of("name", "Moments Threshold", "method","moments", "available", false),
                Map.of("name", "Otsu Threshold", "method","otsu", "available", true),
                Map.of("name", "Renyi Entropy Threshold", "method","renyientropy", "available", false),
                Map.of("name", "Shanbhag Threshold", "method","shanbhag", "available", false),
                Map.of("name", "Triangle Threshold", "method","triangle", "available", false),
                Map.of("name", "Yen Threshold", "method","yen", "available", true),
                Map.of("name", "Percentile Threshold", "method","percentile", "available", false),
                Map.of("name", "H&E Haematoxylin", "method","he-haematoxylin", "available", true),
                Map.of("name", "H&E Eosin", "method","he-eosin", "available", true),
                Map.of("name", "HDAB Haematoxylin", "method","hdab-haematoxylin", "available", true),
                Map.of("name", "HDAB DAB", "method","hdab-dab", "available", true),
                Map.of("name", "Haematoxylin", "method","haematoxylin", "available", false), //To be removed: does not exist
                Map.of("name", "Eosin", "method","eosin", "available", false), //To be removed: does not exist
                Map.of("name", "Red (RGB)", "method","r_rgb", "available", true),
                Map.of("name", "Green (RGB)", "method","g_rgb", "available", true),
                Map.of("name", "Blue (RGB)", "method","b_rgb", "available", true),
                Map.of("name", "Cyan (CMY)", "method","c_cmy", "available", true),
                Map.of("name", "Magenta (CMY)", "method","m_cmy", "available", true),
                Map.of("name", "Yellow (CMY)", "method","y_cmy", "available", true)
        );

        for (Map<String, Object> filter : filters) {
            bootstrapUtilsService.createFilter((String)filter.get("name"), (String)filter.get("method"), (Boolean)filter.get("available"));
        }
    }

    public void updateImageFilters() {
        ImageFilter imageFilter = imageFilterRepository.findAll().stream().filter(x -> x.getName().equals("Binary")).findFirst().orElse(null);
        if (imageFilter!=null) {
            if (imageFilter.getMethod()==null) {
                // still old image filter data
                initImageFilters();
            }
        }
    }
}
