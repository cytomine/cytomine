package be.cytomine.service.image;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.AbstractImage;

import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings("checkstyle:LineLength")
@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ImagePropertiesServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    ImagePropertiesService imagePropertiesService;

    private static WireMockServer wireMockServer = new WireMockServer(8888);

    @BeforeAll
    public static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    void extract_populated_properties_to_abstract_image() throws IOException, IllegalAccessException {
        AbstractImage image = builder.givenAnAbstractImage();
        image.getUploadedFile().setFilename("1636379100999/CMU-2/CMU-2.mrxs");
        image.getUploadedFile().setContentType("MRXS");

        image.setWidth(1);
        image.setPhysicalSizeX(2d);
        image.setColorspace("empty");

        configureFor("localhost", 8888); //       /image/upload1644425985928451/LUNG1_pyr.tif/info
        stubFor(get(urlEqualTo(IMS_API_BASE_PATH + "/image/" + URLEncoder.encode(
                image.getPath(),
                StandardCharsets.UTF_8
            ).replace("%2F", "/") + "/info"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {"image":
                                {
                                    "original_format":"PYRTIFF",
                                    "width":30720,
                                    "height":25600,
                                    "depth":1,
                                    "duration":1,
                                    "physical_size_x":100000.00617,
                                    "physical_size_y":100000.00617,
                                    "physical_size_z":null,
                                    "frame_rate":null,
                                    "n_channels":3,
                                    "n_concrete_channels":1,
                                    "n_samples":3,
                                    "n_planes":1,
                                    "are_rgb_planes":true,
                                    "n_distinct_channels":1,
                                    "acquired_at":null,
                                    "description":"",
                                    "pixel_type":"uint8",
                                    "significant_bits":8,"bits":8},
                                    "instrument":
                                        {"microscope":{"model":null},
                                         "objective":{"nominal_magnification":null,"calibrated_magnification":null}},
                                         "associated":[],
                                         "channels":[
                                            {"index":0,"suggested_name":"R","emission_wavelength":null,"excitation_wavelength":null,"color":"#f00"},
                                            {"index":1,"suggested_name":"G","emission_wavelength":null,"excitation_wavelength":null,"color":"#0f0"},
                                            {"index":2,"suggested_name":"B","emission_wavelength":null,"excitation_wavelength":null,"color":"#00f"}
                                         ],
                                         "representations":[
                                            {"role":"UPLOAD",
                                             "file":
                                                {"file_type":"SINGLE",
                                                "filepath":"/data/images/upload1644425985928451/LUNG1_pyr.tif",
                                                "stem":"LUNG1_pyr",
                                                "extension":".tif",
                                                "created_at":"2022-05-05T22:16:23.318839",
                                                "size":126616954,"is_symbolic":false,"role":"UPLOAD"}
                                                },
                                                {"role":"ORIGINAL",
                                                "file":
                                                    {"file_type":"SINGLE","filepath":"/data/images/upload1644425985928451/processed/original.PYRTIFF","stem":"original","extension":".PYRTIFF","created_at":"2022-05-05T22:16:23.318839","size":126616954,"is_symbolic":true,"role":"ORIGINAL"}
                                                    },
                                                    {"role":"SPATIAL","file":{"file_type":"SINGLE","filepath":"/data/images/upload1644425985928451/processed/visualisation.PYRTIFF","stem":"visualisation","extension":".PYRTIFF","created_at":"2022-05-05T22:16:23.318839","size":126616954,"is_symbolic":true,"role":"SPATIAL"},"pyramid":{"n_tiers":8,"tiers":[{"zoom":7,"level":0,"width":30720,"height":25600,"tile_width":256,"tile_height":256,"downsampling_factor":1.0,"n_tiles":12000,"n_tx":120,"n_ty":100},{"zoom":6,"level":1,"width":15360,"height":12800,"tile_width":256,"tile_height":256,"downsampling_factor":2.0,"n_tiles":3000,"n_tx":60,"n_ty":50},{"zoom":5,"level":2,"width":7680,"height":6400,"tile_width":256,"tile_height":256,"downsampling_factor":4.0,"n_tiles":750,"n_tx":30,"n_ty":25},{"zoom":4,"level":3,"width":3840,"height":3200,"tile_width":256,"tile_height":256,"downsampling_factor":8.0,"n_tiles":195,"n_tx":15,"n_ty":13},{"zoom":3,"level":4,"width":1920,"height":1600,"tile_width":256,"tile_height":256,"downsampling_factor":16.0,"n_tiles":56,"n_tx":8,"n_ty":7},{"zoom":2,"level":5,"width":960,"height":800,"tile_width":256,"tile_height":256,"downsampling_factor":32.0,"n_tiles":16,"n_tx":4,"n_ty":4},{"zoom":1,"level":6,"width":480,"height":400,"tile_width":256,"tile_height":256,"downsampling_factor":64.0,"n_tiles":4,"n_tx":2,"n_ty":2},{"zoom":0,"level":7,"width":240,"height":200,"tile_width":256,"tile_height":256,"downsampling_factor":128.0,"n_tiles":1,"n_tx":1,"n_ty":1}]}}]}
                            
                            """
                    )
                )
        );

        stubFor(get(urlEqualTo(IMS_API_BASE_PATH + "/image/" + URLEncoder.encode(
                image.getPath(),
                StandardCharsets.UTF_8
            ).replace("%2F", "/") + "/metadata"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {"size":14,"items":
                            [{"key":"ImageWidth","value":30720,"type":"INTEGER","namespace":"TIFF"},
                            {"key":"ImageLength","value":25600,"type":"INTEGER","namespace":"TIFF"},
                            {"key":"BitsPerSample","value":"(8, 8, 8)","type":"UNKNOWN","namespace":"TIFF"},
                            {"key":"Compression","value":"JPEG","type":"STRING","namespace":"TIFF"},
                            {"key":"PhotometricInterpretation","value":"YCBCR","type":"STRING","namespace":"TIFF"},
                            {"key":"Orientation","value":"TOPLEFT","type":"STRING","namespace":"TIFF"},
                            {"key":"SamplesPerPixel","value":3,"type":"INTEGER","namespace":"TIFF"},
                            {"key":"XResolution","value":"(429496703, 4294967295)","type":"UNKNOWN","namespace":"TIFF"},
                            {"key":"YResolution","value":"(429496703, 4294967295)","type":"UNKNOWN","namespace":"TIFF"},
                            {"key":"PlanarConfiguration","value":"CONTIG","type":"STRING","namespace":"TIFF"},
                            {"key":"ResolutionUnit","value":"CENTIMETER","type":"STRING","namespace":"TIFF"},
                            {"key":"TileWidth","value":256,"type":"INTEGER","namespace":"TIFF"},
                            {"key":"TileLength","value":256,"type":"INTEGER","namespace":"TIFF"},
                            {"key":"ReferenceBlackWhite","value":"(0, 1, 255, 1, 128, 1, 255, 1, 128, 1, 255, 1)","type":"UNKNOWN","namespace":"TIFF"}]
                            }
                            """
                    )
                )
        );

        imagePropertiesService.extractUseful(image);

        assertThat(image.getWidth()).isEqualTo(30720);
        assertThat(image.getHeight()).isEqualTo(25600);
        assertThat(image.getColorspace()).isEqualTo("empty");
    }

}
