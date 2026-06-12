package be.cytomine.service.report;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.exceptions.ServerException;
import be.cytomine.service.utils.ReportFormatService;
import be.cytomine.utils.DateUtils;
import be.cytomine.utils.JsonObject;

import static be.cytomine.utils.DateUtils.DATE_TIME_FORMAT;

@Slf4j
@Service
@AllArgsConstructor
public class ReportService {

    public static final boolean HAS_PAGINATION = true;
    public static final boolean HAS_HEADER = true;
    public static final List<ReportColumn> ANNOTATION_REPORT_COLUMNS = List.of(
        new ReportColumn("id", "Id", (float) 0.05),
        new ReportColumn("area", "Area (microns²)", (float) 0.10),
        new ReportColumn("perimeter", "Perimeter (mm)", (float) 0.10),
        new ReportColumn("X", "X", (float) 0.05),
        new ReportColumn("Y", "Y", (float) 0.05),
        new ReportColumn("image", "Image Id", (float) 0.07),
        new ReportColumn("filename", "Image Filename", (float) 0.10),
        new ReportColumn("user", "User", (float) 0.05),
        new ReportColumn("term", "Term", (float) 0.05),
        new ReportColumn("cropURL", "View annotation picture", (float) 0.19),
        new ReportColumn("imageURL", "View annotation on image", (float) 0.19)
    );
    public static final List<ReportColumn> USER_REPORT_COLUMNS = List.of(
        new ReportColumn("username", "Username", (float) 0.5),
        new ReportColumn("name", "Name", (float) 0.5)
    );
    public static final List<ReportColumn> IMAGE_CONSULTATION_COLUMNS = List.of(
        new ReportColumn("time", "Cumulated duration (ms)", (float) 0.10),
        new ReportColumn("first", "First consultation", (float) 0.15),
        new ReportColumn("last", "Last consultation", (float) 0.15),
        new ReportColumn("frequency", "Number of consultations", (float) 0.10),
        new ReportColumn("imageId", "Id of image", (float) 0.10),
        new ReportColumn("imageName", "Name", (float) 0.10),
        new ReportColumn("imageThumb", "Thumb", (float) 0.20),
        new ReportColumn(
            "numberOfCreatedAnnotations", "Number of created annotations",
            (float) 0.10
        )
    );
    public static final List<ReportColumn> CONNECTION_HISTORY_REPORT_COLUMNS = List.of(
        new ReportColumn("created", "Date", (float) 0.20),
        new ReportColumn("time", "Duration (ms)", (float) 0.10),
        new ReportColumn("countViewedImages", "Number of viewed images", (float) 0.10),
        new ReportColumn(
            "countCreatedAnnotations", "Number of created annotations",
            (float) 0.10
        ),
        new ReportColumn("os", "Operating System", (float) 0.10),
        new ReportColumn("browser", "Browser", (float) 0.20),
        new ReportColumn("browserVersion", "Browser Version", (float) 0.20)
    );

    private final PDFReportService pdfReportService;

    private final SpreadsheetReportService spreadsheetReportService;

    private final ReportFormatService reportFormatService;

    public byte[] generateConnectionHistoryReport(
        String projectName, String userName,
        List<JsonObject> data
    ) {
        String title = getConnectionHistoryReportTitle(projectName, userName);
        return generateJsonObjectReport(title, data, CONNECTION_HISTORY_REPORT_COLUMNS);
    }

    public byte[] generateImageConsultationReport(
        String projectName, String userName,
        List<JsonObject> data
    ) {
        String title = getImageConsultationReportTitle(projectName, userName);
        return generateJsonObjectReport(title, data, IMAGE_CONSULTATION_COLUMNS);
    }

    public byte[] generateUsersReport(
        String projectName, List<Map<String, Object>> data,
        String format
    ) throws ServerException {
        Object[][] dataForReport =
            reportFormatService.formatMapForReport(USER_REPORT_COLUMNS, data);
        return generateReport(
            getUserReportTitle(projectName), dataForReport, USER_REPORT_COLUMNS,
            format
        );
    }

    public byte[] generateAnnotationsReport(
        String projectName, Set<String> terms,
        Set<String> users, List<Map<String, Object>> data,
        String format
    ) throws ServerException {
        Object[][] dataForReport =
            reportFormatService.formatAnnotationsForReport(ANNOTATION_REPORT_COLUMNS, data);
        return generateReport(
            getAnnotationReportTitle(projectName, terms, users), dataForReport,
            ANNOTATION_REPORT_COLUMNS, format
        );
    }

    private byte[] generateJsonObjectReport(
        String title, List<JsonObject> data,
        List<ReportColumn> columns
    ) {
        Object[][] dataForReport = reportFormatService.formatJsonObjectForReport(columns, data);
        return generateReport(title, dataForReport, columns, "csv");
    }

    public byte[] generateReport(
        String title, Object[][] data, List<ReportColumn> columns,
        String format
    ) throws ServerException {
        float[] columnWidth = reportFormatService.getColumnWidth(columns);
        switch (format) {
            case "csv":
                return spreadsheetReportService.writeSpreadsheet(data);
            case "xls":
                return spreadsheetReportService.writeSpreadsheetXLS(data);
            case "pdf":
                return pdfReportService.writePDF(
                    data, title, columnWidth, HAS_PAGINATION,
                    HAS_HEADER
                );
            default:
                log.error("Format should be one of these types: 'pdf', 'csv' or 'xls'.");
                throw new ServerException(
                    "Failed to generate report, invalid format. Format should be one of these "
                        + "types: 'pdf', 'csv' or 'xls'.");
        }
    }

    private String getConnectionHistoryReportTitle(String projectName, String userName) {
        return "Connections of user " + userName + " to project " + projectName;
    }

    private String getImageConsultationReportTitle(String projectName, String userName) {
        return "Consultations of images into project " + projectName + " by user " + userName;
    }

    private String getAnnotationReportTitle(
        String projectName, Set<String> terms,
        Set<String> users
    ) {
        return "Annotations in " + projectName + " created by " + String.join(" or ", users) + " "
            + "and associated with " + String.join(" or ", terms) + " @ "
            + DateUtils.getLocaleDate(new Date());
    }

    private String getUserReportTitle(String projectName) {
        return "User in " + projectName + " created @ " + DateUtils.getLocaleDate(new Date());
    }

    public String getConnectionHistoryReportFileName(String format, Long projectId, Long userId) {
        return "user_" + userId + "_connections_project_" + projectId + "_"
            + DateUtils.getSimpleFormatLocaleDate(new Date()) + "." + format;
    }

    public String getImageConsultationReportFileName(String format, Long projectId, Long userId) {
        return "image_consultations_of_user_" + userId + "_project_" + projectId + "_"
            + DateUtils.getSimpleFormatLocaleDate(new Date()) + "." + format;
    }

    public String getAnnotationReportFileName(String format, Long projectId) {
        return DateUtils.getSimpleFormatLocaleDate(new Date()) + "_annotations_project" + projectId
            + "." + format;
    }

    public String getAnnotationReportFileName(String format, String projectName) {
        return LocalDateTime.now().format(DATE_TIME_FORMAT) + "_" + projectName + "_annotations." + format;
    }

    public String getUsersReportFileName(String format, Long projectId) {
        return DateUtils.getSimpleFormatLocaleDate(new Date()) + "_users_project" + projectId + "."
            + format;
    }

}
