package be.cytomine.domain;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.PostGisTestConfiguration;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.*;
import be.cytomine.dto.image.AreaDTO;
import be.cytomine.repositorynosql.social.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Import;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class MongoDBDomainTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PersistentProjectConnectionRepository persistentProjectConnectionRepository;

    @Autowired
    PersistentConnectionRepository persistentConnectionRepository;

    @Autowired
    PersistentImageConsultationRepository persistentImageConsultationRepository;

    @Autowired
    PersistentUserPositionRepository persistentUserPositionRepository;

    @Autowired
    LastConnectionRepository lastConnectionRepository;

    @Autowired
    LastUserPositionRepository lastUserPositionRepository;

    @Autowired
    MongoClient mongoClient;

    SimpleDateFormat mongoDBFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;

    @BeforeEach
    public void cleanDB() {
        persistentProjectConnectionRepository.deleteAll();
        persistentConnectionRepository.deleteAll();
        persistentImageConsultationRepository.deleteAll();
        persistentUserPositionRepository.deleteAll();
        lastConnectionRepository.deleteAll();
        lastUserPositionRepository.deleteAll();
    }

    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void last_connection_indexes() throws ParseException, JsonProcessingException {
        ListIndexesIterable<Document> indexes = retrieveIndex("lastConnection");
        Document indexId = null;
        Document indexUserDate = null;
        for (Document index : indexes) {
            if (index.get("name").equals("_id_")) {
                indexId = index;
            }
            if (index.get("name").equals("date_2")) {
                indexUserDate = index;
            }
        }
        assertThat(indexes).hasSize(2);
        assertThat(indexId).isNotNull();
        assertThat(((Document)indexId.get("key")).get("_id")).isEqualTo(1);
        assertThat(indexUserDate).isNotNull();
        assertThat(((Document)indexUserDate.get("key")).get("date")).isEqualTo(1); // 1 or 2
        assertThat(indexUserDate.get("expireAfterSeconds")).isEqualTo(300L);
    }
    
    @Test
    void last_connection_domain() throws ParseException {
        Long expectedId = 60657L;
        Long expectedUserId = 58L;
        Date expectedDate = mongoDBFormat.parse("2022-02-02T07:30:23.384Z");

        LastConnection lastConnection = new LastConnection();
        lastConnection.setId(expectedId);
        lastConnection.setProject(null);
        lastConnection.setUser(expectedUserId);
        lastConnection.setCreated(expectedDate);
        lastConnection.setDate(expectedDate);
        lastConnection = lastConnectionRepository.insert(lastConnection);

        Document document = retrieveDocument("lastConnection", lastConnection.getId());

        assertThat(document.getLong("_id")).isEqualTo(expectedId);
        assertThat(document.getLong("user")).isEqualTo(expectedUserId);
        assertThat(document.getInteger("version")).isEqualTo(0);
        assertThat(document.get("project")).isNull();
        assertThat(document.getDate("date")).isEqualTo(expectedDate);
        assertThat(document.getDate("created")).isEqualTo(expectedDate);
    }

    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void last_user_position_index() throws ParseException, JsonProcessingException {
        ListIndexesIterable<Document> indexes = retrieveIndex("lastUserPosition");
        Document indexId = null;
        Document indexUserImageSliceCreated = null;
        Document locationImageSlice = null;
        Document created = null;
        Document image = null;
        for (Document index : indexes) {
            if (index.get("name").equals("_id_")) {
                indexId = index;
            }
            if (index.get("name").equals("user_1_image_1_slice_1_created_-1")) {
                indexUserImageSliceCreated = index;
            }

            if (index.get("name").equals("location_2d_image_1_slice_1")) {
                locationImageSlice = index;
            }

            if (index.get("name").equals("created_1")) {
                created = index;
            }

            if (index.get("name").equals("image_1")) {
                image = index;
            }
        }
        assertThat(indexes).hasSize(5);

        assertThat(indexId).isNotNull();
        assertThat(((Document)indexId.get("key")).get("_id")).isEqualTo(1);

        assertThat(indexUserImageSliceCreated).isNotNull();
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("user")).isEqualTo(1);
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("image")).isEqualTo(1);
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("slice")).isEqualTo(1);
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("created")).isEqualTo(-1);

        assertThat(locationImageSlice).isNotNull();
        assertThat(((Document)locationImageSlice.get("key")).get("location")).isEqualTo("2d");
        assertThat(((Document)locationImageSlice.get("key")).get("image")).isEqualTo(1);
        assertThat(((Document)locationImageSlice.get("key")).get("slice")).isEqualTo(1);

        assertThat(created).isNotNull();
        assertThat(((Document)created.get("key")).get("created")).isEqualTo(1);
        assertThat(created.get("expireAfterSeconds")).isEqualTo(60L);

        assertThat(image).isNotNull();
        assertThat(((Document)image.get("key")).get("image")).isEqualTo(1);
    }

    @Test
    void last_user_position_domain() throws ParseException {
        boolean expectedBroadcast = false;
        Long expectedId = 60911L;
        Long expectedImageId = 29240L;
        Long expectedProjectId = 22782L;
        Long expectedSliceId = 29241L;
        String expectedImageName = "CMU-1-Small-Region (1).svs";
        Date expectedDate = mongoDBFormat.parse("2022-02-02T07:40:46.710Z");
        Double expectedRotation = 0d;
        Long expectedUserId = 58L;
        Integer expectedZoom = 5;

        List<List<Double>> expectedLocation = new AreaDTO(
                new be.cytomine.dto.image.Point(-109d, 2548d),
                new be.cytomine.dto.image.Point(683d, 2548d),
                new be.cytomine.dto.image.Point(683d, 2028d),
                new be.cytomine.dto.image.Point(-109d, 2028d)
        ).toMongodbLocation().getCoordinates();

        LastUserPosition lastPosition = new LastUserPosition();
        lastPosition.setId(expectedId);
        lastPosition.setBroadcast(expectedBroadcast);
        lastPosition.setCreated(expectedDate);
        lastPosition.setImage(expectedImageId);
        lastPosition.setImageName(expectedImageName);
        lastPosition.setLocation(expectedLocation);
        lastPosition.setProject(expectedProjectId);
        lastPosition.setRotation(expectedRotation);
        lastPosition.setSlice(expectedSliceId);
        lastPosition.setUser(expectedUserId);
        lastPosition.setZoom(expectedZoom);
        lastPosition = lastUserPositionRepository.insert(lastPosition);

        Document document = retrieveDocument("lastUserPosition", lastPosition.getId());

        assertThat(document.getLong("_id")).isEqualTo(expectedId);
        assertThat(document.getLong("image")).isEqualTo(expectedImageId);
        assertThat(document.getLong("project")).isEqualTo(expectedProjectId);
        assertThat(document.getLong("slice")).isEqualTo(expectedSliceId);
        assertThat(document.getBoolean("broadcast")).isEqualTo(expectedBroadcast);
        assertThat(document.getString("imageName")).isEqualTo(expectedImageName);

        assertThat(document.getDate("created")).isEqualTo(expectedDate);
        assertThat(document.getDouble("rotation")).isEqualTo(expectedRotation);
        assertThat(document.getLong("user")).isEqualTo(expectedUserId);
        assertThat(document.getInteger("zoom")).isEqualTo(expectedZoom);

        @SuppressWarnings("unchecked")
        List<List<Double>> actualLocation = (List<List<Double>>) document.get("location");
        assertThat(actualLocation).isEqualTo(expectedLocation);
    }

    @Test
    void peristent_connection_indexes() {
        ListIndexesIterable<Document> indexes = retrieveIndex("persistentConnection");

        Document indexId = null;
        Document indexUserCreated = null;
        for (Document index : indexes) {
            if (index.get("name").equals("_id_")) {
                indexId = index;
            }
            if (index.get("name").equals("user_1_created_-1")) {
                indexUserCreated = index;
            }
        }
        assertThat(indexes).hasSize(2);
        assertThat(indexId).isNotNull();
        assertThat(((Document)indexId.get("key")).get("_id")).isEqualTo(1);
        assertThat(indexUserCreated).isNotNull();
        assertThat(((Document)indexUserCreated.get("key")).get("user")).isEqualTo(1);
        assertThat(((Document)indexUserCreated.get("key")).get("created")).isEqualTo(-1);
    }

    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void persistent_connection_domain() throws ParseException, JsonProcessingException {
        PersistentConnection connection = new PersistentConnection();
        connection.setId(3073L);
        connection.setCreated(mongoDBFormat.parse("2021-09-22T09:06:32.472Z"));
        connection.setSession("B7850470EED8CD7570E05C50FD5F02F6");
        connection.setProject(null);

        connection = persistentConnectionRepository.insert(connection);

        Document document = retrieveDocument("persistentConnection", connection.getId());

        String expectedResults =
                "{\n" +
                        "   \"_id\":3073,\n" +
                        "   \"created\":{\n" +
                        "      \"$date\":\"2021-09-22T07:06:32.472Z\"\n" + //UTC
                        "   },\n" +
                        "   \"session\":\"B7850470EED8CD7570E05C50FD5F02F6\"\n" +
                        "}";

        String expectedResultsAnotherTimeZone = expectedResults.replaceAll("2021-09-22T07:06:32.472Z", "2021-09-22T09:06:32.472Z");

        assertThat(objectMapper.readTree(document.toJson())).isIn(objectMapper.readTree(expectedResults), objectMapper.readTree(expectedResultsAnotherTimeZone));
    }


    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void persistent_project_connection_domain() throws ParseException, JsonProcessingException {
        PersistentProjectConnection connection = new PersistentProjectConnection();
        connection.setId(3073L);
        connection.setBrowser("firefox");
        connection.setBrowserVersion("92.0.0");
        connection.setCreated(mongoDBFormat.parse("2021-09-22T09:06:32.472Z"));
        connection.setOs("Linux");
        connection.setProject(3063L);
        connection.setSession("B7850470EED8CD7570E05C50FD5F02F6");
        connection.setUser(58L);
        connection.setCountCreatedAnnotations(0);
        connection.setCountViewedImages(0);
        connection.setTime(139164L);

        connection = persistentProjectConnectionRepository.insert(connection);

        Document document = retrieveDocument("persistentProjectConnection", connection.getId());

        String expectedResults =
                "{\n" +
                        "   \"_id\":3073,\n" +
                        "   \"browser\":\"firefox\",\n" +
                        "   \"browserVersion\":\"92.0.0\",\n" +
                        "   \"created\":{\n" +
                        "      \"$date\":\"2021-09-22T07:06:32.472Z\"\n" + //utc
                        "   },\n" +
                        "   \"os\":\"Linux\",\n" +
                        "   \"project\":3063,\n" +
                        "   \"session\":\"B7850470EED8CD7570E05C50FD5F02F6\",\n" +
                        "   \"user\":58,\n" +
                        "   \"countCreatedAnnotations\":0,\n" +
                        "   \"countViewedImages\":0,\n" +
                        "   \"time\":139164\n" +
                        "}";

        String expectedResultsAnotherTimeZone = expectedResults.replaceAll("2021-09-22T07:06:32.472Z","2021-09-22T09:06:32.472Z");

        assertThat(objectMapper.readTree(document.toJson())).isIn(objectMapper.readTree(expectedResults), objectMapper.readTree(expectedResultsAnotherTimeZone));
        // TODO: test index
    }


    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void persistent_image_consultation_domain() throws ParseException, JsonProcessingException {
        PersistentImageConsultation consultation = new PersistentImageConsultation();
        consultation.setId(3975L);
        consultation.setUser(58L);
        consultation.setImage(3962L);
        consultation.setProject(3063L);
        consultation.setSession("B6AC04394B9D9F746A15E511C5DC243B");
        consultation.setProjectConnection(3974L);
        consultation.setCreated(mongoDBFormat.parse("2021-09-23T08:55:02.602Z"));
        consultation.setSession("B6AC04394B9D9F746A15E511C5DC243B");
        consultation.setMode("view");
        consultation.setImageName("CMU-1-Small-Region (1).svs");
        consultation.setImageThumb("http://localhost-core/api/imageinstance/3962/thumb.png?maxSize=256");
        consultation.setTime(12149L);
        consultation.setCountCreatedAnnotations(0);

        consultation = persistentImageConsultationRepository.insert(consultation);

        Document document = retrieveDocument("persistentImageConsultation", consultation.getId());

        String expectedResults =
                "{\n" +
                        "\"_id\": 3975,\n" +
                        "\"created\": {\n" +
                        "\"$date\": \"2021-09-23T06:55:02.602Z\"\n" + //UTC
                        "},\n" +
                        "\"image\": 3962,\n" +
                        "\"imageName\": \"CMU-1-Small-Region (1).svs\",\n" +
                        "\"imageThumb\": \"http://localhost-core/api/imageinstance/3962/thumb.png?maxSize=256\",\n" +
                        "\"mode\": \"view\",\n" +
                        "\"project\": 3063,\n" +
                        "\"projectConnection\": 3974,\n" +
                        "\"session\": \"B6AC04394B9D9F746A15E511C5DC243B\",\n" +
                        "\"user\": 58,\n" +
                        "\"countCreatedAnnotations\": 0,\n" +
                        "\"time\": 12149\n" +
                        "}";

        String expectedResultsAnotherTimeZone = expectedResults.replaceAll("2021-09-23T06:55:02.602Z", "2021-09-23T08:55:02.602Z");

        assertThat(objectMapper.readTree(document.toJson())).isIn(objectMapper.readTree(expectedResults), objectMapper.readTree(expectedResultsAnotherTimeZone));
        // TODO: issue with Date: created seems to have issue with UTC

        // TODO: test index
    }

    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void persistent_user_position_index() throws ParseException, JsonProcessingException {
        ListIndexesIterable<Document> indexes = retrieveIndex("persistentUserPosition");
        Document indexId = null;
        Document indexUserImageSliceCreated = null;
        Document locationImageSlice = null;
        Document image = null;
        for (Document index : indexes) {
            if (index.get("name").equals("_id_")) {
                indexId = index;
            }
            if (index.get("name").equals("user_1_image_1_slice_1_created_-1")) {
                indexUserImageSliceCreated = index;
            }

            if (index.get("name").equals("location_2d_image_1_slice_1")) {
                locationImageSlice = index;
            }

            if (index.get("name").equals("image_1")) {
                image = index;
            }
        }
        assertThat(indexes).hasSize(4);

        assertThat(indexId).isNotNull();
        assertThat(((Document)indexId.get("key")).get("_id")).isEqualTo(1);

        assertThat(indexUserImageSliceCreated).isNotNull();
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("user")).isEqualTo(1);
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("image")).isEqualTo(1);
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("slice")).isEqualTo(1);
        assertThat(((Document)indexUserImageSliceCreated.get("key")).get("created")).isEqualTo(-1);

        assertThat(locationImageSlice).isNotNull();
        assertThat(((Document)locationImageSlice.get("key")).get("location")).isEqualTo("2d");
        assertThat(((Document)locationImageSlice.get("key")).get("image")).isEqualTo(1);
        assertThat(((Document)locationImageSlice.get("key")).get("slice")).isEqualTo(1);

        assertThat(image).isNotNull();
        assertThat(((Document)image.get("key")).get("image")).isEqualTo(1);
    }

    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void persistent_user_position_domain() throws ParseException, JsonProcessingException {
        ImageInstance imageInstance = new ImageInstance();
        imageInstance.setId(3962L);
        Project project = new Project();
        project.setId(3063L);
        SliceInstance sliceInstance = new SliceInstance();
        sliceInstance.setId(3963L);
        User user = new User();
        user.setId(58L);

        PersistentUserPosition lastPosition = new PersistentUserPosition();
        lastPosition.setId(3977L);
        lastPosition.setBroadcast(false);
        lastPosition.setCreated(mongoDBFormat.parse("2021-09-23T08:55:03.608Z"));
        lastPosition.setImage(imageInstance.getId());
        lastPosition.setImageName("CMU-1-Small-Region (1).svs");
        lastPosition.setLocation(new AreaDTO(
                new be.cytomine.dto.image.Point(-3338d,3128d),
                new be.cytomine.dto.image.Point(5558d,3128d),
                new be.cytomine.dto.image.Point(5558d,-160d),
                new be.cytomine.dto.image.Point(-3338d,-160d)
        ).toMongodbLocation().getCoordinates());
        lastPosition.setProject(project.getId());
        lastPosition.setRotation(0d);
        lastPosition.setSession("B6AC04394B9D9F746A15E511C5DC243B");
        lastPosition.setSlice(sliceInstance.getId());
        lastPosition.setUser(user.getId());
        lastPosition.setZoom(2);

        lastPosition = persistentUserPositionRepository.insert(lastPosition);

        Document document = retrieveDocument("persistentUserPosition", lastPosition.getId());

        MongoCollection<Document> persistentProjectConnectionFromGrails = mongoClient.getDatabase(mongoDatabaseName).getCollection("persistentUserPosition");
        ArrayList<Document> results = persistentProjectConnectionFromGrails.find(eq("_id", 3977L))
                .into(new ArrayList<>());
        for (Document result : results) {
            System.out.println(result.toJson());
        }

        String expectedResults =
                "{\n" +
                        "\"_id\": 3977,\n" +
                        "\"broadcast\": false,\n" +
                        "\"created\": {\n" +
                        "\"$date\": \"2021-09-23T06:55:03.608Z\"\n" + // //UTC
                        "},\n" +
                        "\"image\": 3962,\n" +
                        "\"imageName\": \"CMU-1-Small-Region (1).svs\",\n" +
                        "\"location\": [\n" +
                        "[-3338.0, 3128.0],\n" +
                        "[5558.0, 3128.0],\n" +
                        "[5558.0, -160.0],\n" +
                        "[-3338.0, -160.0]\n" +
                        "],\n" +
                        "\"project\": 3063,\n" +
                        "\"rotation\": 0.0,\n" +
                        "\"session\": \"B6AC04394B9D9F746A15E511C5DC243B\",\n" +
                        "\"slice\": 3963,\n" +
                        "\"user\": 58,\n" +
                        "\"zoom\": 2\n" +
                        "}";

        String expectedResultsAnotherTimeZone = expectedResults.replaceAll("2021-09-23T06:55:03.608Z", "2021-09-23T08:55:03.608Z");

        assertThat(objectMapper.readTree(document.toJson())).isIn(objectMapper.readTree(expectedResults), objectMapper.readTree(expectedResultsAnotherTimeZone));
       //Fails because rotation is a string
    }


    /**
     * Check that the format of MongoDB is the same as
     */
    @Test
    void annotation_action_indexes() throws ParseException, JsonProcessingException {
        ListIndexesIterable<Document> indexes = retrieveIndex("annotationAction");
        Document indexId = null;
        Document indexUserDate = null;
        for (Document index : indexes) {
            if (index.get("name").equals("_id_")) {
                indexId = index;
            }
            if (index.get("name").equals("user_1_image_1_created_-1")) {
                indexUserDate = index;
            }
        }
        assertThat(indexes).hasSize(2);
        assertThat(indexId).isNotNull();
        assertThat(((Document)indexId.get("key")).get("_id")).isEqualTo(1);
        assertThat(indexUserDate).isNotNull();
        assertThat(((Document)indexUserDate.get("key")).get("user")).isEqualTo(1);
        assertThat(((Document)indexUserDate.get("key")).get("image")).isEqualTo(1);
        assertThat(((Document)indexUserDate.get("key")).get("created")).isEqualTo(-1);
    }


    private Document retrieveDocument(String collectionName, Long id) {
        MongoCollection<Document> persistentProjectConnection = mongoClient.getDatabase(mongoDatabaseName).getCollection(collectionName);

        List<Document> results = persistentProjectConnection.find(eq("_id", id))
                .into(new ArrayList<>());
        System.out.println("****************************");
        assertThat(results).hasSize(1);
        Document document = results.get(0);
        return document;
    }

    private ListIndexesIterable<Document> retrieveIndex(String collectionName) {
        MongoCollection<Document> persistentProjectConnection = mongoClient.getDatabase(mongoDatabaseName).getCollection(collectionName);
        return persistentProjectConnection.listIndexes();
    }
}
