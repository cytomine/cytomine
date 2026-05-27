package be.cytomine.controller.ontology;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ReviewedAnnotationResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.config.properties.ApplicationProperties;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.repository.ontology.ReviewedAnnotationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("checkstyle:LineLength")
@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class ReviewedAnnotationResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restReviewedAnnotationControllerMockMvc;

    @Autowired
    private ReviewedAnnotationRepository reviewedAnnotationRepository;

    @Autowired
    private ApplicationProperties applicationProperties;
    @MockitoBean
    private ReviewedAnnotationHttpContract reviewedAnnotationHttpContract;

    private Project project;
    private ImageInstance image;
    private Term term;
    private User me;
    private ReviewedAnnotation reviewedAnnotation;

    public static ReviewedAnnotation givenAReviewedAnnotationWithValidImageServer(BasicInstanceBuilder builder)
        throws ParseException {
        AbstractImage image = builder.givenAnAbstractImage();
        image.setWidth(109240);
        image.setHeight(220696);
        image.getUploadedFile().setFilename("1636379100999/CMU-2/CMU-2.mrxs");
        image.getUploadedFile().setContentType("MRXS");
        ImageInstance imageInstance = builder.givenAnImageInstance(image, builder.givenAProject());
        imageInstance.setInstanceFilename("CMU-2");
        AbstractSlice slice = builder.givenAnAbstractSlice(image, 0, 0, 0);
        slice.setUploadedFile(image.getUploadedFile());
        SliceInstance sliceInstance = builder.givenASliceInstance(imageInstance, slice);
        return builder.givenAReviewedAnnotation(sliceInstance, "POLYGON((1 1,50 10,50 50,10 50,1 1))",
            builder.givenSuperAdmin(), null);
    }

    @Test
    @Transactional
    public void getAReviewedAnnotation() throws Exception {
        Long userId = builder.givenSuperAdmin().getId();
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        when(reviewedAnnotationHttpContract.findById(eq(reviewedAnnotation.getId()), eq(userId))).thenReturn(
            Optional.of(new ReviewedAnnotationResponse(reviewedAnnotation.getId(), userId, userId,
                reviewedAnnotation.getImage().getId(), reviewedAnnotation.getSlice().getId(),
                reviewedAnnotation.getProject().getId(), reviewedAnnotation.getParentIdent(),
                reviewedAnnotation.getParentClassName(), 0, reviewedAnnotation.getWktLocation(), 0.0, List.of(),
                LocalDateTime.now(), LocalDateTime.now(), Optional.empty())));

        restReviewedAnnotationControllerMockMvc.perform(
                get("/api/reviewedannotation/{id}.json", reviewedAnnotation.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reviewedAnnotation.getId().intValue()))
            .andExpect(jsonPath("$.imageId").value(reviewedAnnotation.getImage().getId().intValue()))
            .andExpect(jsonPath("$.projectId").value(reviewedAnnotation.getProject().getId().intValue()))
            .andExpect(jsonPath("$.userId").value(userId.intValue()))
            .andExpect(jsonPath("$.wktLocation").value(reviewedAnnotation.getWktLocation()));
    }

    @Test
    @Transactional
    public void getAReviewedAnnotationNotExists() throws Exception {
        Long userId = builder.givenSuperAdmin().getId();
        when(reviewedAnnotationHttpContract.findById(eq(0L), eq(userId))).thenReturn(Optional.empty());
        restReviewedAnnotationControllerMockMvc.perform(get("/api/reviewedannotation/{id}.json", 0))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void listAnnotations() throws Exception {
        restReviewedAnnotationControllerMockMvc.perform(get("/api/reviewedannotation.json")).andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void statsAnnotations() throws Exception {
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        restReviewedAnnotationControllerMockMvc.perform(
                get("/api/imageinstance/{image}/reviewedannotation/stats.json", reviewedAnnotation.getImage().getId()))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void countAnnotationsByProject() throws Exception {
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        restReviewedAnnotationControllerMockMvc.perform(
                get("/api/project/{idUser}/reviewedannotation/count.json", reviewedAnnotation.getProject().getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(greaterThan(0)));

        Project projectWithoutAnnotation = builder.givenAProject();
        restReviewedAnnotationControllerMockMvc.perform(
                get("/api/project/{idUser}/reviewedannotation/count.json", projectWithoutAnnotation.getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @Transactional
    public void countAnnotationsByProjectWithDates() throws Exception {
        ReviewedAnnotation oldReviewedAnnotation = builder.givenAReviewedAnnotation();
        oldReviewedAnnotation.setCreated(DateUtils.addDays(new Date(), -1));

        ReviewedAnnotation newReviewedAnnotation =
            builder.persistAndReturn(builder.givenANotPersistedReviewedAnnotation(oldReviewedAnnotation.getProject()));


        restReviewedAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/reviewedannotation/count.json",
                oldReviewedAnnotation.getProject().getId()).param("startDate",
                    String.valueOf(oldReviewedAnnotation.getCreated().getTime()))
                .param("endDate", String.valueOf(newReviewedAnnotation.getCreated().getTime()))).andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2));

        restReviewedAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/reviewedannotation/count.json",
                oldReviewedAnnotation.getProject().getId()).param("startDate",
                String.valueOf(DateUtils.addSeconds(oldReviewedAnnotation.getCreated(), -1).getTime())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2));

        restReviewedAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/reviewedannotation/count.json",
                oldReviewedAnnotation.getProject().getId()).param("endDate",
                String.valueOf(DateUtils.addSeconds(newReviewedAnnotation.getCreated(), 1).getTime())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2));

        restReviewedAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/reviewedannotation/count.json",
                oldReviewedAnnotation.getProject().getId()).param("startDate",
                String.valueOf(DateUtils.addSeconds(newReviewedAnnotation.getCreated(), -1).getTime())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));

        restReviewedAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/reviewedannotation/count.json",
                oldReviewedAnnotation.getProject().getId()).param("endDate",
                String.valueOf(DateUtils.addSeconds(oldReviewedAnnotation.getCreated(), 1).getTime())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));

        restReviewedAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/reviewedannotation/count.json",
                oldReviewedAnnotation.getProject().getId()).param("endDate",
                String.valueOf(DateUtils.addDays(oldReviewedAnnotation.getCreated(), -2).getTime())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
    }


    @Test
    @Transactional
    public void addValidReviewedAnnotation() throws Exception {
        Long userId = builder.givenSuperAdmin().getId();
        ReviewedAnnotation reviewedAnnotation = builder.givenANotPersistedReviewedAnnotation();
        ReviewedAnnotationResponse annotationData =
            new ReviewedAnnotationResponse(999L, userId, userId, reviewedAnnotation.getImage().getId(),
                reviewedAnnotation.getSlice().getId(), reviewedAnnotation.getProject().getId(),
                reviewedAnnotation.getParentIdent(), reviewedAnnotation.getParentClassName(), 0,
                reviewedAnnotation.getWktLocation(), 0.0, List.of(), LocalDateTime.now(), LocalDateTime.now(),
                Optional.empty());
        when(reviewedAnnotationHttpContract.create(eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "ADD_REVIEWED_ANNOTATION")));

        restReviewedAnnotationControllerMockMvc.perform(
                post("/api/reviewedannotation.json").contentType(MediaType.APPLICATION_JSON)
                    .content(reviewedAnnotation.toJSON())).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true)).andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.commandId").exists()).andExpect(jsonPath("$.command").exists());
    }

    @Test
    @Transactional
    public void editValidReviewedAnnotation() throws Exception {
        Long userId = builder.givenSuperAdmin().getId();
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        ReviewedAnnotationResponse annotationData =
            new ReviewedAnnotationResponse(reviewedAnnotation.getId(), userId, userId,
                reviewedAnnotation.getImage().getId(), reviewedAnnotation.getSlice().getId(),
                reviewedAnnotation.getProject().getId(), reviewedAnnotation.getParentIdent(),
                reviewedAnnotation.getParentClassName(), 0, reviewedAnnotation.getWktLocation(), 0.0, List.of(),
                LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(reviewedAnnotationHttpContract.update(eq(reviewedAnnotation.getId()), eq(userId), any())).thenReturn(
            Optional.of(
                new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "UPDATE_REVIEWED_ANNOTATION")));

        restReviewedAnnotationControllerMockMvc.perform(
                put("/api/reviewedannotation/{id}.json", reviewedAnnotation.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(reviewedAnnotation.toJSON())).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true)).andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.commandId").exists()).andExpect(jsonPath("$.command").exists());
    }

    @Test
    @Transactional
    public void deleteReviewedAnnotation() throws Exception {
        Long userId = builder.givenSuperAdmin().getId();
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        ReviewedAnnotationResponse annotationData =
            new ReviewedAnnotationResponse(reviewedAnnotation.getId(), userId, userId,
                reviewedAnnotation.getImage().getId(), reviewedAnnotation.getSlice().getId(),
                reviewedAnnotation.getProject().getId(), reviewedAnnotation.getParentIdent(),
                reviewedAnnotation.getParentClassName(), 0, reviewedAnnotation.getWktLocation(), 0.0, List.of(),
                LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(reviewedAnnotationHttpContract.delete(eq(reviewedAnnotation.getId()), eq(userId))).thenReturn(Optional.of(
            new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "DELETE_REVIEWED_ANNOTATION")));

        restReviewedAnnotationControllerMockMvc.perform(
                delete("/api/reviewedannotation/{id}.json", reviewedAnnotation.getId()).contentType(
                    MediaType.APPLICATION_JSON).content(reviewedAnnotation.toJSON())).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true)).andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.commandId").exists()).andExpect(jsonPath("$.command").exists());
    }

    @Test
    @Transactional
    public void delete_reviewed_annotation_not_exist_fails() throws Exception {
        Long userId = builder.givenSuperAdmin().getId();
        when(reviewedAnnotationHttpContract.delete(eq(0L), eq(userId))).thenReturn(Optional.empty());

        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        restReviewedAnnotationControllerMockMvc.perform(
            delete("/api/reviewedannotation/{id}.json", 0).contentType(MediaType.APPLICATION_JSON)
                .content(reviewedAnnotation.toJSON())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void startImageReview() throws Exception {
        ImageInstance imageInstance = builder.givenAnImageInstance();
        assertThat(imageInstance.getReviewStart()).isNull();
        restReviewedAnnotationControllerMockMvc.perform(
            post("/api/imageinstance/{image}/review.json", imageInstance.getId()).contentType(
                MediaType.APPLICATION_JSON).content("")).andExpect(status().isOk());
        assertThat(imageInstance.getReviewStart()).isNotNull();
    }

    @Test
    @Transactional
    public void stopImageReview() throws Exception {
        ImageInstance imageInstance = builder.givenAnImageInstance();
        imageInstance.setReviewStart(new Date());
        imageInstance.setReviewUser(imageInstance.getUser());
        assertThat(imageInstance.getReviewStop()).isNull();
        restReviewedAnnotationControllerMockMvc.perform(
            delete("/api/imageinstance/{image}/review.json", imageInstance.getId())).andExpect(status().isOk());
        assertThat(imageInstance.getReviewStop()).isNotNull();
    }

    @Test
    @Transactional
    public void cancelImageReview() throws Exception {
        ImageInstance imageInstance = builder.givenAnImageInstance();
        imageInstance.setReviewStart(new Date());
        imageInstance.setReviewUser(imageInstance.getUser());
        assertThat(imageInstance.getReviewStop()).isNull();
        restReviewedAnnotationControllerMockMvc.perform(
                delete("/api/imageinstance/{image}/review.json", imageInstance.getId()).param("cancel", "true"))
            .andExpect(status().isOk());
        assertThat(imageInstance.getReviewStart()).isNull();
        assertThat(imageInstance.getReviewStop()).isNull();
    }

    @Test
    @Transactional
    public void stopImageReviewRefuseIfImageNotStartedToReview() throws Exception {
        ImageInstance imageInstance = builder.givenAnImageInstance();
        assertThat(imageInstance.getReviewStart()).isNull();
        restReviewedAnnotationControllerMockMvc.perform(
            delete("/api/imageinstance/{image}/review.json", imageInstance.getId())).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void addAnnotationReview() throws Exception {
        UserAnnotation userAnnotation = builder.givenAUserAnnotation();
        userAnnotation.getImage().setReviewStart(new Date());
        userAnnotation.getImage().setReviewUser(userAnnotation.getUser());

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(userAnnotation);
        assertThat(reviewedAnnotationRepository.findByParentIdent(userAnnotation.getId())).isEmpty();
        em.refresh(userAnnotation);
        restReviewedAnnotationControllerMockMvc.perform(
            post("/api/annotation/{annotation}/review.json", userAnnotation.getId()).contentType(
                MediaType.APPLICATION_JSON).content("")).andExpect(status().isOk());


        assertThat(reviewedAnnotationRepository.findByParentIdent(userAnnotation.getId())).isPresent();
//        wireMockServer.verify(WireMock.putRequestedFor(urlPathMatching("/reviewed-annotations/terms/.*"))
//            .withRequestBody(WireMock.containing(annotationTerm.getTerm().getId().toString())));
    }

    @Test
    @Transactional
    public void addAnnotationReviewWithTermsChange() throws Exception {
        UserAnnotation userAnnotation = builder.givenAUserAnnotation();
        userAnnotation.getImage().setReviewStart(new Date());
        userAnnotation.getImage().setReviewUser(userAnnotation.getUser());
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(userAnnotation);
        Term anotherTerm = builder.givenATerm(userAnnotation.getProject().getOntology());

        assertThat(reviewedAnnotationRepository.findByParentIdent(userAnnotation.getId())).isEmpty();

        restReviewedAnnotationControllerMockMvc.perform(
                post("/api/annotation/{annotation}/review.json", userAnnotation.getId()).contentType(
                    MediaType.APPLICATION_JSON).content("").param("terms", anotherTerm.getId().toString()))
            .andExpect(status().isOk());


        assertThat(reviewedAnnotationRepository.findByParentIdent(userAnnotation.getId())).isPresent();
//        wireMockServer.verify(WireMock.putRequestedFor(urlPathMatching("/reviewed-annotations/terms/.*"))
//            .withRequestBody(WireMock.containing(anotherTerm.getId().toString())));
    }

    @Test
    @Transactional
    public void removeAnnotationReview() throws Exception {
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();

        assertThat(reviewedAnnotationRepository.findByParentIdent(reviewedAnnotation.getParentIdent())).isPresent();

        restReviewedAnnotationControllerMockMvc.perform(
            delete("/api/annotation/{annotation}/review.json", reviewedAnnotation.getParentIdent()).contentType(
                MediaType.APPLICATION_JSON).content("")).andExpect(status().isOk());

        assertThat(reviewedAnnotationRepository.findByParentIdent(reviewedAnnotation.getParentIdent())).isEmpty();
    }

    @Test
    @Transactional
    public void reviewFullLayer() throws Exception {
        UserAnnotation userAnnotation = builder.givenAUserAnnotation();
        userAnnotation.getImage().setReviewStart(new Date());
        userAnnotation.getImage().setReviewUser(userAnnotation.getUser());
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(userAnnotation);
        em.refresh(userAnnotation);

        assertThat(reviewedAnnotationRepository.findByParentIdent(userAnnotation.getId())).isEmpty();

        restReviewedAnnotationControllerMockMvc.perform(
                post("/api/imageinstance/{image}/annotation/review.json", userAnnotation.getImage().getId()).contentType(
                    MediaType.APPLICATION_JSON).content("").param("users", userAnnotation.getUser().getId().toString()))
            .andExpect(status().isOk());


        assertThat(reviewedAnnotationRepository.findByParentIdent(userAnnotation.getId())).isPresent();
    }

    @Test
    @Transactional
    public void unreviewFullLayer() throws Exception {
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        reviewedAnnotation.getImage().setReviewStart(new Date());
        reviewedAnnotation.getImage().setReviewUser(reviewedAnnotation.getUser());

        assertThat(reviewedAnnotationRepository.findByParentIdent(reviewedAnnotation.getParentIdent())).isPresent();

        restReviewedAnnotationControllerMockMvc.perform(delete("/api/imageinstance/{image}/annotation/review.json",
            reviewedAnnotation.getImage().getId()).contentType(MediaType.APPLICATION_JSON).content("")
            .param("users", reviewedAnnotation.getUser().getId().toString())).andExpect(status().isOk());


        assertThat(reviewedAnnotationRepository.findByParentIdent(reviewedAnnotation.getParentIdent())).isEmpty();
    }

    @Disabled("Randomly fail with ProxyExchange, need to find a solution")
    @Test
    @jakarta.transaction.Transactional
    public void getReviewedAnnotationCrop() throws Exception {
        ReviewedAnnotation annotation = givenAReviewedAnnotationWithValidImageServer(builder);

        byte[] mockResponse = UUID.randomUUID().toString()
            .getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url =
            "/image/" + URLEncoder.encode("1636379100999/CMU-2/CMU-2.mrxs", StandardCharsets.UTF_8).replace("%2F", "/")
                + "/annotation/crop";
        String body =
            "{\"length\":512,\"z_slices\":0,\"annotations\":[{\"geometry\":\"POLYGON ((1 1, 50 10, 50 50, 10 50, 1 1))\"}],\"timepoints\":0,\"background_transparency\":0}";
        System.out.println(url);
        System.out.println(body);
//        wireMockServer.stubFor(WireMock.post(urlEqualTo(IMS_API_BASE_PATH + url)).withRequestBody(WireMock.equalTo(
//                    body
//                ))
//                .willReturn(
//                    aResponse().withBody(mockResponse)
//                )
//        );


        MvcResult mvcResult = restReviewedAnnotationControllerMockMvc.perform(
                get("/api/reviewedannotation/{id}/crop.png?maxSize=512", annotation.getId())).andExpect(status().isOk())
            .andReturn();
//        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
//        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    @Disabled("Randomly fail with ProxyExchange, need to find a solution")
    @Test
    @jakarta.transaction.Transactional
    public void getReviewedAnnotationCropMask() throws Exception {
        ReviewedAnnotation annotation = givenAReviewedAnnotationWithValidImageServer(builder);

        byte[] mockResponse = UUID.randomUUID().toString()
            .getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url =
            "/image/" + URLEncoder.encode("1636379100999/CMU-2/CMU-2.mrxs", StandardCharsets.UTF_8).replace("%2F", "/")
                + "/annotation/mask";
        String body =
            "{\"level\":0,\"z_slices\":0,\"annotations\":[{\"geometry\":\"POLYGON ((1 1, 50 10, 50 50, 10 50, 1 1))\",\"fill_color\":\"#fff\"}],\"timepoints\":0}";
        System.out.println(url);
        System.out.println(body);
//        wireMockServer.stubFor(WireMock.post(urlEqualTo(IMS_API_BASE_PATH + url)).withRequestBody(WireMock.equalTo(
//                    body
//                ))
//                .willReturn(
//                    aResponse().withBody(mockResponse)
//                )
//        );


        MvcResult mvcResult = restReviewedAnnotationControllerMockMvc.perform(
            get("/api/reviewedannotation/{id}/mask.png", annotation.getId())).andExpect(status().isOk()).andReturn();
//        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
//        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    @Disabled("Randomly fail with ProxyExchange, need to find a solution")
    @Test
    @jakarta.transaction.Transactional
    public void getReviewedAnnotationAlphaMask() throws Exception {
        ReviewedAnnotation annotation = givenAReviewedAnnotationWithValidImageServer(builder);

        byte[] mockResponse = UUID.randomUUID().toString()
            .getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content


        String url =
            "/image/" + URLEncoder.encode(annotation.getImage().getBaseImage().getPath(), StandardCharsets.UTF_8)
                .replace("%2F", "/") + "/annotation/crop";
        String body =
            "{\"level\":0,\"z_slices\":0,\"annotations\":[{\"geometry\":\"POLYGON ((1 1, 50 10, 50 50, 10 50, 1 1))\"}],\"timepoints\":0,\"background_transparency\":100}";
        System.out.println(url);
        System.out.println(body);
//        wireMockServer.stubFor(WireMock.post(urlEqualTo(IMS_API_BASE_PATH + url)).withRequestBody(WireMock.equalTo(
//                    body
//                ))
//                .willReturn(
//                    aResponse().withBody(mockResponse)
//                )
//        );

        MvcResult mvcResult = restReviewedAnnotationControllerMockMvc.perform(
                get("/api/reviewedannotation/{id}/alphamask.png", annotation.getId())).andExpect(status().isOk())
            .andReturn();
//        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
//        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }
}
