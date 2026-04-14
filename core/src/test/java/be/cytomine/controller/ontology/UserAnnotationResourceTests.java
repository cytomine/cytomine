package be.cytomine.controller.ontology;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.TestUtils;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.UserAnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserAnnotationResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.properties.ApplicationProperties;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.SharedAnnotation;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.utils.JsonObject;

import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class UserAnnotationResourceTests {

    private static WireMockServer wireMockServer = new WireMockServer(8888);
    @Autowired
    private EntityManager em;
    @Autowired
    private BasicInstanceBuilder builder;
    @Autowired
    private MockMvc restUserAnnotationControllerMockMvc;
    @Autowired
    private MockMvc restAnnotationDomainControllerMockMvc;
    @Autowired
    private ApplicationProperties applicationProperties;
    @MockitoBean
    private UserAnnotationHttpContract userAnnotationHttpContract;
    private Project project;
    private ImageInstance image;
    private SliceInstance slice;
    private Term term;
    private User me;
    private UserAnnotation userAnnotation;

    private static void setupStub() {
        /* Simulate call to PIMS */
        wireMockServer.stubFor(WireMock.post(urlPathMatching(IMS_API_BASE_PATH + "/image/.*/annotation/drawing"))
            .withRequestBody(WireMock.matching(".*")).willReturn(
                aResponse().withStatus(HttpStatus.OK.value()).withBody(UUID.randomUUID().toString().getBytes())));

        /* Simulate call to CBIR server */
        wireMockServer.stubFor(
            WireMock.post(urlPathEqualTo(CBIR_API_BASE_PATH + "/images")).withQueryParam("storage", matching(".*"))
                .withQueryParam("index", equalTo("annotation"))
                .willReturn(aResponse().withBody(UUID.randomUUID().toString())));

        wireMockServer.stubFor(WireMock.delete(urlPathMatching(CBIR_API_BASE_PATH + "/images/.*"))
            .withQueryParam("storage", matching(".*")).withQueryParam("index", equalTo("annotation"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString())));
    }

    @BeforeAll
    public static void beforeAll() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();

        setupStub();
    }

    @AfterAll
    public static void afterAll() {
        try {
            wireMockServer.stop();
        } catch (Exception e) {
        }
    }

    public static UserAnnotation given_a_user_annotation_with_valid_image_server(BasicInstanceBuilder builder)
        throws ParseException {
        AbstractImage image = builder.given_an_abstract_image();
        image.setWidth(109240);
        image.setHeight(220696);
        image.getUploadedFile().setFilename("1636379100999/CMU-2/CMU-2.mrxs");
        image.getUploadedFile().setContentType("MRXS");
        ImageInstance imageInstance = builder.given_an_image_instance(image, builder.given_a_project());
        imageInstance.setInstanceFilename("CMU-2");
        AbstractSlice slice = builder.given_an_abstract_slice(image, 0, 0, 0);
        slice.setUploadedFile(image.getUploadedFile());
        SliceInstance sliceInstance = builder.given_a_slice_instance(imageInstance, slice);
        UserAnnotation userAnnotation =
            builder.given_a_user_annotation(sliceInstance, "POLYGON((1 1,50 10,50 50,10 50,1 1))",
                builder.given_superadmin(), null);
        return userAnnotation;
    }

    @Test
    @Transactional
    public void get_a_user_annotation() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        when(userAnnotationHttpContract.findById(eq(userAnnotation.getId()), eq(userId))).thenReturn(Optional.of(
            new UserAnnotationResponse(userAnnotation.getId(), userId, userAnnotation.getImage().getId(),
                userAnnotation.getSlice().getId(), userAnnotation.getProject().getId(), userAnnotation.getWktLocation(),
                0.0, 0, LocalDateTime.now(), LocalDateTime.now(), Optional.empty())));

        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}.json", userAnnotation.getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(userAnnotation.getId().intValue()))
            .andExpect(jsonPath("$.imageId").value(userAnnotation.getImage().getId().intValue()))
            .andExpect(jsonPath("$.projectId").value(userAnnotation.getProject().getId().intValue()))
            .andExpect(jsonPath("$.userId").value(userId.intValue()))
            .andExpect(jsonPath("$.wktLocation").value(userAnnotation.getWktLocation()));
    }

    @Test
    @Transactional
    public void get_a_user_annotation_not_exists() throws Exception {
        Long userId = builder.given_superadmin().getId();
        when(userAnnotationHttpContract.findById(eq(0L), eq(userId))).thenReturn(Optional.empty());
        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}.json", 0))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void list_annotations_light() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation.json", userAnnotation.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id=='" + userAnnotation.getId() + "')]").exists());
    }

    @Test
    @Transactional
    public void count_annotations_by_user() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        User newUser = builder.given_a_user();
        when(userAnnotationHttpContract.countByUser(eq(userAnnotation.getUser().getId()), eq(userId))).thenReturn(3L);
        when(userAnnotationHttpContract.countByUser(eq(newUser.getId()), eq(userId))).thenReturn(0L);

        restUserAnnotationControllerMockMvc.perform(
                get("/api/user/{idUser}/userannotation/count.json", userAnnotation.getUser().getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(greaterThan(0)));

        restUserAnnotationControllerMockMvc.perform(
                get("/api/user/{idUser}/userannotation/count.json", newUser.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @Transactional
    public void count_annotations_by_project() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        Project projectWithoutAnnotation = builder.given_a_project();
        when(userAnnotationHttpContract.countByUserAndProject(eq(userAnnotation.getUser().getId()),
            eq(userAnnotation.getProject().getId()), anyLong())).thenReturn(3L);
        when(userAnnotationHttpContract.countByUserAndProject(eq(userAnnotation.getUser().getId()),
            eq(projectWithoutAnnotation.getId()), anyLong())).thenReturn(0L);

        restUserAnnotationControllerMockMvc.perform(
                get("/api/user/{idUser}/userannotation/count.json", userAnnotation.getUser().getId()).param("project",
                    userAnnotation.getProject().getId().toString())).andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(greaterThan(0)));

        restUserAnnotationControllerMockMvc.perform(
                get("/api/user/{idUser}/userannotation/count.json", userAnnotation.getUser().getId()).param("project",
                    projectWithoutAnnotation.getId().toString())).andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @Transactional
    public void count_annotations_by_project_with_dates() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation oldUserAnnotation = builder.given_a_user_annotation();
        builder.persistAndReturn(builder.given_a_not_persisted_user_annotation(oldUserAnnotation.getProject()));

        // Simulate date-filtered counts returned by the repository service
        when(userAnnotationHttpContract.countByProject(eq(oldUserAnnotation.getProject().getId()), eq(userId), any(),
            any())).thenReturn(2L, 2L, 2L, 1L, 1L, 0L);

        restUserAnnotationControllerMockMvc.perform(
                get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId()).param(
                    "startDate", "1000").param("endDate", "2000")).andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2));

        restUserAnnotationControllerMockMvc.perform(
            get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId()).param(
                "startDate", "500")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2));

        restUserAnnotationControllerMockMvc.perform(
            get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId()).param(
                "endDate", "3000")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2));

        restUserAnnotationControllerMockMvc.perform(
            get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId()).param(
                "startDate", "1500")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));

        restUserAnnotationControllerMockMvc.perform(
            get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId()).param(
                "endDate", "1500")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));

        restUserAnnotationControllerMockMvc.perform(
            get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId()).param(
                "endDate", "100")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @Transactional
    public void download_user_annotation_csv_document() throws Exception {
        buildDownloadContext();
        MvcResult mvcResult = performDownload("csv");
        checkResult(";", mvcResult);
    }

    @Test
    @Transactional
    public void download_user_annotation_xls_document() throws Exception {
        buildDownloadContext();
        MvcResult mvcResult = performDownload("xls");
        checkXLSResult(mvcResult);
    }

    @Test
    @Transactional
    public void download_user_annotation_pdf_document() throws Exception {
        buildDownloadContext();
        restAnnotationDomainControllerMockMvc.perform(
                get("/api/project/{project}/userannotation/download", this.project.getId()).param("format", "pdf")
                    .param("users", this.me.getId().toString()).param("reviewed", "false")
                    .param("terms", this.term.getId().toString()).param("images", this.image.getId().toString()))
            .andExpect(status().isOk()).andExpect(header().string("Content-Type", "application/pdf")).andReturn();
    }

    private void buildDownloadContext() throws ParseException {
        this.project = builder.given_a_project();
        this.image = builder.given_an_image_instance(this.project);
        this.slice = builder.given_a_slice_instance(this.image, 0, 0, 0);
        this.term = builder.given_a_term(this.project.getOntology());
        this.me = builder.given_superadmin();
        this.userAnnotation =
            builder.given_a_user_annotation(this.slice, "POLYGON((1 1,5 1,5 5,1 5,1 1))", this.me, this.term);
    }

    private void checkResult(String delimiter, MvcResult result) throws UnsupportedEncodingException {
        TestUtils.checkSpreadsheetAnnotationResult(delimiter, result, this.userAnnotation, this.project, this.image,
            this.me, this.term, "userannotation", applicationProperties.getServerURL());
    }

    private void checkXLSResult(MvcResult result) throws IOException {
        TestUtils.checkSpreadsheetXLSAnnotationResult(result, this.userAnnotation, this.project, this.image, this.me,
            this.term, "userannotation", applicationProperties.getServerURL());
    }

    private MvcResult performDownload(String format) throws Exception {
        return restAnnotationDomainControllerMockMvc.perform(
                get("/api/project/{project}/userannotation/download", this.project.getId()).param("format", format)
                    .param("users", this.me.getId().toString()).param("reviewed", "false")
                    .param("terms", this.term.getId().toString()).param("images", this.image.getId().toString()))
            .andExpect(status().isOk()).andReturn();
    }

    @Test
    @Transactional
    public void add_valid_user_annotation() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        UserAnnotationResponse annotationData =
            new UserAnnotationResponse(999L, userId, userAnnotation.getImage().getId(),
                userAnnotation.getSlice().getId(), userAnnotation.getProject().getId(), userAnnotation.getWktLocation(),
                0.0, 0, LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(userAnnotationHttpContract.create(eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "ADD_USER_ANNOTATION")));

        restUserAnnotationControllerMockMvc.perform(
                post("/api/userannotation.json").contentType(MediaType.APPLICATION_JSON).content(userAnnotation.toJSON()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.data").exists()).andExpect(jsonPath("$.commandId").exists())
            .andExpect(jsonPath("$.command").exists());
    }

    @Test
    @Transactional
    public void add_user_annotation_with_not_valid_location() throws Exception {
        // Location validation is now handled by the repository service
        Long userId = builder.given_superadmin().getId();
        when(userAnnotationHttpContract.create(eq(userId), any())).thenThrow(
            new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                "Location too small"));

        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        JsonObject jsonObject = userAnnotation.toJsonObject();
        jsonObject.put("location",
            "POLYGON ((225.73582220103702 306.89723126347087, 225.73582220103702 307.93556995227914, 226.08028300710947 307.93556995227914, 226.08028300710947 306.89723126347087, 225.73582220103702 306.89723126347087))");
        restUserAnnotationControllerMockMvc.perform(
                post("/api/userannotation.json").contentType(MediaType.APPLICATION_JSON).content(jsonObject.toJsonString()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void add_valid_user_annotation_without_project() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        UserAnnotationResponse annotationData =
            new UserAnnotationResponse(999L, userId, userAnnotation.getImage().getId(),
                userAnnotation.getSlice().getId(), userAnnotation.getProject().getId(), userAnnotation.getWktLocation(),
                0.0, 0, LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(userAnnotationHttpContract.create(eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "ADD_USER_ANNOTATION")));

        JsonObject jsonObject = userAnnotation.toJsonObject();
        jsonObject.remove("project");

        restUserAnnotationControllerMockMvc.perform(
                post("/api/userannotation.json").contentType(MediaType.APPLICATION_JSON).content(jsonObject.toJsonString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.projectId").value(userAnnotation.getProject().getId()));
    }

    @Test
    @Transactional
    public void add_valid_user_annotation_with_terms() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        UserAnnotationResponse annotationData =
            new UserAnnotationResponse(999L, userId, userAnnotation.getImage().getId(),
                userAnnotation.getSlice().getId(), userAnnotation.getProject().getId(), userAnnotation.getWktLocation(),
                0.0, 0, LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(userAnnotationHttpContract.create(eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "ADD_USER_ANNOTATION")));

        Term term1 = builder.given_a_term(userAnnotation.getProject().getOntology());
        Term term2 = builder.given_a_term(userAnnotation.getProject().getOntology());
        JsonObject jsonObject = userAnnotation.toJsonObject();
        jsonObject.put("term", Arrays.asList(term1.getId(), term2.getId()));

        restUserAnnotationControllerMockMvc.perform(
                post("/api/userannotation.json").contentType(MediaType.APPLICATION_JSON).content(jsonObject.toJsonString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Transactional
    public void edit_valid_user_annotation() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        UserAnnotationResponse annotationData =
            new UserAnnotationResponse(userAnnotation.getId(), userId, userAnnotation.getImage().getId(),
                userAnnotation.getSlice().getId(), userAnnotation.getProject().getId(), userAnnotation.getWktLocation(),
                0.0, 0, LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(userAnnotationHttpContract.update(eq(userAnnotation.getId()), eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "UPDATE_USER_ANNOTATION")));

        restUserAnnotationControllerMockMvc.perform(
                put("/api/userannotation/{id}.json", userAnnotation.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(userAnnotation.toJSON())).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true)).andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.commandId").exists()).andExpect(jsonPath("$.command").exists());
    }

    @Test
    @Transactional
    public void delete_user_annotation() throws Exception {
        Long userId = builder.given_superadmin().getId();
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        UserAnnotationResponse annotationData =
            new UserAnnotationResponse(userAnnotation.getId(), userId, userAnnotation.getImage().getId(),
                userAnnotation.getSlice().getId(), userAnnotation.getProject().getId(), userAnnotation.getWktLocation(),
                0.0, 0, LocalDateTime.now(), LocalDateTime.now(), Optional.empty());
        when(userAnnotationHttpContract.delete(eq(userAnnotation.getId()), eq(userId))).thenReturn(
            Optional.of(new HttpCommandResponse(true, annotationData, UUID.randomUUID(), "DELETE_USER_ANNOTATION")));

        restUserAnnotationControllerMockMvc.perform(
                delete("/api/userannotation/{id}.json", userAnnotation.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(userAnnotation.toJSON())).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true)).andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.commandId").exists()).andExpect(jsonPath("$.command").exists());
    }

    @Test
    @Transactional
    public void delete_user_annotation_not_exist_fails() throws Exception {
        Long userId = builder.given_superadmin().getId();
        when(userAnnotationHttpContract.delete(eq(0L), eq(userId))).thenReturn(Optional.empty());

        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(
            delete("/api/userannotation/{id}.json", 0).contentType(MediaType.APPLICATION_JSON)
                .content(userAnnotation.toJSON())).andExpect(status().isNotFound());
    }

    @Disabled("Randomly fail with ProxyExchange, need to find a solution")
    @Test
    @jakarta.transaction.Transactional
    public void get_user_annotation_crop() throws Exception {
        UserAnnotation annotation = given_a_user_annotation_with_valid_image_server(builder);

        configureFor("localhost", 8888);

        byte[] mockResponse = UUID.randomUUID().toString()
            .getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url =
            "/image/" + URLEncoder.encode("1636379100999/CMU-2/CMU-2.mrxs", StandardCharsets.UTF_8).replace("%2F", "/")
                + "/annotation/crop";
        String body =
            "{\"length\":512,\"z_slices\":0,\"annotations\":[{\"geometry\":\"POLYGON ((1 1, 50 10, 50 50, 10 50, 1 1))\"}],\"timepoints\":0,\"background_transparency\":0}";
        System.out.println(url);
        System.out.println(body);
        stubFor(post(urlEqualTo(IMS_API_BASE_PATH + url)).withRequestBody(equalTo(body))
            .willReturn(aResponse().withBody(mockResponse)));

        MvcResult mvcResult = restUserAnnotationControllerMockMvc.perform(
                get("/api/userannotation/{id}/crop.png?maxSize=512", annotation.getId())).andExpect(status().isOk())
            .andReturn();
        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    @Disabled("Randomly fail with ProxyExchange, need to find a solution")
    @Test
    @jakarta.transaction.Transactional
    public void get_user_annotation_crop_mask() throws Exception {
        UserAnnotation annotation = given_a_user_annotation_with_valid_image_server(builder);

        configureFor("localhost", 8888);

        byte[] mockResponse = UUID.randomUUID().toString()
            .getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url =
            "/image/" + URLEncoder.encode("1636379100999/CMU-2/CMU-2.mrxs", StandardCharsets.UTF_8).replace("%2F", "/")
                + "/annotation/mask";
        String body =
            "{\"length\":512,\"z_slices\":0,\"annotations\":[{\"geometry\":\"POLYGON ((1 1, 50 10, 50 50, 10 50, 1 1))\",\"fill_color\":\"#fff\"}],\"timepoints\":0}";
        System.out.println(url);
        System.out.println(body);
        stubFor(post(urlEqualTo(IMS_API_BASE_PATH + url)).withRequestBody(equalTo(body))
            .willReturn(aResponse().withBody(mockResponse)));

        MvcResult mvcResult = restUserAnnotationControllerMockMvc.perform(
                get("/api/userannotation/{id}/mask.png?maxSize=512", annotation.getId())).andExpect(status().isOk())
            .andReturn();
        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    @Disabled("Randomly fail with ProxyExchange, need to find a solution")
    @Test
    @jakarta.transaction.Transactional
    public void get_user_annotation_alpha_mask() throws Exception {
        UserAnnotation annotation = given_a_user_annotation_with_valid_image_server(builder);

        configureFor("localhost", 8888);

        byte[] mockResponse = UUID.randomUUID().toString()
            .getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url =
            "/image/" + URLEncoder.encode(annotation.getImage().getBaseImage().getPath(), StandardCharsets.UTF_8)
                .replace("%2F", "/") + "/annotation/crop";
        String body =
            "{\"level\":0,\"z_slices\":0,\"annotations\":[{\"geometry\":\"POLYGON ((1 1, 50 10, 50 50, 10 50, 1 1))\"}],\"timepoints\":0,\"background_transparency\":100}";
        System.out.println(url);
        System.out.println(body);
        stubFor(WireMock.post(urlEqualTo(IMS_API_BASE_PATH + url)).withRequestBody(equalTo(body))
            .willReturn(aResponse().withBody(mockResponse)));

        MvcResult mvcResult = restUserAnnotationControllerMockMvc.perform(
            get("/api/userannotation/{id}/alphamask.png", annotation.getId())).andExpect(status().isOk()).andReturn();
        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    @Test
    @Transactional
    public void create_comments_for_annotation() throws Exception {
        SharedAnnotation annotation = builder.given_a_shared_annotation();

        JsonObject jsonObject = annotation.toJsonObject();
        jsonObject.put("subject", "subject for test mail");
        jsonObject.put("message", "message for test mail");
        jsonObject.put("users", List.of(builder.given_superadmin().getId()));

        restUserAnnotationControllerMockMvc.perform(
                post("/api/userannotation/{id}/comment.json", annotation.getAnnotationIdent()).contentType(
                    MediaType.APPLICATION_JSON).content(jsonObject.toJsonString())).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true)).andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddSharedAnnotationCommand"))
            .andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.sharedannotation.id").exists());
    }

    @Test
    @Transactional
    public void get_comment_for_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        SharedAnnotation comment = builder.given_a_shared_annotation(userAnnotation);

        restUserAnnotationControllerMockMvc.perform(
                get("/api/userannotation/{annotation}/comment/{id}.json", userAnnotation.getId(), comment.getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(comment.getId()));
    }

    @Test
    @Transactional
    public void list_comment_for_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        SharedAnnotation comment = builder.given_a_shared_annotation(userAnnotation);

        restUserAnnotationControllerMockMvc.perform(
                get("/api/userannotation/{annotation}/comment.json", userAnnotation.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(equalTo(1)));
    }

    @Test
    @Transactional
    public void list_comment_for_annotation_with_pagination() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        builder.given_a_shared_annotation(userAnnotation);
        builder.given_a_shared_annotation(userAnnotation);
        builder.given_a_shared_annotation(userAnnotation);

        restUserAnnotationControllerMockMvc.perform(
                get("/api/userannotation/{annotation}/comment.json", userAnnotation.getId()).param("max", "3")
                    .param("offset", "0")).andExpect(status().isOk()).andExpect(jsonPath("$.size").value(equalTo(3)))
            .andExpect(jsonPath("$.collection", hasSize(3)));

        restUserAnnotationControllerMockMvc.perform(
                get("/api/userannotation/{annotation}/comment.json", userAnnotation.getId()).param("max", "1")
                    .param("offset", "2")).andExpect(status().isOk()).andExpect(jsonPath("$.size").value(equalTo(3)))
            .andExpect(jsonPath("$.collection", hasSize(1)));
    }
}
