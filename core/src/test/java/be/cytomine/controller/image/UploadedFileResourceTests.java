package be.cytomine.controller.image;

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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.repository.image.UploadedFileRepository;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class UploadedFileResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restUploadedFileControllerMockMvc;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Test
    @Transactional
    public void listUploaded() throws Exception {
        UploadedFile uploadedFile = builder.givenAUploadedFile();

        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + uploadedFile.getId() + ")]").exists());
    }

    @Test
    @Transactional
    public void listUploadedHirerachicalTree() throws Exception {
        UploadedFile uploadedFile = builder.givenAUploadedFile();

        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json").param(
                "root",
                uploadedFile.getId().toString()
            ))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void listUploadedWithSearch() throws Exception {
        UploadedFile uploadedFile = builder.givenAUploadedFile();
        uploadedFile.setOriginalFilename("abracadabra");

        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("originalFilename[equals]", "abracadabra"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + uploadedFile.getId() + ")]").exists());

        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("originalFilename[equals]", "notFound"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + uploadedFile.getId() + ")]").doesNotExist());
    }


    @Test
    @Transactional
    public void listUploadedFileWithPagination() throws Exception {

        UploadedFile image1 = builder.givenAUploadedFile();
        UploadedFile image2 = builder.givenAUploadedFile();
        UploadedFile image3 = builder.givenAUploadedFile();

        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("offset", "0")
                .param("max", "0")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath(
                "$.collection",
                hasSize(greaterThanOrEqualTo(3))
            )) // default sorting must be created desc
            .andExpect(jsonPath("$.collection[0].id").value(image3.getId()))
            .andExpect(jsonPath("$.collection[1].id").value(image2.getId()))
            .andExpect(jsonPath("$.collection[2].id").value(image1.getId()))
            .andExpect(jsonPath("$.offset").value(0))
            .andExpect(jsonPath("$.perPage", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.size", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.totalPages").value(1));


        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("offset", "0")
                .param("max", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1)))) // default sorting must be created desc
            .andExpect(jsonPath("$.collection[0].id").value(image3.getId()))
            .andExpect(jsonPath("$.offset").value(0))
            .andExpect(jsonPath("$.perPage").value(1))
            .andExpect(jsonPath("$.size", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(3)));


        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("offset", "1")
                .param("max", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1)))) // default sorting must be created desc
            .andExpect(jsonPath("$.collection[0].id").value(image2.getId()))
            .andExpect(jsonPath("$.offset").value(1))
            .andExpect(jsonPath("$.perPage").value(1))
            .andExpect(jsonPath("$.size", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(3)));

        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("offset", "1")
                .param("max", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(
                "$.collection",
                hasSize(greaterThanOrEqualTo(2))
            )) // default sorting must be created desc
            .andExpect(jsonPath("$.collection[0].id").value(image2.getId()))
            .andExpect(jsonPath("$.collection[1].id").value(image1.getId()))
            .andExpect(jsonPath("$.offset").value(1))
            .andExpect(jsonPath("$.size", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.totalPages").value(1));


        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("offset", "0")
                .param("max", "500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(
                "$.collection",
                hasSize(greaterThanOrEqualTo(3))
            )) // default sorting must be created desc
            .andExpect(jsonPath("$.collection[0].id").value(image3.getId()))
            .andExpect(jsonPath("$.collection[1].id").value(image2.getId()))
            .andExpect(jsonPath("$.collection[2].id").value(image1.getId()))
            .andExpect(jsonPath("$.offset").value(0))
            .andExpect(jsonPath("$.totalPages").value(1));


        restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("offset", "500")
                .param("max", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(0)))) // default sorting must be created desc
            .andExpect(jsonPath("$.offset").value(500))
            .andExpect(jsonPath("$.perPage").value(0))
            .andExpect(jsonPath("$.size").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    List<Long> retrieveIds(MvcResult mvcResult) throws UnsupportedEncodingException {
        Map<String, Object> result = JsonObject.toMap(mvcResult.getResponse().getContentAsString());
        List<Map<String, Object>> collection = (List<Map<String, Object>>) result.get("collection");
        return collection.stream().map(x -> Long.valueOf(x.get("id").toString())).collect(Collectors.toList());
    }

    @Test
    @Transactional
    void sortUploadedFile() throws Exception {
        UploadedFile uploadedFile = builder.givenAUploadedFile();
        uploadedFile.setSize(1L);
        UploadedFile uploadedFileChild1 = builder.givenAUploadedFile();
        uploadedFileChild1.setParent(uploadedFile);
        UploadedFile uploadedfileChild2 = builder.givenAUploadedFile();
        uploadedfileChild2.setParent(uploadedFile);
        uploadedfileChild2.setSize(uploadedFile.getSize() + 200);
        uploadedfileChild2.setOriginalFilename(uploadedFile.getOriginalFilename() + "s");
        uploadedfileChild2.setStatus(9);
        UploadedFile uploadedFile2 = builder.givenAUploadedFile();
        uploadedFile2.setSize(100000L);

        MvcResult mvcResult;
        List<Long> ids;
        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "created")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        Long first = ids.get(0);
        Long last = ids.get(ids.size() - 1);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "created")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        assertThat(ids.get(0)).isEqualTo(last);
        assertThat(ids.get(ids.size() - 1)).isEqualTo(first);


        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "originalFilename")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        first = ids.get(0);
        last = ids.get(ids.size() - 1);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "originalFilename")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        assertThat(ids.get(0)).isEqualTo(last);
        assertThat(ids.get(ids.size() - 1)).isEqualTo(first);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "size")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        first = ids.get(0);
        last = ids.get(ids.size() - 1);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "size")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        assertThat(ids.get(0)).isEqualTo(last);
        assertThat(ids.get(ids.size() - 1)).isEqualTo(first);


        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "contentType")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        first = ids.get(0);
        last = ids.get(ids.size() - 1);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "contentType")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        assertThat(ids.get(0)).isEqualTo(last);
        assertThat(ids.get(ids.size() - 1)).isEqualTo(first);


        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "globalSize")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        List<UploadedFile> uploadedFiles = ids.stream()
            .map(x -> uploadedFileRepository.getById(x))
            .collect(Collectors.toList());
        assertThat(uploadedFiles.get(0).getSize()).isLessThan(uploadedFiles.get(uploadedFiles.size() - 1).getSize());

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "globalSize")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        uploadedFiles = ids.stream().map(x -> uploadedFileRepository.getById(x)).collect(Collectors.toList());
        assertThat(uploadedFiles.get(0).getSize()).isGreaterThan(uploadedFiles.get(uploadedFiles.size() - 1).getSize());

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "status")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        first = ids.get(0);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "status")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        assertThat(ids.get(0)).isNotEqualTo(last);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "parentFilename")
                .param("order", "asc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        first = ids.get(0);

        mvcResult = restUploadedFileControllerMockMvc.perform(get("/api/uploadedfile.json")
                .param("onlyRootsWithDetails", "true")
                .param("sort", "parentFilename")
                .param("order", "desc"))
            .andExpect(status().isOk()).andReturn();
        ids = retrieveIds(mvcResult);
        assertThat(ids.get(0)).isNotEqualTo(first);
    }

}
