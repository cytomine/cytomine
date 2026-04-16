package be.cytomine.service.report;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class SpreadsheetReportServiceTests {

    @Autowired
    SpreadsheetReportService spreadsheetReportService;

    Object[][] validData = {
        {
            "ID",
            "Area (microns²)",
            "Perimeter (mm)",
            "X",
            "Y",
            "Image Filename",
            "View annotation picture",
            "View annotation on image"
        },
        {
            '2',
            (float) 23298.5267702416,
            0.614768015861511,
            736,
            true,
            (long) 6836067,
            "https://...",
            "https://...",
            "0.614768015861511",
            "736",
            "1978.375",
            "6836067"
        },
    };
    String validDataResult = "ID;Area (microns²);Perimeter (mm);X;Y;Image Filename;View annotation picture;"
        + "View annotation on image\r\n2;23298.527;0.614768015861511;736;true;6836067"
        + ";https://...;https://...;0.614768015861511;736;1978.375;6836067\r\n";

    Object[][] delimiterInData = {{"ID", null, "Perimeter ; (mm)"}};
    String delimiterInDataResult = "ID;;\"Perimeter ; (mm)\"\r\n";

    Object[][] nullData = {{"ID", null, "Perimeter (mm)"}};
    String nullCsvDataResult = "ID;;Perimeter (mm)\r\n";

    Object[][] emptyData = {{}};
    String emptyCsvDataResult = "\r\n";

    @Disabled("ignored for now seems to not work on self-hosted runners")
    @Test
    public void shouldGenerateSpreadsheetSuccessfullyWithValidData() {
        assertEquals(createSpreadsheet(validData), validDataResult);
    }

    @Test
    public void generateSpreadsheetsWithDelimiterInDataWorks() {
        assertEquals(createSpreadsheet(delimiterInData), delimiterInDataResult);
    }

    @Test
    public void nullDataReturnEmptyString() {
        assertEquals(createSpreadsheet(nullData), nullCsvDataResult);
    }

    @Test
    public void emptyDataArrayReturnEmptyString() {
        assertEquals(createSpreadsheet(emptyData), emptyCsvDataResult);
    }

    private String createSpreadsheet(Object[][] data) {
        byte[] csvByteArray = spreadsheetReportService.writeSpreadsheet(data);
        return new String(csvByteArray);
    }
}
