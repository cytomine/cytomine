package be.cytomine.appengine.unit.models;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import be.cytomine.appengine.models.task.formats.FileFormat;
import be.cytomine.appengine.models.task.formats.JpegFormat;
import be.cytomine.appengine.models.task.formats.PngFormat;
import be.cytomine.appengine.models.task.formats.TiffFormat;
import be.cytomine.appengine.models.task.formats.WSIDicomFormat;
import be.cytomine.appengine.models.task.formats.ZipFormat;

public class ImageFormatTest {

    private static final int EXPECTED_WIDTH = 680;

    private static final int EXPECTED_WSIDICOM_WIDTH = 240;

    private static final int EXPECTED_WSIDICOM_HEIGHT = 240;

    private static final int EXPECTED_HEIGHT = 512;

    private static Map<String, File> images;

    @BeforeAll
    public static void setUp() throws IOException {
        images = new HashMap<>();
        images.put("PNG", loadImage("src/test/resources/artifacts/images/image.png"));
        images.put("JPEG", loadImage("src/test/resources/artifacts/images/image.jpg"));
        images.put("TIFF", loadImage("src/test/resources/artifacts/images/image.tif"));
        images.put("ZIP", loadImage("src/test/resources/artifacts/images/image.zip"));
        images.put("WSIDICOM", loadImage("src/test/resources/artifacts/images/image"));
    }

    @AfterAll
    public static void tearDown() {
        images.clear();
    }

    private static File loadImage(String imagePath) throws IOException {
        return new File(imagePath);
    }

    private static Stream<Arguments> streamImageFormat() {
        return Stream.of(
            Arguments.of(new PngFormat(), "PNG"),
            Arguments.of(new JpegFormat(), "JPEG"),
            Arguments.of(new TiffFormat(), "TIFF"),
            Arguments.of(new ZipFormat(), "ZIP"),
            Arguments.of(new WSIDicomFormat(), "WSIDICOM")
        );
    }

    @ParameterizedTest
    @MethodSource("streamImageFormat")
    public void testCheckSignature(FileFormat format, String formatKey) {
        File image = images.get(formatKey);
        Assertions.assertTrue(format.checkSignature(image), "Image signature should be valid.");
    }

    @ParameterizedTest
    @MethodSource("streamImageFormat")
    public void testGetDimensions(FileFormat format, String formatKey)
        throws IOException {
        if (formatKey.equalsIgnoreCase("ZIP")) {
            return;
        }
        File image = images.get(formatKey);
        if (formatKey.equalsIgnoreCase("WSIDICOM")) {
            image = Files.walk(image.toPath())
                .filter(Files::isRegularFile)
                .max(Comparator.comparingLong(p -> p.toFile().length()))
                .map(Path::toFile)
                .orElse(null);

        }

        Dimension dimension = format.getDimensions(image);

        if (formatKey.equalsIgnoreCase("WSIDICOM")) {

            Assertions.assertEquals(
                EXPECTED_WSIDICOM_WIDTH,
                dimension.getWidth(),
                "Width should be " + EXPECTED_WIDTH + " pixels."
            );

            Assertions.assertEquals(
                EXPECTED_WSIDICOM_HEIGHT,
                dimension.getHeight(),
                "Height should be " + EXPECTED_HEIGHT + " pixels."
            );

        } else {
            Assertions.assertEquals(
                EXPECTED_WIDTH,
                dimension.getWidth(),
                "Width should be " + EXPECTED_WIDTH + " pixels."
            );
            Assertions.assertEquals(
                EXPECTED_HEIGHT,
                dimension.getHeight(),
                "Height should be " + EXPECTED_HEIGHT + " pixels."
            );
        }
    }

    @ParameterizedTest
    @MethodSource("streamImageFormat")
    public void testValidate(FileFormat format, String formatKey) {
        if (formatKey.equalsIgnoreCase("ZIP")) {
            return;
        }
        File image = images.get(formatKey);
        Assertions.assertTrue(format.validate(image), "Validation should return true.");
    }

    @Test
    public void testValidateWithoutFileExtension(@TempDir Path tempDir) throws IOException {
        // task run inputs are streamed to storage under the parameter name, without extension
        File withoutExtension = tempDir.resolve("a_fixed_image").toFile();
        Files.copy(Path.of("src/test/resources/artifacts/images/image.tif"), withoutExtension.toPath());

        TiffFormat format = new TiffFormat();

        Assertions.assertTrue(format.validate(withoutExtension), "Validation should only rely on the file content.");
        Dimension dimension = format.getDimensions(withoutExtension);
        Assertions.assertEquals(EXPECTED_WIDTH, dimension.getWidth(), "Width should be " + EXPECTED_WIDTH + " pixels.");
        Assertions.assertEquals(EXPECTED_HEIGHT, dimension.getHeight(), "Height should be " + EXPECTED_HEIGHT + " pixels.");
    }

    @Test
    public void testValidateTiffWithoutSamplesPerPixel(@TempDir Path tempDir) throws IOException {
        // streamed under the parameter name, without extension, like task run inputs
        File withoutExtension = tempDir.resolve("a_fixed_image").toFile();
        Files.copy(
            Path.of("src/test/resources/artifacts/images/image-no-samples-per-pixel.tif"),
            withoutExtension.toPath()
        );

        TiffFormat format = new TiffFormat();

        // must not throw a NullPointerException when the SamplesPerPixel field is absent
        Assertions.assertFalse(format.validate(withoutExtension), "Validation should return false.");
    }
}
